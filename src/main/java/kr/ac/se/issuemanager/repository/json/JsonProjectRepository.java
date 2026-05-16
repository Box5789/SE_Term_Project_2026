package kr.ac.se.issuemanager.repository.json;

import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.repository.ProjectRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JsonProjectRepository implements ProjectRepository {
    private final JsonListStore<Project> store;
    private final List<Project> projects;

    public JsonProjectRepository(Path dataDirectory) {
        this.store = new JsonListStore<>(dataDirectory.resolve("projects.json"), Project.class);
        this.projects = store.load();
    }

    @Override
    public List<Project> findAll() {
        return projects.stream()
                .sorted(Comparator.comparing(Project::getName))
                .toList();
    }

    @Override
    public Optional<Project> findById(String id) {
        return projects.stream().filter(project -> project.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Project> findByName(String name) {
        return projects.stream().filter(project -> project.getName().equals(name)).findFirst();
    }

    @Override
    public Project save(Project project) {
        projects.removeIf(existing -> existing.getId().equals(project.getId()));
        projects.add(project);
        store.save(projects);
        return project;
    }

    @Override
    public void saveAll(List<Project> projects) {
        this.projects.clear();
        this.projects.addAll(new ArrayList<>(projects));
        store.save(this.projects);
    }
}

