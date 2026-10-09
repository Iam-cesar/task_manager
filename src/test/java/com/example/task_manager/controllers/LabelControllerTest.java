package com.example.task_manager.controllers;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.services.LabelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LabelController.class)
class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabelService labelService;

    @Test
    void returnsRequestedPageOfLabels() throws Exception {
        var label = new LabelModel(new CreateLabelDto("Urgent", "#FF0000"));
        ReflectionTestUtils.setField(label, "id", 1);
        var pageable = PageRequest.of(1, 2);
        when(labelService.findAll("urgent", pageable))
            .thenReturn(new PageImpl<>(List.of(label), pageable, 5));

        mockMvc.perform(get("/labels")
                .param("search", "urgent")
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.page.totalElements").value(5))
            .andExpect(jsonPath("$.page.totalPages").value(3));

        verify(labelService).findAll(eq("urgent"), eq(pageable));
    }
}
