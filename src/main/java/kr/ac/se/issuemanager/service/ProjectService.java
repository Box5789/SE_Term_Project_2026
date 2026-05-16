package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.repository.ProjectRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(String name, String description) {
        UserService.requireText(name, "프로젝트 이름");
        projectRepository.findByName(name).ifPresent(project -> {
            throw new ServiceException("이미 존재하는 프로젝트입니다: " + name);
        });
        Project project = new Project(
                IdGenerator.nextId("PROJECT", projectRepository.findAll(), Project::getId),
                name,
                description == null ? "" : description,
                LocalDateTime.now()
        );
        return projectRepository.save(project);
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }

    public Project findById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ServiceException("프로젝트를 찾을 수 없습니다: " + id));
    }
}

