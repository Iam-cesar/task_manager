package com.example.task_manager.infra;

import com.example.task_manager.exceptions.LabelNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class RestExceptionHandlerTest {

    private final MockMvc mockMvc = standaloneSetup(new LabelLookupController())
        .setControllerAdvice(new RestExceptionHandler())
        .build();

    @Test
    void mapsMissingLabelToNotFound() throws Exception {
        mockMvc.perform(get("/test/labels/404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Label not found"));
    }

    @RestController
    static class LabelLookupController {
        @GetMapping("/test/labels/{id}")
        void getMissingLabel() {
            throw new LabelNotFoundException();
        }
    }
}
