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
        // CREATE user1 (201 created)
        int user1Id = createUser("vasya", "Vasya");

        // CREATE user2 (201 created)
        int user2Id = createUser("sasha", "Sasha");

        // GET non-existing chat (404 not found)
        mockMvc.perform(get("/api/chats/999999"))
                .andExpect(status().isNotFound());

        // CREATE chat (201 created)
        String chatJSON = """
                {
                    "user1Id": %d,
                    "user2Id": %d
                }
                """.formatted(user1Id, user2Id);
        MvcResult chatRes = mockMvc.perform(
                post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatJSON)
                )
                .andExpect(status().isCreated())
                .andReturn();


        int chatId = JsonPath.parse(chatRes.getResponse().getContentAsString()).read("$.id", Integer.class);

        // GET  chat (200 ok)
        mockMvc.perform(get("/api/chats/" + chatId))
                .andExpect(status().isOk());

        // Create existing chat (user1, user2) (409 conflict)
        mockMvc.perform(
                        post("/api/chats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(chatJSON)
                )
                .andExpect(status().isConflict());

        // Create existing chat (user2, user1) (409 conflict)
        String chatJSON2 = """
                {
                    "user1Id": %d,
                    "user2Id": %d
                }
                """.formatted(user2Id, user1Id);

        mockMvc.perform(
                        post("/api/chats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(chatJSON2)
                )
                .andExpect(status().isConflict());

        // DELETE chat (204 no content)
        mockMvc.perform(delete("/api/chats/" + chatId))
                .andExpect(status().isNoContent());

        // DELETE non-existing chat (404 not found)
        mockMvc.perform(delete("/api/chats/" + chatId))
                .andExpect(status().isNotFound());

        // DELETE user1 (204 no content)
        deleteUser(user1Id);

        // DELETE user2 (204 no content)
        deleteUser(user2Id);
    }
}
