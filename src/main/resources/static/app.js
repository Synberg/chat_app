const API_BASE = "http://localhost:8080/api";

let currentUser = null;
let currentChat = null;
let stompClient = null;
let chatSubscription = null;

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".hidden-when-logged-out").forEach(el => el.style.display = "none");
    document.getElementById("logoutBtn").style.display = "none";

    document.getElementById("loginBtn").onclick = () => {
        const username = document.getElementById("username").value;
        const displayName = document.getElementById("displayName").value;
        login(username, displayName);
    };

    document.getElementById("createChatBtn").onclick = () => {
        const other = document.getElementById("otherUser").value;
        createChat(other);
    };

    document.getElementById("sendMsgBtn").onclick = () => {
        const text = document.getElementById("messageText").value;
        if (text.trim()) sendMessage(text);
    };

    document.getElementById("logoutBtn").onclick = logout;
});

// ---------------- REST ----------------
async function login(username, displayName) {
    const res = await fetch(`${API_BASE}/users`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ username, displayName })
    });
    currentUser = await res.json();

    document.getElementById("login-block").style.display = "none";
    document.getElementById("logoutBtn").style.display = "block";
    document.querySelectorAll(".hidden-when-logged-out").forEach(el => el.style.display = "block");

    connectWebSocket();
    await loadChats();
}

async function loadChats() {
    const res = await fetch(`${API_BASE}/users/${currentUser.id}/chats`);
    const chats = await res.json();

    const list = document.getElementById("chatsList");
    list.innerHTML = "";

    chats.forEach(c => {
        const other = c.user1Id === currentUser.id ? c.user2Username : c.user1Username;

        const li = document.createElement("li");
        li.textContent = `Chat #${c.id} with ${other}`;

        // кнопка удаления
        const delBtn = document.createElement("button");
        delBtn.textContent = "🗑";
        delBtn.className = "delete-chat-btn";
        delBtn.onclick = (e) => {
            e.stopPropagation(); // чтобы не открывался чат
            deleteChat(c.id);
        };

        li.appendChild(delBtn);
        li.onclick = () => openChat(c.id);
        list.appendChild(li);
    });
}

async function createChat(otherUsername) {
    await fetch(`${API_BASE}/chats`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ username1: currentUser.username, username2: otherUsername })
    });
    // WS пришлет update -> loadChats
}

async function openChat(chatId) {
    currentChat = chatId;
    const res = await fetch(`${API_BASE}/chats/${chatId}/messages`);
    const messages = await res.json();
    renderMessages(messages);

    document.getElementById("chat-title").textContent = `Chat #${chatId}`;
    subscribeToChat(chatId);
}

async function deleteChat(chatId) {
    if (!confirm("Delete this chat?")) return;

    try {
        const res = await fetch(`${API_BASE}/chats/${chatId}`, { method: "DELETE" });
        if (!res.ok) {
            console.error("deleteChat failed", res.status);
            return;
        }

        // Если текущий чат удалён — сбросить UI
        if (currentChat === chatId) {
            currentChat = null;
            document.getElementById("messages").innerHTML = "";
            document.getElementById("chat-title").textContent = "Select a chat";
        }

        // Перерисовать список чатов
        await loadChats();
    } catch (e) {
        console.error("deleteChat error", e);
    }
}

async function sendMessage(text) {
    if (!currentChat) return;
    await fetch(`${API_BASE}/messages`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ text, userId: currentUser.id, chatId: currentChat })
    });
    document.getElementById("messageText").value = "";
}

async function editMessage(messageId, oldText) {
    const newText = prompt("Edit message:", oldText);
    if (!newText || newText === oldText) return;
    await fetch(`${API_BASE}/messages/${messageId}`, {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ text: newText })
    });
}

async function deleteMessage(messageId) {
    if (!confirm("Delete this message?")) return;
    await fetch(`${API_BASE}/messages/${messageId}`, { method: "DELETE" });
}

// ---------------- Render ----------------
function renderMessages(messages) {
    const messagesDiv = document.getElementById("messages");
    messagesDiv.innerHTML = "";
    messages.forEach(renderMessage);
}

function renderMessage(msg) {
    const messagesDiv = document.getElementById("messages");

    const wrapper = document.createElement("div");
    wrapper.className = "msg-wrapper " + (msg.userId === currentUser.id ? "mine" : "theirs");

    const time = formatDateIsoToLocal(msg.createdAt);
    const edited = msg.edited ? `<span class="edited">(edited)</span>` : "";

    let controls = "";
    if (msg.userId === currentUser.id) {
        controls = `
            <button class="msg-btn edit" onclick="editMessage(${msg.id}, '${escapeJs(msg.text)}')">✏</button>
            <button class="msg-btn delete" onclick="deleteMessage(${msg.id})">🗑</button>
        `;
    }

    wrapper.innerHTML = `
        <div class="msg-bubble">
            <div class="msg-header">
                <span class="author">${msg.userDisplayName}</span>
                <span class="time">${time}</span>
                ${edited}
                ${controls}
            </div>
            <div class="msg-text">${escapeHtml(msg.text)}</div>
        </div>
    `;

    messagesDiv.appendChild(wrapper);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function escapeHtml(s) {
    if (!s) return "";
    return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
}

function escapeJs(s) {
    if (!s) return "";
    return s.replaceAll("'", "\\'");
}

function formatDateIsoToLocal(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    return d.toLocaleString();
}

// ---------------- WebSocket ----------------
function connectWebSocket() {
    const socket = new SockJS(`${API_BASE.replace("/api","")}/ws`);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, frame => {
        console.log("WS connected", frame);

        // новые чаты
        stompClient.subscribe(`/topic/newChat.${currentUser.id}`, async () => {
            await loadChats();
        });
    });
}

function subscribeToChat(chatId) {
    if (!stompClient) return;

    if (chatSubscription) {
        try { chatSubscription.unsubscribe(); } catch {}
    }

    chatSubscription = stompClient.subscribe(`/topic/chat.${chatId}`, async () => {
        // просто перерисовываем все чаты и текущий чат
        await loadChats();
        if (currentChat) await openChat(currentChat);
    });
}

// ---------------- Logout ----------------
function logout() {
    currentUser = null;
    currentChat = null;
    if (stompClient && stompClient.connected) stompClient.disconnect();

    document.getElementById("login-block").style.display = "flex";
    document.getElementById("logoutBtn").style.display = "none";
    document.querySelectorAll(".hidden-when-logged-out").forEach(el => el.style.display = "none");
    document.getElementById("messages").innerHTML = "";
    document.getElementById("chatsList").innerHTML = "";
    document.getElementById("chat-title").textContent = "Select a chat";
    document.getElementById("username").value = "";
    document.getElementById("displayName").value = "";
}
