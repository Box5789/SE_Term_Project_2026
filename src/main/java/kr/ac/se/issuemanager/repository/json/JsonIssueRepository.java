package kr.ac.se.issuemanager.repository.json;

import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.repository.IssueRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JsonIssueRepository implements IssueRepository {
    private final JsonListStore<Issue> store;
    private final List<Issue> issues;

    public JsonIssueRepository(Path dataDirectory) {
        this.store = new JsonListStore<>(dataDirectory.resolve("issues.json"), Issue.class);
        this.issues = store.load();
    }

    @Override
    public List<Issue> findAll() {
        return issues.stream()
                .sorted(Comparator.comparing(Issue::getReportedDate))
                .toList();
    }

    @Override
    public List<Issue> findByProject(String projectId) {
        return findAll().stream()
                .filter(issue -> issue.getProjectId().equals(projectId))
                .toList();
    }

    @Override
    public Optional<Issue> findById(String id) {
        return issues.stream().filter(issue -> issue.getId().equals(id)).findFirst();
    }

    @Override
    public Issue save(Issue issue) {
        issues.removeIf(existing -> existing.getId().equals(issue.getId()));
        issues.add(issue);
        store.save(issues);
        return issue;
    }

    @Override
    public void saveAll(List<Issue> issues) {
        this.issues.clear();
        this.issues.addAll(new ArrayList<>(issues));
        store.save(this.issues);
    }
}

