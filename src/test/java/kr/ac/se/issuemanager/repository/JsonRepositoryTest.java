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
import java.time.LocalDateTime;

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

    @Test
    void findByIdReflectsChangesSavedByAnotherRepository() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "동기화 오류", "다른 UI에서 저장한 상태를 새로고침해야 한다.", Priority.MAJOR);
        JsonIssueRepository javaFxRepository = new JsonIssueRepository(tempDir);
        JsonIssueRepository swingRepository = new JsonIssueRepository(tempDir);

        Issue changed = swingRepository.findById(issue.getId()).orElseThrow();
        changed.setAssigneeId(fixture.dev2.getId());
        changed.setFixerId(fixture.dev2.getId());
        changed.setStatus(IssueStatus.FIXED);
        swingRepository.save(changed);

        Issue reloaded = javaFxRepository.findById(issue.getId()).orElseThrow();

        assertEquals(IssueStatus.FIXED, reloaded.getStatus());
        assertEquals(fixture.dev2.getId(), reloaded.getFixerId());
    }

    @Test
    void saveKeepsIssuesAddedByAnotherRepository() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue original = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "기존 이슈", "저장 직전 reload 대상이다.", Priority.MAJOR);
        JsonIssueRepository firstRepository = new JsonIssueRepository(tempDir);
        JsonIssueRepository secondRepository = new JsonIssueRepository(tempDir);
        Issue staleOriginal = firstRepository.findById(original.getId()).orElseThrow();

        Issue externalIssue = new Issue("ISSUE-999", fixture.project.getId(), "외부 이슈",
                "다른 UI가 먼저 저장한 이슈다.", fixture.tester.getId(),
                LocalDateTime.of(2026, 5, 27, 9, 0), Priority.MINOR, IssueStatus.NEW);
        secondRepository.save(externalIssue);

        staleOriginal.setStatus(IssueStatus.REOPENED);
        firstRepository.save(staleOriginal);

        JsonIssueRepository verifier = new JsonIssueRepository(tempDir);
        assertEquals(2, verifier.findAll().size());
        assertTrue(verifier.findById(externalIssue.getId()).isPresent());
        assertEquals(IssueStatus.REOPENED, verifier.findById(original.getId()).orElseThrow().getStatus());
    }
}
