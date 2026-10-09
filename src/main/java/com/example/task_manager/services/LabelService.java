package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.UpdateLabelDto;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.repositories.LabelRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelService {

	private final LabelRepository labelRepository;

	@Transactional
	public LabelModel saveAndFlush(CreateLabelDto aDto) {

		final var label = new LabelModel(aDto);

		return labelRepository.saveAndFlush(label);
	}

	public List<LabelModel> findAll(final String search) {

		return search == null || search.isBlank()
			? labelRepository.findAllWithTasks()
			: findByName(search);
	}

	public Page<LabelModel> findAll(final String search, final Pageable pageable) {
		final var idsPage = search == null || search.isBlank()
			? labelRepository.findAllIds(pageable)
			: labelRepository.searchIdsByName(search, pageable);

		if (idsPage.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, idsPage.getTotalElements());
		}

		final var labelsById = labelRepository.findAllWithTasksByIdIn(idsPage.getContent())
			.stream()
			.collect(Collectors.toMap(LabelModel::getId, Function.identity()));

		final var labelsInPageOrder = idsPage.getContent().stream()
			.map(labelsById::get)
			.toList();

		return new PageImpl<>(labelsInPageOrder, pageable, idsPage.getTotalElements());
	}

	public List<LabelModel> findByName(@NonNull final String aName) {

		List<Integer> labelIds = labelRepository.searchIdsByName(aName);

		if (labelIds.isEmpty()) {
			return List.of();
		}

		return labelRepository.findAllWithTasksByIdIn(labelIds);
	}

	public LabelModel findById(final int anId) {
		return labelRepository.findByIdWithTasks(anId)
			.orElseThrow(LabelNotFoundException::new);
	}

	@Transactional
	public LabelModel updateAndFlush(
		final int anId,
		@NonNull final UpdateLabelDto aDto
	) {
		final var label = findById(anId);

		BeanUtils.copyProperties(aDto, label, "id");

		return labelRepository.saveAndFlush(label);
	}

	public void deleteById(final int anId) {
		var label = findById(anId);

		if (label != null) {
			labelRepository.deleteById(label.getId());
		}
	}
}
