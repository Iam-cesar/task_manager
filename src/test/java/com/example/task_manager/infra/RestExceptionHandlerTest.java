package com.example.task_manager.infra;

import com.example.task_manager.exceptions.LabelNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            .andExpect(jsonPath("$.message").value("Label not found"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void mapsInvalidBodyToStandardValidationError() throws Exception {
        mockMvc.perform(post("/test/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": ""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("name"))
            .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void mapsMalformedBodyToStandardValidationError() throws Exception {
        mockMvc.perform(post("/test/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("body"));
    }

    @Test
    void mapsInvalidPathVariableToStandardValidationError() throws Exception {
        mockMvc.perform(get("/test/labels/not-a-number/type"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("id"));
    }

    @Test
    void mapsConstraintViolationToStandardValidationError() throws Exception {
        mockMvc.perform(get("/test/labels/0/minimum"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("id"))
            .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void mapsUnexpectedErrorsWithoutExposingInternalDetails() throws Exception {
        mockMvc.perform(get("/test/error"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value("500 INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @RestController
    static class LabelLookupController {
        @GetMapping("/test/labels/{id}")
        void getMissingLabel() {
            throw new LabelNotFoundException();
        }

        @GetMapping("/test/labels/{id}/type")
        void getLabelWithInvalidId(@PathVariable int id) {
        }

        @GetMapping("/test/labels/{id}/minimum")
        void getLabelWithOutOfRangeId(@PathVariable @Min(1) int id) {
        }

        @GetMapping("/test/error")
        void throwUnexpectedError() {
            throw new IllegalStateException("internal detail");
        }

        @PostMapping("/test/labels")
        void createLabel(@Valid @RequestBody InvalidLabelRequest request) {
        }
    }

    record InvalidLabelRequest(@NotBlank String name) {
    }
}
