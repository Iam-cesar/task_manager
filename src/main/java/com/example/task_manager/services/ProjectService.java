package com.example.task_manager.services;

import com.example.task_manager.dtos.CreateProjectDto;
import com.example.task_manager.dtos.UpdateProjectDto;
import com.example.task_manager.exceptions.ProjectAlreadyExistsException;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.ProjectMemberModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.ProjectMemberRepository;
import com.example.task_manager.repositories.ProjectRepository;
import com.example.task_manager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectModel saveAndFlush(@NonNull CreateProjectDto createProjectDto) throws IllegalArgumentException {

        if (createProjectDto.getProject_owner_id() == null) {
            throw new IllegalArgumentException("Project owner id must not be null");
        }

        UserModel user = userRepository.findById(createProjectDto.getProject_owner_id()).orElseThrow(UserNotFoundException::new);

        if (existsByOwnerIdAndName(user.getId(), createProjectDto.getName())) {
            throw new ProjectAlreadyExistsException();
        }

        ProjectModel projectModel = new ProjectModel(createProjectDto);
        projectModel.setProject_owner(user);

        return projectRepository.saveAndFlush(projectModel);
    }

    public ProjectModel addMembers(int id, @NonNull List<Integer> memberIds) {

        ProjectModel projectById = findById(id);
        ProjectModel projectRef = projectRepository.getReferenceById(projectById.getId());

        for (Integer memberId : memberIds) {
            UserModel userRef = userRepository.getReferenceById(memberId);

            ProjectMemberModel member = new ProjectMemberModel(projectRef, userRef);
            projectMemberRepository.save(member);
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
        return projectRepository.findById(id).orElseThrow(ProjectNotFoundException::new);
    }

    public List<ProjectModel> findAllWithRelations() {
        return projectRepository.findALlWithRelations();
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
