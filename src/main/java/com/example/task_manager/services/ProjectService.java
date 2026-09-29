package com.example.task_manager.services;

import com.example.task_manager.dtos.CreateProjectDto;
import com.example.task_manager.dtos.UpdateProjectDto;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectModel saveAndFlush(CreateProjectDto createProjectDto) throws IllegalArgumentException{

        ProjectModel projectModel = new ProjectModel(createProjectDto);

        if (createProjectDto.getProject_owner_id() == null) {
            throw new IllegalArgumentException("Project owner id must not be null");
        }

        UserModel user = userService.findById(createProjectDto.getProject_owner_id());
        projectModel.setProject_owner(user);

        return projectRepository.saveAndFlush(projectModel);
    }

    public ProjectModel findById(Integer id) {
        return projectRepository.findById(id).orElseThrow(ProjectNotFoundException::new);
    }

    public List<ProjectModel> findAll() {
        return projectRepository.findAll();
    }

    public ProjectModel updateAndFlush(int id, UpdateProjectDto updateProjectDto) {
        ProjectModel projectById = findById(id);

        BeanUtils.copyProperties(updateProjectDto, projectById);

        return projectRepository.saveAndFlush(projectById);
    }

    public void delete(int id) {

        ProjectModel projectById = findById(id);

        if  (projectById != null) {
            projectRepository.deleteById(id);
        }
    }
}
