package kr.ac.se.issuemanager.repository;

import kr.ac.se.issuemanager.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    List<Project> findAll();

    Optional<Project> findById(String id);

    Optional<Project> findByName(String name);

    Project save(Project project);

    void saveAll(List<Project> projects);
}

