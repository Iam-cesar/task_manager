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

    public ProjectModel saveAndFlush(@NonNull CreateProjectDto dto) throws IllegalArgumentException {

        if (dto.getProject_owner_id() == null) {
            throw new IllegalArgumentException("Project owner id must not be null");
        }

        UserModel user = userRepository.findById(dto.getProject_owner_id()).orElseThrow(UserNotFoundException::new);

        if (existsByOwnerIdAndName(user.getId(), dto.getName())) {
            throw new ProjectAlreadyExistsException();
        }

        ProjectModel projectModel = new ProjectModel(dto);
        projectModel.setProject_owner(user);

        return projectRepository.saveAndFlush(projectModel);
    }

    @Transactional
    public ProjectModel addMembers(
            int id,
            @NonNull List<Integer> ids
    ) {

        ProjectModel projectById = findById(id);

        List<UserModel> usersToAdd = userRepository.findAllById(ids);

        projectById.getMembers().addAll(usersToAdd);

        return projectRepository.saveAndFlush(projectById);
    }

    @Transactional
    public ProjectModel removeMembers(
            int id,
            @NonNull List<Integer> ids
    ) {

        ProjectModel projectById = findById(id);

        if (!ids.isEmpty()) {

            projectMemberRepository.deleteByProjectIDAndUserIdsIn(projectById.getId(), ids);

            projectById.getMembers().removeIf(user -> ids.contains(user.getId()));
        }

        return projectRepository.saveAndFlush(projectById);
    }

    private @NonNull ProjectModel deactivate(@NonNull ProjectModel projectModel) {

        projectModel.deactivate();

        return projectRepository.saveAndFlush(projectModel);
    }

    private @NonNull ProjectModel activate(@NonNull ProjectModel projectModel) {

        projectModel.activate();

        return projectRepository.saveAndFlush(projectModel);
    }

    public ProjectModel status(int id) {

        ProjectModel projectById = findById(id);

        return projectById.isActive() ? deactivate(projectById) : activate(projectById);
    }

    public ProjectModel findById(Integer id) {
        return projectRepository.findByIdWithRelations(id)
                .orElseThrow(ProjectNotFoundException::new);
    }

    public List<ProjectModel> findAllWithRelations() {
        return projectRepository.findAllWithRelations();
    }

    public List<ProjectModel> findAll() {
        return projectRepository.findAll();
    }

    public ProjectModel updateAndFlush(int id, @NonNull UpdateProjectDto updateProjectDto) {

        ProjectModel projectById = findById(id);

        if (updateProjectDto.name() != null && !updateProjectDto.name().equals(projectById.getName())) {

            if (existsByOwnerIdAndName(projectById.getProject_owner().getId(), updateProjectDto.name())) {
                throw new ProjectAlreadyExistsException();
            }

            projectById.setName(updateProjectDto.name());
        }

        if (updateProjectDto.description() != null) {

            projectById.setDescription(updateProjectDto.description());
        }

        return projectRepository.saveAndFlush(projectById);
    }

    public void delete(int id) {

        ProjectModel projectById = findById(id);

        if (projectById != null) {
            projectRepository.deleteById(id);
        }
    }

    public boolean existsByOwnerIdAndName(Integer ownerId, String name) {
        return projectRepository.existsByProjectOwnerIdAndName(ownerId, name);
    }
}
