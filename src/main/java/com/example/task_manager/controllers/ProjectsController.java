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
            @RequestBody @Valid @NonNull final CreateProjectDto aDto,
            @NonNull UriComponentsBuilder uriBuilder
    ) {
        var projectCreated = new ProjectResponseDto(projectService.saveAndFlush(aDto));
        
        URI uri = uriBuilder.path("/projects/{id}").buildAndExpand(projectCreated.getId()).toUri();

        return ResponseEntity.created(uri).body(projectCreated);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ProjectResponseDto> status(@PathVariable int anId) {
        var projectResponseDto = new ProjectResponseDto(projectService.status(anId));

        return ResponseEntity.ok(projectResponseDto);
    }

    @PostMapping("/{id}/add-members")
    public ResponseEntity<ProjectResponseDto> addMembers(
        @PathVariable final int anId,
        @RequestBody @Valid @NonNull MemberIdsDto aDto
    ) {
        var projectResponseDto = new ProjectResponseDto(
            projectService.addMembers(anId, aDto.member_ids()));

        return ResponseEntity.ok(projectResponseDto);
    }

    @PostMapping("/{id}/remove-members")
    public ResponseEntity<ProjectResponseDto> removeMembers(
        @PathVariable final int anId,
        @RequestBody @Valid @NonNull MemberIdsDto aListOfMemberIds
    ) {
        var projectResponseDto = new ProjectResponseDto(
            projectService.removeMembers(anId, aListOfMemberIds.member_ids()));

        return ResponseEntity.ok(projectResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable int anId) {

        var project = new ProjectResponseDto(projectService.findById(anId));

        project.add(linkTo(methodOn(ProjectsController.class)
            .getAllProjects()).withRel("projects"));

        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {

        List<ProjectResponseDto> projects = convertProjectsToList(projectService.findAllWithRelations());

        if  (!projects.isEmpty()) {
            for (ProjectResponseDto project : projects) {
                int id  = project.getId();
                project.add(linkTo(methodOn(ProjectsController.class)
                    .getProjectById(id)).withSelfRel());
            }
        }

        return ResponseEntity.ok(projects);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
        @PathVariable final int id,
        @Valid @NonNull @RequestBody UpdateProjectDto aDto
    ) {
        var projectUpdated = new ProjectResponseDto(projectService.updateAndFlush(id, aDto));

        return ResponseEntity.ok(projectUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable final int id) {
        projectService.delete(id);

        return ResponseEntity.noContent().build();
    }

    private @NonNull List<ProjectResponseDto> convertProjectsToList (@NonNull final List<ProjectModel> aListOfProjectModels) {

        return aListOfProjectModels.stream().map(ProjectResponseDto::new).toList();
    }
}
