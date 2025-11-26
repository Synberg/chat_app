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

        // 1. GET non-existing (404 not found)
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound());

        // 2. CREATE user1 (201 created)
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

        int id1 = JsonPath.parse(res1.getResponse().getContentAsString()).read("$.id", Integer.class);

        // 3. GET user1 (200 ok)
        mockMvc.perform(get("/api/users/" + id1))
                .andExpect(status().isOk());

        // 4. CREATE user2 (201 created)
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

        int id2 = JsonPath.parse(res2.getResponse().getContentAsString()).read("$.id", Integer.class);

        // 5. GET user2 (200 ok)
        mockMvc.perform(get("/api/users/" + id2))
                .andExpect(status().isOk());

        // 6. UPDATE user2: username vasya456 -> vasya123 (409 conflict)
        String conflict = """
                {
                    "username": "vasya123",
                    "displayName": "TryConflict"
                }
                """;

        mockMvc.perform(
                        put("/api/users/" + id2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(conflict)
                )
                .andExpect(status().isConflict());

        // 7. UPDATE user2: username vasya456 -> vasya789, displayName Vasya2 -> Not Vasya (200 ok)
        String update = """
                {
                    "username": "vasya789",
                    "displayName": "Not Vasya"
                }
                """;

        mockMvc.perform(
                        put("/api/users/" + id2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(update)
                )
                .andExpect(status().isOk());

        // 8. UPDATE non-existing user (404 not found)
        mockMvc.perform(
                        put("/api/users/999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(update)
                )
                .andExpect(status().isNotFound());

        // 9. DELETE user1 (200 ok)
        mockMvc.perform(delete("/api/users/" + id1))
                .andExpect(status().isOk());

        // 10. DELETE user2 (200 ok)
        mockMvc.perform(delete("/api/users/" + id2))
                .andExpect(status().isOk());

        // 11. DELETE non-existing user (404 not found)
        mockMvc.perform(delete("/api/users/999999"))
                .andExpect(status().isNotFound());
    }
}
