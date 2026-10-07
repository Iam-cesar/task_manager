package com.example.task_manager.services;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.UpdateProjectDto;
import com.example.task_manager.exceptions.ProjectAlreadyExistsException;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.ProjectMemberRepository;
import com.example.task_manager.repositories.ProjectRepository;
import com.example.task_manager.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

	@Transactional
    public ProjectModel saveAndFlush(
		@NonNull final CreateProjectDto aDto
	) throws IllegalArgumentException {

        if (aDto.project_owner_id() == null) {
            throw new IllegalArgumentException("Project owner id must not be null");
        }

        final var user = userRepository.findById(aDto.project_owner_id())
            .orElseThrow(UserNotFoundException::new);

        if (existsByOwnerIdAndName(user.getId(), aDto.name())) {
            throw new ProjectAlreadyExistsException();
        }

        final var projectModel = new ProjectModel(aDto);
        projectModel.setProject_owner(user);

        return projectRepository.saveAndFlush(projectModel);
    }

    @Transactional
    public ProjectModel addMembers(
            final int anId,
            @NonNull final List<Integer> aListOfIds
    ) {
        final var project = findById(anId);

        final List<UserModel> alistOfUsersToAdd = userRepository.findAllById(aListOfIds);

        project.getMembers().addAll(alistOfUsersToAdd);

        return projectRepository.saveAndFlush(project);
    }

    @Transactional
    public ProjectModel removeMembers(
            final int anId,
            @NonNull final List<Integer> aListOfIds
    ) {
        final var project = findById(anId);

        if (!aListOfIds.isEmpty()) {

            projectMemberRepository.deleteByProjectIDAndUserIdsIn(project.getId(), aListOfIds);

            project.getMembers()
                .removeIf(user -> aListOfIds.contains(user.getId()));
        }

        return projectRepository.saveAndFlush(project);
    }

    private @NonNull ProjectModel deactivate(@NonNull final ProjectModel projectModel) {

        projectModel.deactivate();

        return projectRepository.saveAndFlush(projectModel);
    }

    private @NonNull ProjectModel activate(@NonNull final ProjectModel projectModel) {

        projectModel.activate();

        return projectRepository.saveAndFlush(projectModel);
    }

    public ProjectModel status(final int anId) {

        final var project = findById(anId);

        return project.isActive() ? deactivate(project) : activate(project);
    }

    public ProjectModel findById(final Integer anId) {

        return projectRepository.findByIdWithRelations(anId)
            .orElseThrow(ProjectNotFoundException::new);
    }

	public List<ProjectModel> searchByNameAndDescription(@NonNull final String aName) {
		List<Integer> projectIds = projectRepository.searchIdsByNameAndDescription(aName);
		if (projectIds.isEmpty()) {
			return List.of();
		}

		return projectRepository.findAllWithRelationsByIdIn(projectIds);
	}

    public List<ProjectModel> findAllWithRelations() {
        return projectRepository.findAllWithRelations();
    }

    public List<ProjectModel> findAll(final String aSearch) {

	    return  aSearch == null || aSearch.isBlank()
		    ? findAllWithRelations()
		    : searchByNameAndDescription(aSearch);
    }

	@Transactional
    public ProjectModel updateAndFlush(
        final int anId,
        @NonNull final UpdateProjectDto aDto
    ) {
        final var project = findById(anId);

        if (aDto.name() != null && !aDto.name().equals(project.getName())) {

            if (existsByOwnerIdAndName(project.getProject_owner().getId(), aDto.name())) {
                throw new ProjectAlreadyExistsException();
            }

            project.setName(aDto.name());
        }

        if (aDto.description() != null) {

            project.setDescription(aDto.description());
        }

        return projectRepository.saveAndFlush(project);
    }

    public void delete(final int anId) {

        final var project = findById(anId);

        if (project != null) {
            projectRepository.deleteById(project.getId());
        }
    }

    public boolean existsByOwnerIdAndName(final Integer ownerId, final String aName) {
        return projectRepository.existsByProjectOwnerIdAndName(ownerId, aName);
    }
}
