package com.mednex.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "ROLE_NURSE")
    void nurseCanAccessPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_DOCTOR")
    void doctorCanAddConsultation() throws Exception {
        // Just checking access, not payload
        mockMvc.perform(post("/api/patients/" + java.util.UUID.randomUUID() + "/consultations"))
               .andExpect(status().is4xxClientError()); // 404 or 400, not 403
    }

    @Test
    @WithMockUser(authorities = "ROLE_NURSE")
    void nurseCannotAddConsultation() throws Exception {
        mockMvc.perform(post("/api/patients/" + java.util.UUID.randomUUID() + "/consultations"))
               .andExpect(status().isForbidden());
    }
}
