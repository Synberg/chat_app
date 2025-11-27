package org.synberg.pet.chat.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private int createUser(String username, String displayName) throws Exception {
        String userJson = """
                {
                    "username": "%s",
                    "displayName": "%s"
                }
                """.formatted(username, displayName);

        MvcResult res = mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJson)
                )
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.parse(res.getResponse().getContentAsString()).read("$.id", Integer.class);
    }

    private void deleteUser(int id) throws Exception {
        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void fullCrudScenario() throws Exception {
        // 1. CREATE user1 (201 created)
        int id1 = createUser("vasya", "Vasya");

        // 2. CREATE user2 (201 created)
        int id2 = createUser("sasha", "Sasha");

        // 3. GET non-existing chat (404 not found)
        mockMvc.perform(get("/api/chats/999999"))
                .andExpect(status().isNotFound());

        // 4. CREATE chat (201 created)
        String chatJSON = """
                {
                    "user1Id": %d,
                    "user2Id": %d
                }
                """.formatted(id1, id2);
        MvcResult chatRes = mockMvc.perform(
                post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatJSON)
                )
                .andExpect(status().isCreated())
                .andReturn();


        int chatId = JsonPath.parse(chatRes.getResponse().getContentAsString()).read("$.id", Integer.class);

        // 5. GET  chat (200 ok)
        mockMvc.perform(get("/api/chats/" + chatId))
                .andExpect(status().isOk());

        // 6. Create existing chat (user1, user2) (409 conflict)
        mockMvc.perform(
                        post("/api/chats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(chatJSON)
                )
                .andExpect(status().isConflict());

        // 7. Create existing chat (user2, user1) (409 conflict)
        String chatJSON2 = """
                {
                    "user1Id": %d,
                    "user2Id": %d
                }
                """.formatted(id2, id1);

        mockMvc.perform(
                        post("/api/chats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(chatJSON2)
                )
                .andExpect(status().isConflict());

        // 8. DELETE chat (204 no content)
        mockMvc.perform(delete("/api/chats/" + chatId))
                .andExpect(status().isNoContent());

        // 9. DELETE non-existing chat (404 not found)
        mockMvc.perform(delete("/api/chats/999999"))
                .andExpect(status().isNotFound());

        // 10. DELETE user1 (204 no content)
        deleteUser(id1);

        // 11. DELETE user2 (204 no content)
        deleteUser(id2);
    }
}
