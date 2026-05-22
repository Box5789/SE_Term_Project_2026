package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.ServiceFixture;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IssueServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void testerCanRegisterIssueWithAutomaticFields() {
        ServiceFixture fixture = new ServiceFixture(tempDir);

        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "로그인 오류", "버튼 클릭 시 예외가 발생한다.", Priority.CRITICAL);

        assertEquals(IssueStatus.NEW, issue.getStatus());
        assertEquals(fixture.tester.getId(), issue.getReporterId());
        assertEquals(Priority.CRITICAL, issue.getPriority());
    }

    @Test
    void titleAndDescriptionAreRequired() {
        ServiceFixture fixture = new ServiceFixture(tempDir);

        assertThrows(ServiceException.class, () -> fixture.issueService.registerIssue(
                fixture.project.getId(), fixture.tester.getId(), "", "설명", Priority.MAJOR));
        assertThrows(ServiceException.class, () -> fixture.issueService.registerIssue(
                fixture.project.getId(), fixture.tester.getId(), "제목", " ", Priority.MAJOR));
    }

    @Test
    void statusFlowFromNewToClosedWorks() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "검색 오류", "상태 필터가 적용되지 않는다.", Priority.MAJOR);

        Issue assigned = fixture.issueService.assignIssue(issue.getId(), fixture.pl.getId(), fixture.dev1.getId(),
                "dev1에게 배정합니다.");
        assertEquals(IssueStatus.ASSIGNED, assigned.getStatus());
        assertEquals(fixture.dev1.getId(), assigned.getAssigneeId());

        Issue fixed = fixture.issueService.markFixed(issue.getId(), fixture.dev1.getId(), "수정 완료");
        assertEquals(IssueStatus.FIXED, fixed.getStatus());
        assertEquals(fixture.dev1.getId(), fixed.getFixerId());

        Issue resolved = fixture.issueService.resolveIssue(issue.getId(), fixture.tester.getId(), "검증 완료");
        assertEquals(IssueStatus.RESOLVED, resolved.getStatus());

        Issue closed = fixture.issueService.closeIssue(issue.getId(), fixture.pl.getId(), "종료합니다.");
        assertEquals(IssueStatus.CLOSED, closed.getStatus());
    }

    @Test
    void invalidRoleAndAssigneeAreRejected() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "저장 오류", "재실행 후 데이터가 없다.", Priority.MAJOR);

        assertThrows(ServiceException.class, () -> fixture.issueService.assignIssue(
                issue.getId(), fixture.tester.getId(), fixture.dev1.getId(), "잘못된 배정"));
        fixture.issueService.assignIssue(issue.getId(), fixture.pl.getId(), fixture.dev1.getId(), "정상 배정");
        assertThrows(ServiceException.class, () -> fixture.issueService.markFixed(
                issue.getId(), fixture.dev2.getId(), "다른 dev가 처리"));
    }

    @Test
    void reopenAndReassignFlowWorks() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "댓글 정렬 오류", "댓글 순서가 뒤집힌다.", Priority.MINOR);
        fixture.issueService.assignIssue(issue.getId(), fixture.pl.getId(), fixture.dev1.getId(), "배정");
        fixture.issueService.markFixed(issue.getId(), fixture.dev1.getId(), "수정");

        Issue reopened = fixture.issueService.reopenIssue(issue.getId(), fixture.tester.getId(), "아직 재현됩니다.");
        assertEquals(IssueStatus.REOPENED, reopened.getStatus());

        Issue reassigned = fixture.issueService.assignIssue(issue.getId(), fixture.pl.getId(), fixture.dev2.getId(),
                "dev2에게 재배정합니다.");
        assertEquals(IssueStatus.ASSIGNED, reassigned.getStatus());
        assertEquals(fixture.dev2.getId(), reassigned.getAssigneeId());
    }
}

