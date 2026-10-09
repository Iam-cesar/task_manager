package com.example.task_manager.controllers;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.UpdateLabelDto;
import com.example.task_manager.dtos.output.LabelResponseDto;
import com.example.task_manager.helpers.ConvertRepresentationModel;
import com.example.task_manager.services.LabelService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@AllArgsConstructor
@RequestMapping("/labels")
public class LabelController {

	private final LabelService labelService;

	@PostMapping
	public ResponseEntity<LabelResponseDto> createLabel(
		@RequestBody @Valid final CreateLabelDto aDto,
		@NonNull final UriComponentsBuilder uriBuilder
	) {
		final var createdLabel = new LabelResponseDto(labelService.saveAndFlush(aDto));
		URI uri = uriBuilder.path("/labels/{id}").buildAndExpand(createdLabel.getId()).toUri();

		return ResponseEntity.created(uri).body(createdLabel);
	};

	@GetMapping
	public ResponseEntity<PagedModel<LabelResponseDto>> findAllLabels(
		@RequestParam(required = false) String search,
		@PageableDefault(size = 10) final Pageable pageable
	) {
		final var labels = ConvertRepresentationModel
			.toPageList(labelService.findAll(search, pageable), LabelResponseDto::new);

		if  (!labels.isEmpty()) {
			for (LabelResponseDto label : labels.getContent()) {
				int id  = label.getId();
				label.add(linkTo(methodOn(LabelController.class).findLabelById(id)).withSelfRel());
			}
		}


		return ResponseEntity.ok(new PagedModel<>(labels));
	}

	@GetMapping("/{id}")
	public ResponseEntity<LabelResponseDto> findLabelById(@PathVariable final int id) {

		final var label = new LabelResponseDto(labelService.findById(id), true);

		return ResponseEntity.ok(label);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<LabelResponseDto> updateLabel(
		@PathVariable final int id,
		@RequestBody @Valid final UpdateLabelDto aDto
	) {
		final var updatedLabel = new LabelResponseDto(labelService.updateAndFlush(id, aDto));

		return ResponseEntity.ok(updatedLabel);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<LabelResponseDto> deleteLabel(@PathVariable final int id) {

		labelService.deleteById(id);

		return ResponseEntity.noContent().build();
	}
}
