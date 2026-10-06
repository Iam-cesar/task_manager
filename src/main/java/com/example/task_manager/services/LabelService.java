package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.UpdateLabelDto;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.TaskLabelModel;
import com.example.task_manager.repositories.LabelRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

	private final LabelRepository labelRepository;

	@Transactional
	public LabelModel saveAndFlush(CreateLabelDto aDto) {
		final var label = new LabelModel(aDto);

		return labelRepository.saveAndFlush(label);
	}

	public List<LabelModel> findAll() {
		return labelRepository.findAll();
	}

	public List<LabelModel> findByName(@NonNull final String name) {
		return labelRepository.findByName(name);
	}

	public LabelModel findById(final int id) {
		return labelRepository.findById(id).orElseThrow(LabelNotFoundException::new);
	}

	public LabelModel updateAndFlush(@NonNull final UpdateLabelDto aDto, final int id) {
		final var label = findById(id);

		BeanUtils.copyProperties(aDto, label, "id");

		return labelRepository.saveAndFlush(label);
	}

	public void deleteById(final int id) {
		var label = findById(id);

		if (label != null) {
			labelRepository.deleteById(id);
		}
	}
}
