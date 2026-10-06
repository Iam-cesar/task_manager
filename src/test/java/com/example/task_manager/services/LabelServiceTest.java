package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.UpdateLabelDto;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.repositories.LabelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private LabelService labelService;

    @Test
    void savesLabelFromCreateDto() {
        var dto = new CreateLabelDto("Urgent", "#FF0000");
        when(labelRepository.saveAndFlush(any(LabelModel.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var result = labelService.saveAndFlush(dto);

        assertEquals("Urgent", result.getName());
        assertEquals("#FF0000", result.getColor());
        verify(labelRepository).saveAndFlush(any(LabelModel.class));
    }

    @Test
    void findsLabelsByFullTextQuery() {
        var labels = List.of(new LabelModel(new CreateLabelDto("Urgent", "#FF0000")));
        when(labelRepository.findByName("urgent")).thenReturn(labels);

        assertSame(labels, labelService.findByName("urgent"));
    }

    @Test
    void updatesExistingLabel() {
        var label = new LabelModel(new CreateLabelDto("Old", "#111111"));
        ReflectionTestUtils.setField(label, "id", 7);
        when(labelRepository.findById(7)).thenReturn(Optional.of(label));
        when(labelRepository.saveAndFlush(label)).thenReturn(label);

        var result = labelService.updateAndFlush(new UpdateLabelDto("New", "#222222"), 7);

        assertSame(label, result);
        assertEquals("New", result.getName());
        assertEquals("#222222", result.getColor());
        verify(labelRepository).saveAndFlush(label);
    }

    @Test
    void throwsWhenUpdatingMissingLabel() {
        when(labelRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(
            LabelNotFoundException.class,
            () -> labelService.updateAndFlush(new UpdateLabelDto("New", "#222222"), 404)
        );
        verify(labelRepository, never()).saveAndFlush(any(LabelModel.class));
    }

    @Test
    void deletesExistingLabel() {
        var label = new LabelModel(new CreateLabelDto("Old", "#111111"));
        when(labelRepository.findById(7)).thenReturn(Optional.of(label));

        labelService.deleteById(7);

        verify(labelRepository).deleteById(7);
    }

    @Test
    void throwsWhenDeletingMissingLabel() {
        when(labelRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(LabelNotFoundException.class, () -> labelService.deleteById(404));
        verify(labelRepository, never()).deleteById(404);
    }
}
