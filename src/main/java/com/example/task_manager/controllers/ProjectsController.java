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

    @PostMapping()
    @Transactional
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody @Valid @NonNull CreateProjectDto project,
            @NonNull UriComponentsBuilder uriBuilder
    ) {
        ProjectResponseDto projectCreated = new ProjectResponseDto(projectService.saveAndFlush(project));
        URI uri = uriBuilder.path("/projects/{id}").buildAndExpand(projectCreated.getId()).toUri();

        return ResponseEntity.created(uri).body(projectCreated);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable int id) {

        ProjectResponseDto project = new ProjectResponseDto(projectService.findById(id));
        project.add(linkTo(methodOn(ProjectsController.class).getAllProjects()).withSelfRel());

        return ResponseEntity.ok(project);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {

        List<ProjectResponseDto> projects = convertProjectsToList(projectService.findAll());

        if  (!projects.isEmpty()) {
            for (ProjectResponseDto project : projects) {
                int id  = project.getId();
                project.add(linkTo(methodOn(ProjectsController.class).getProjectById(id)).withSelfRel());
            }
        }

        return ResponseEntity.ok(projects);
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable int id,
            @Valid @NonNull @RequestBody UpdateProjectDto updateProjectDto
    ) {
        ProjectResponseDto projectUpdated = new ProjectResponseDto(projectService.updateAndFlush(id, updateProjectDto));

        return ResponseEntity.ok(projectUpdated);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteProject(@PathVariable int id) {

        projectService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private @NonNull List<ProjectResponseDto> convertProjectsToList (@NonNull List<ProjectModel> projects) {

        return projects.stream().map(ProjectResponseDto::new).toList();
    }
}
