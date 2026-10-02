package com.example.task_manager.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.MemberIdsDto;
import com.example.task_manager.dtos.input.UpdateProjectDto;
import com.example.task_manager.dtos.output.ProjectResponseDto;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.services.ProjectService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@RestController
@AllArgsConstructor
@RequestMapping("/projects")
public class ProjectsController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody @Valid @NonNull CreateProjectDto project,
            @NonNull UriComponentsBuilder uriBuilder
    ) {
        ProjectResponseDto projectCreated = new ProjectResponseDto(projectService.saveAndFlush(project));
        URI uri = uriBuilder.path("/projects/{id}").buildAndExpand(projectCreated.getId()).toUri();

        return ResponseEntity.created(uri).body(projectCreated);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ProjectResponseDto> status(@PathVariable int id) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto(projectService.status(id));

        return ResponseEntity.ok(projectResponseDto);
    }

    @PostMapping("/{id}/add-members")
    public ResponseEntity<ProjectResponseDto> addMembers(
            @PathVariable int id,
            @RequestBody @Valid @NonNull MemberIdsDto dto
    ) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto(
                projectService.addMembers(id, dto.member_ids()));

        return ResponseEntity.ok(projectResponseDto);
    }

    @PostMapping("/{id}/remove-members")
    public ResponseEntity<ProjectResponseDto> removeMembers(
            @PathVariable int id,
            @RequestBody @Valid @NonNull MemberIdsDto member_ids
    ) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto(
                projectService.removeMembers(id, member_ids.member_ids()));

        return ResponseEntity.ok(projectResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable int id) {

        ProjectResponseDto project = new ProjectResponseDto(projectService.findById(id));
        project.add(linkTo(methodOn(ProjectsController.class).getAllProjects()).withRel("projects"));

        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {

        List<ProjectResponseDto> projects = convertProjectsToList(projectService.findAllWithRelations());

        if  (!projects.isEmpty()) {
            for (ProjectResponseDto project : projects) {
                int id  = project.getId();
                project.add(linkTo(methodOn(ProjectsController.class).getProjectById(id)).withSelfRel());
            }
        }

        return ResponseEntity.ok(projects);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable int id,
            @Valid @NonNull @RequestBody UpdateProjectDto updateProjectDto
    ) {
        ProjectResponseDto projectUpdated = new ProjectResponseDto(projectService.updateAndFlush(id, updateProjectDto));

        return ResponseEntity.ok(projectUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable int id) {
        projectService.delete(id);

        return ResponseEntity.noContent().build();
    }

    private @NonNull List<ProjectResponseDto> convertProjectsToList (@NonNull List<ProjectModel> projects) {

        return projects.stream().map(ProjectResponseDto::new).toList();
    }
}
