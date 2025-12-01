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
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullCrudScenario() throws Exception {

        // GET non-existing user (404 not found)
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound());

        // CREATE user1 (201 created)
        String user1 = """
                {
                    "username": "vasya123",
                    "displayName": "Vasya"
                }
                """;

        MvcResult res1 = mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(user1)
                )
                .andExpect(status().isCreated())
                .andReturn();

        int user1Id = JsonPath.parse(res1.getResponse().getContentAsString()).read("$.id", Integer.class);

        // GET user1 (200 ok)
        mockMvc.perform(get("/api/users/" + user1Id))
                .andExpect(status().isOk());

        // CREATE user2 (201 created)
        String user2 = """
                {
                    "username": "vasya456",
                    "displayName": "Vasya2"
                }
                """;

        MvcResult res2 = mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(user2)
                )
                .andExpect(status().isCreated())
                .andReturn();

        int user2Id = JsonPath.parse(res2.getResponse().getContentAsString()).read("$.id", Integer.class);

        // GET user2 (200 ok)
        mockMvc.perform(get("/api/users/" + user2Id))
                .andExpect(status().isOk());

        // UPDATE user2: username vasya456 -> vasya123 (409 conflict)
        String conflict = """
                {
                    "username": "vasya123",
                    "displayName": "TryConflict"
                }
                """;

        mockMvc.perform(
                        put("/api/users/" + user2Id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(conflict)
                )
                .andExpect(status().isConflict());

        // UPDATE user2: username vasya456 -> vasya789, displayName Vasya2 -> Not Vasya (200 ok)
        String update = """
                {
                    "username": "vasya789",
                    "displayName": "Not Vasya"
                }
                """;

        mockMvc.perform(
                        put("/api/users/" + user2Id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(update)
                )
                .andExpect(status().isOk());

        // UPDATE non-existing user (404 not found)
        mockMvc.perform(
                        put("/api/users/999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(update)
                )
                .andExpect(status().isNotFound());

        // DELETE user1 (204 no content)
        mockMvc.perform(delete("/api/users/" + user1Id))
                .andExpect(status().isNoContent());

        // DELETE user2 (204 no content)
        mockMvc.perform(delete("/api/users/" + user2Id))
                .andExpect(status().isNoContent());

        // DELETE non-existing user (404 not found)
        mockMvc.perform(delete("/api/users/" + user2Id))
                .andExpect(status().isNotFound());
    }
}
