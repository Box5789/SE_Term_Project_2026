package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.ServiceFixture;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StatisticsRecommendationTest {
    @TempDir
    Path tempDir;

    @Test
    void statisticsCountStatusAndAssignee() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue issue = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "배정 오류", "담당자 통계에 포함되어야 한다.", Priority.MAJOR);
        fixture.issueService.assignIssue(issue.getId(), fixture.pl.getId(), fixture.dev1.getId(), "배정");

        assertEquals(1, fixture.statisticsService.countByStatus(fixture.project.getId()).get(IssueStatus.ASSIGNED));
        assertEquals(1, fixture.statisticsService.countByAssignee(fixture.project.getId()).get(fixture.dev1.getId()));
    }

    @Test
    void recommendationUsesSimilarSolvedIssueFixer() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue solved = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "로그인 버튼 오류", "로그인 화면 버튼 클릭 시 예외가 발생한다.", Priority.MAJOR);
        fixture.issueService.assignIssue(solved.getId(), fixture.pl.getId(), fixture.dev2.getId(), "배정");
        fixture.issueService.markFixed(solved.getId(), fixture.dev2.getId(), "로그인 버튼 예외 수정");

        Issue target = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "로그인 예외 발생", "로그인 버튼을 누르면 예외가 표시된다.", Priority.CRITICAL);

        List<Recommendation> recommendations = fixture.recommendationService.recommendAssignees(target.getId());

        assertFalse(recommendations.isEmpty());
        assertEquals(fixture.dev2.getId(), recommendations.getFirst().devId());
    }

    @Test
    void recommendationFallsBackToDevelopersWhenNoHistoryExists() {
        ServiceFixture fixture = new ServiceFixture(tempDir);
        Issue target = fixture.issueService.registerIssue(fixture.project.getId(), fixture.tester.getId(),
                "새 기능 오류", "이력과 무관한 신규 문제", Priority.MINOR);

        List<Recommendation> recommendations = fixture.recommendationService.recommendAssignees(target.getId());

        assertEquals(2, recommendations.size());
        assertEquals(0.0, recommendations.getFirst().score());
    }
}

