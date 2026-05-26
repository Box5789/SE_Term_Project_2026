package kr.ac.se.issuemanager.repository;

import kr.ac.se.issuemanager.ServiceFixture;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;
import kr.ac.se.issuemanager.repository.json.JsonIssueRepository;
import kr.ac.se.issuemanager.repository.json.JsonProjectRepository;
import kr.ac.se.issuemanager.repository.json.JsonUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRestoresUsersProjectsIssuesAndComments() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "JSON 오류", "댓글까지 저장되어야 한다.", Priority.MAJOR);
        fixture.issueService.addComment(issue.getId(), fixture.tester.getId(), "첫 댓글");

        JsonUserRepository users = new JsonUserRepository(tempDir);
        JsonProjectRepository projects = new JsonProjectRepository(tempDir);
        JsonIssueRepository issues = new JsonIssueRepository(tempDir);

        assertEquals(4, users.findAll().size());
        assertEquals(1, projects.findAll().size());
        assertEquals(1, issues.findAll().size());
        assertEquals(1, issues.findById(issue.getId()).orElseThrow().getComments().size());
    }

    @Test
    void missingFilesStartWithEmptyLists() {
        JsonUserRepository users = new JsonUserRepository(tempDir);
        JsonProjectRepository projects = new JsonProjectRepository(tempDir);
        JsonIssueRepository issues = new JsonIssueRepository(tempDir);

        assertTrue(users.findAll().isEmpty());
        assertTrue(projects.findAll().isEmpty());
        assertTrue(issues.findAll().isEmpty());
    }

    @Test
    void commentOrderIsKeptAfterReload() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "순서 오류", "댓글이 작성 시간 순서여야 한다.", Priority.MAJOR);
        fixture.issueService.addComment(issue.getId(), fixture.tester.getId(), "첫 번째");
        fixture.issueService.addComment(issue.getId(), fixture.pl.getId(), "두 번째");

        Issue reloaded = new JsonIssueRepository(tempDir).findById(issue.getId()).orElseThrow();

        assertEquals(IssueStatus.NEW, reloaded.getStatus());
        assertEquals("첫 번째", reloaded.getComments().getFirst().getContent());
        assertEquals("두 번째", reloaded.getComments().get(1).getContent());
    }
}

