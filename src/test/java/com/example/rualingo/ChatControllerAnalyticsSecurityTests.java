package com.example.rualingo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.rualingo.model.User;
import com.example.rualingo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
// Fresh context avoids inheriting leftover entity state from other test classes sharing the cached context.
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ChatControllerAnalyticsSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void askEndpoint_allowsAnonymousPost() throws Exception {
        mockMvc.perform(post("/api/chat/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "userQuery": "languages"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
    }

    @Test
    void analyticsEndpoint_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/chat/analytics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "USER")
    void analyticsEndpoint_returnsAnalyticsForAuthenticatedUser() throws Exception {
        User user = new User();
        user.setEmail("student@example.com");
        user.setUsername("student");
        user.setActive(true);
        userRepository.save(user);

        mockMvc.perform(get("/api/chat/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.totalActivities").isNumber())
                .andExpect(jsonPath("$.totalChats").isNumber());
    }
}

