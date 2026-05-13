package com.example.rualingo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRegistrationSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void studentRegistration_allowsAnonymousPost() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "username": "student1",
                                  "email": "student1@example.com",
                                  "password": "Password123!",
                                  "first_name": "John",
                                  "second_name": "Doe",
                                  "gender": "Male",
                                  "date_of_birth": "1990-01-01",
                                  "province_of_origin": "Western Cape",
                                  "role": "Student"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void studentRegistration_mapsLegacyStudentRoleToUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "username": "student2",
                                  "email": "student2@example.com",
                                  "password": "Password123!",
                                  "first_name": "Jane",
                                  "second_name": "Smith",
                                  "gender": "Female",
                                  "date_of_birth": "1992-05-15",
                                  "province_of_origin": "Gauteng",
                                  "role": "Student"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void adminRegistration_allowsUpToTenAdmins() throws Exception {
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    """
                                    {
                                      "username": "admin%s",
                                      "email": "admin%s@example.com",
                                      "password": "Password123!",
                                      "first_name": "Admin%s",
                                      "second_name": "User%s",
                                      "gender": "Male",
                                      "date_of_birth": "1985-01-01",
                                      "province_of_origin": "Western Cape",
                                      "role": "Admin"
                                    }
                                    """
                                            .formatted(i, i, i, i)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "username": "admin11",
                                  "email": "admin11@example.com",
                                  "password": "Password123!",
                                  "first_name": "Admin11",
                                  "second_name": "User11",
                                  "gender": "Male",
                                  "date_of_birth": "1985-01-01",
                                  "province_of_origin": "Western Cape",
                                  "role": "Admin"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationEndpoint_allowsCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/auth/register")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                // If CORS is configured, Spring should answer preflight without a 403.
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
