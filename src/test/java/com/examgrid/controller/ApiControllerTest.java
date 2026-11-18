package com.examgrid.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seatLookupIsPublic() throws Exception {
        mockMvc.perform(post("/api/allocations/seating").with(httpBasic("admin", "admin123"))
                        .param("date", "2026-12-07").param("startTime", "09:30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentsSeated").value(148));

        mockMvc.perform(get("/api/seats/NNM24CS224"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usn").value("NNM24CS224"))
                .andExpect(jsonPath("$[0].subjectCode").value("CS3603"));
    }

    @Test
    void unknownUsnReturns404() throws Exception {
        mockMvc.perform(get("/api/seats/XYZ123456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found: XYZ123456"));
    }

    @Test
    void adminApisNeedLogin() throws Exception {
        mockMvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/students").with(httpBasic("NNM24CS224", "Welcome@123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/students").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void invalidStudentIsRejected() throws Exception {
        String body = "{\"usn\":\"12\",\"name\":\"\",\"branch\":\"CSE\",\"semester\":11,\"email\":\"bad\"}";
        mockMvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.usn").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.semester").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void facultyCanOnlySeeOwnRoster() throws Exception {
        mockMvc.perform(get("/api/duties/faculty/FAC101").with(httpBasic("FAC101", "Welcome@123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/duties/faculty/FAC102").with(httpBasic("FAC101", "Welcome@123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invigilationBeforeSeatingGives422() throws Exception {
        mockMvc.perform(post("/api/allocations/invigilation").with(httpBasic("admin", "admin123"))
                        .param("date", "2026-12-08").param("startTime", "14:00"))
                .andExpect(status().isUnprocessableEntity());
    }
}
