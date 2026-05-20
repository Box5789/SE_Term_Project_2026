package kr.ac.se.issuemanager.repository;

import kr.ac.se.issuemanager.model.Issue;

import java.util.List;
import java.util.Optional;

public interface IssueRepository {
    List<Issue> findAll();

    List<Issue> findByProject(String projectId);

    Optional<Issue> findById(String id);

    Issue save(Issue issue);

    void saveAll(List<Issue> issues);
}

