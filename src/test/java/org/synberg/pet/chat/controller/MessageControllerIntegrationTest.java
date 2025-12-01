package org.synberg.pet.chat.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerIntegrationTest {
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

    private int createChat(int user1Id, int user2Id) throws Exception {
        String chatJSON = """
                {
                    "user1Id": %d,
                    "user2Id": %d
                }
                """.formatted(user1Id, user2Id);

        MvcResult res = mockMvc.perform(
                        post("/api/chats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(chatJSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.parse(res.getResponse().getContentAsString()).read("$.id", Integer.class);
    }

    @Test
    void fullCrudScenario() throws Exception {

        // GET non-existing message (404 not found)
        mockMvc.perform(get("/api/messages/999999"))
                .andExpect(status().isNotFound());

        // CREATE user1 (201 created)
        int id1 = createUser("vasya", "Vasya");

        // CREATE user2 (201 created)
        int id2 = createUser("sasha", "Sasha");

        // CREATE chat (201 created)
        int chatId = createChat(id1, id2);

        // CREATE user1 message (201 created)
        String messageJSON = """
                {
                    "text": "MyMessage",
                    "userId": %d,
                    "chatId": %d
                }
                """.formatted(id1, chatId);

        MvcResult messageRes = mockMvc.perform(
                        post("/api/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(messageJSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        int messageId = JsonPath.parse(messageRes.getResponse().getContentAsString()).read("$.id", Integer.class);

        // GET original message and check content (200 ok)
        MvcResult getBeforeUpdate = mockMvc.perform(get("/api/messages/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("MyMessage"))
                .andReturn();

        // editedAt must be null
        String beforeJson = getBeforeUpdate.getResponse().getContentAsString();
        Object editedAtBefore = JsonPath.parse(beforeJson).read("$.editedAt");
        assertThat(editedAtBefore).isNull();

        // UPDATE user1 message (200 ok)
        String updateJSON = """
                {
                    "text": "UpdatedMessage"
                }
                """;

        mockMvc.perform(
                        put("/api/messages/" + messageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // GET updated user1 message and check content (200 ok)
        MvcResult getAfterUpdate = mockMvc.perform(get("/api/messages/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("UpdatedMessage"))
                .andReturn();

        // editedAt must NOT be null
        String afterJson = getAfterUpdate.getResponse().getContentAsString();
        Object editedAtAfter = JsonPath.parse(afterJson).read("$.editedAt");
        assertThat(editedAtAfter).isNotNull();

        // UPDATE non-existing message (404 not found)
        mockMvc.perform(
                        put("/api/messages/999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                )
                .andExpect(status().isNotFound());

        // DELETE user1 message (204 no content)
        mockMvc.perform(delete("/api/messages/" + messageId))
                .andExpect(status().isNoContent());

        // DELETE chat (204 no content)
        mockMvc.perform(delete("/api/chats/" + chatId))
                .andExpect(status().isNoContent());

        // DELETE user1 (204 no content)
        mockMvc.perform(delete("/api/users/" + id1))
                .andExpect(status().isNoContent());

        // DELETE user2 (204 no content)
        mockMvc.perform(delete("/api/users/" + id2))
                .andExpect(status().isNoContent());

        // DELETE non-existing message (404 not found)
        mockMvc.perform(delete("/api/messages/" + messageId))
                .andExpect(status().isNotFound());
    }
}
