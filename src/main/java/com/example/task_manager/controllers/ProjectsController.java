package com.example.task_manager.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;

import com.example.task_manager.helpers.ConvertRepresentationModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.MemberIdsDto;
import com.example.task_manager.dtos.input.UpdateProjectDto;
import com.example.task_manager.dtos.output.ProjectResponseDto;
import com.example.task_manager.services.ProjectService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@RestController
@AllArgsConstructor
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Manage projects and their members")
public class ProjectsController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody @Valid @NonNull final CreateProjectDto aDto,
            @NonNull final UriComponentsBuilder uriBuilder
    ) {
        final var projectCreated = new ProjectResponseDto(projectService.saveAndFlush(aDto));
        
        URI uri = uriBuilder.path("/projects/{id}")
	        .buildAndExpand(projectCreated.getId()).toUri();

        return ResponseEntity.created(uri).body(projectCreated);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ProjectResponseDto> status(@PathVariable final int id) {
        final var projectResponseDto = new ProjectResponseDto(projectService.status(id));

        return ResponseEntity.ok(projectResponseDto);
    }

    @PostMapping("/{id}/add-members")
    public ResponseEntity<ProjectResponseDto> addMembers(
        @PathVariable final int id,
        @RequestBody @Valid @NonNull MemberIdsDto aDto
    ) {
        final var projectResponseDto = new ProjectResponseDto(
            projectService.addMembers(id, aDto.member_ids()));

        return ResponseEntity.ok(projectResponseDto);
    }

    @PostMapping("/{id}/remove-members")
    public ResponseEntity<ProjectResponseDto> removeMembers(
        @PathVariable final int id,
        @RequestBody @Valid @NonNull MemberIdsDto aListOfMemberIds
    ) {
        final var projectResponseDto = new ProjectResponseDto(
            projectService.removeMembers(id, aListOfMemberIds.member_ids()));

        return ResponseEntity.ok(projectResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable final int id) {

        final var project = new ProjectResponseDto(projectService.findById(id));

        project.add(linkTo(methodOn(ProjectsController.class)
			.getAllProjects(null, null)).withRel("projects"));

        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<PagedModel<ProjectResponseDto>> getAllProjects(
	    @RequestParam(required = false) String search,
	    @PageableDefault(size = 10) final Pageable pageable
    ) {
        final var projects = ConvertRepresentationModel.toPageList(
            projectService.findAll(search, pageable),
            ProjectResponseDto::new
        );

        if (!projects.isEmpty()) {
            for (ProjectResponseDto project : projects.getContent()) {
                int id  = project.getId();
                project.add(linkTo(methodOn(ProjectsController.class).getProjectById(id)).withSelfRel());
            }
        }

        return ResponseEntity.ok(new PagedModel<>(projects));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
        @PathVariable final int id,
        @Valid @NonNull @RequestBody UpdateProjectDto aDto
    ) {
        final var projectUpdated = new ProjectResponseDto(projectService.updateAndFlush(id, aDto));

        return ResponseEntity.ok(projectUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable final int id) {

        projectService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
