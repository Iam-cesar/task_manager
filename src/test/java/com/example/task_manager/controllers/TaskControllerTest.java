package com.example.task_manager.controllers;

import com.example.task_manager.dtos.output.TaskStatusCountDto;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void passesOverdueFilterToTaskService() throws Exception {
        var pageable = PageRequest.of(0, 10);
        when(taskService.findAll(null, true, pageable))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/tasks")
                .param("overdue", "true"))
            .andExpect(status().isOk());

        verify(taskService).findAll(null, true, pageable);
        verify(taskService, never()).findAll(null, false, pageable);
    }

    @Test
    void capsRequestedPageSizeAtConfiguredMaximum() throws Exception {
        var pageable = PageRequest.of(0, 100);
        when(taskService.findAll(null, false, pageable))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/tasks").param("size", "1000"))
            .andExpect(status().isOk());

        verify(taskService).findAll(null, false, pageable);
    }

    @Test
    void returnsStatusSummary() throws Exception {
        when(taskService.summarizeByStatus())
            .thenReturn(List.of(new TaskStatusCountDto(TaskStatusEnum.PENDING, 3)));

        mockMvc.perform(get("/tasks/summary/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("pending"))
            .andExpect(jsonPath("$[0].count").value(3));
    }

    @Test
    void reportsPastDueDateAsAFieldValidationError() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Past deadline",
                      "description": "Invalid due date",
                      "project_id": 1,
                      "user_id": 1,
                      "due_date": "2000-01-01T00:00:00Z"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("due_date"));

        verify(taskService, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
}
