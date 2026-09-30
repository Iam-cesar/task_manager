package com.example.task_manager.controllers;

import com.example.task_manager.dtos.CreateProjectDto;
import com.example.task_manager.dtos.ProjectResponseDto;
import com.example.task_manager.dtos.UpdateProjectDto;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.services.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    public ResponseEntity<ProjectResponseDto> addMembers(@PathVariable int id, List<Integer> member_ids) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto(projectService.addMembers(id, member_ids));

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
