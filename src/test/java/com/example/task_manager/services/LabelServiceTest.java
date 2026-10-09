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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    void findsAllWithoutSearchFilter() {
        var labels = List.of(new LabelModel(new CreateLabelDto("Urgent", "#FF0000")));
        when(labelRepository.findAllWithTasks()).thenReturn(labels);

        assertSame(labels, labelService.findAll(null));
        assertSame(labels, labelService.findAll("   "));
        verify(labelRepository, times(2)).findAllWithTasks();
    }

    @Test
    void findsPageOfLabelsWithoutSearchFilter() {
        var urgent = new LabelModel(new CreateLabelDto("Urgent", "#FF0000"));
        var lowPriority = new LabelModel(new CreateLabelDto("Low", "#00FF00"));
        ReflectionTestUtils.setField(urgent, "id", 1);
        ReflectionTestUtils.setField(lowPriority, "id", 2);
        Page<Number> page = new PageImpl<>(List.of(1, 2), PageRequest.of(0, 2), 3);
        when(labelRepository.findAllIds(page.getPageable())).thenReturn(page);
        when(labelRepository.findAllWithTasksByIdIn(List.of(1, 2)))
            .thenReturn(List.of(lowPriority, urgent));

        var result = labelService.findAll(null, page.getPageable());

        assertEquals(List.of(urgent, lowPriority), result.getContent());
        assertEquals(3, result.getTotalElements());
        verify(labelRepository).findAllIds(page.getPageable());
    }

    @Test
    void findsPageOfLabelsUsingSearchFilter() {
        var urgent = new LabelModel(new CreateLabelDto("Urgent", "#FF0000"));
        ReflectionTestUtils.setField(urgent, "id", 1);
        var pageable = PageRequest.of(0, 2);
        when(labelRepository.searchIdsByName("urgent", pageable))
            .thenReturn(new PageImpl<>(List.of(1L), pageable, 1));
        when(labelRepository.findAllWithTasksByIdIn(List.of(1))).thenReturn(List.of(urgent));

        var result = labelService.findAll("urgent", pageable);

        assertEquals(List.of(urgent), result.getContent());
        assertEquals(1, result.getTotalElements());
        verify(labelRepository).searchIdsByName("urgent", pageable);
    }

    @Test
    void findsAllWithSearchFilter() {
        var labels = List.of(new LabelModel(new CreateLabelDto("Urgent", "#FF0000")));
        when(labelRepository.searchIdsByName("urgent")).thenReturn(List.of(1L));
        when(labelRepository.findAllWithTasksByIdIn(List.of(1))).thenReturn(labels);

        assertSame(labels, labelService.findAll("urgent"));
    }

    @Test
    void findsLabelsByFullTextQuery() {
        var labels = List.of(new LabelModel(new CreateLabelDto("Urgent", "#FF0000")));
        when(labelRepository.searchIdsByName("urgent")).thenReturn(List.of(1));
        when(labelRepository.findAllWithTasksByIdIn(List.of(1))).thenReturn(labels);

        assertSame(labels, labelService.findByName("urgent"));
    }

    @Test
    void returnsEmptyListWhenSearchFindsNoIds() {
        when(labelRepository.searchIdsByName("unknown")).thenReturn(List.of());

        var result = labelService.findByName("unknown");

        assertTrue(result.isEmpty());
        verify(labelRepository, never()).findAllWithTasksByIdIn(any());
    }

    @Test
    void findsLabelById() {
        var label = new LabelModel(new CreateLabelDto("Urgent", "#FF0000"));
        when(labelRepository.findByIdWithTasks(1)).thenReturn(Optional.of(label));

        var result = labelService.findById(1);

        assertSame(label, result);
    }

    @Test
    void throwsWhenFindingMissingLabelById() {
        when(labelRepository.findByIdWithTasks(404)).thenReturn(Optional.empty());

        assertThrows(LabelNotFoundException.class, () -> labelService.findById(404));
    }

    @Test
    void updatesExistingLabel() {
        var label = new LabelModel(new CreateLabelDto("Old", "#111111"));
        ReflectionTestUtils.setField(label, "id", 7);
        when(labelRepository.findByIdWithTasks(7)).thenReturn(Optional.of(label));
        when(labelRepository.saveAndFlush(label)).thenReturn(label);

        var result = labelService.updateAndFlush(7, new UpdateLabelDto("New", "#222222"));

        assertSame(label, result);
        assertEquals("New", result.getName());
        assertEquals("#222222", result.getColor());
        verify(labelRepository).saveAndFlush(label);
    }

    @Test
    void throwsWhenUpdatingMissingLabel() {
        when(labelRepository.findByIdWithTasks(404)).thenReturn(Optional.empty());

        assertThrows(
            LabelNotFoundException.class,
            () -> labelService.updateAndFlush(404, new UpdateLabelDto("New", "#222222"))
        );
        verify(labelRepository, never()).saveAndFlush(any(LabelModel.class));
    }

    @Test
    void deletesExistingLabel() {
        var label = new LabelModel(new CreateLabelDto("Old", "#111111"));
        ReflectionTestUtils.setField(label, "id", 7);
        when(labelRepository.findByIdWithTasks(7)).thenReturn(Optional.of(label));

        labelService.deleteById(7);

        verify(labelRepository).deleteById(7);
    }

    @Test
    void throwsWhenDeletingMissingLabel() {
        when(labelRepository.findByIdWithTasks(404)).thenReturn(Optional.empty());

        assertThrows(LabelNotFoundException.class, () -> labelService.deleteById(404));
        verify(labelRepository, never()).deleteById(404);
    }
}
