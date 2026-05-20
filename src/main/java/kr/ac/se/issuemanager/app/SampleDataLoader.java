package kr.ac.se.issuemanager.app;

import kr.ac.se.issuemanager.model.Comment;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;
import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class SampleDataLoader {
    private SampleDataLoader() {
    }

    public static void ensureDemoData(ApplicationContext context) {
        if (context.userRepository().findAll().isEmpty()) {
            context.userRepository().saveAll(defaultUsers());
        }
        if (context.projectRepository().findAll().isEmpty()) {
            context.projectRepository().save(new Project(
                    "PROJECT-001",
                    "project1",
                    "SE Term Project 데모 프로젝트",
                    LocalDateTime.of(2026, 5, 13, 10, 0)
            ));
        }
        if (context.issueRepository().findAll().isEmpty()) {
            context.issueRepository().saveAll(defaultIssues());
        }
    }

    private static List<User> defaultUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("admin", "admin", Role.ADMIN));
        users.add(new User("PL1", "PL1", Role.PL));
        users.add(new User("PL2", "PL2", Role.PL));
        for (int index = 1; index <= 10; index++) {
            users.add(new User("dev" + index, "dev" + index, Role.DEV));
        }
        for (int index = 1; index <= 5; index++) {
            users.add(new User("tester" + index, "tester" + index, Role.TESTER));
        }
        return users;
    }

    private static List<Issue> defaultIssues() {
        List<Issue> issues = new ArrayList<>();
        issues.add(solvedIssue("ISSUE-001", "로그인 버튼 클릭 시 오류 발생",
                "로그인 화면에서 버튼을 클릭하면 인증 예외가 발생한다.",
                "tester2", "dev1", Priority.MAJOR, IssueStatus.CLOSED,
                LocalDateTime.of(2026, 5, 18, 14, 10)));
        issues.add(solvedIssue("ISSUE-002", "검색 결과 필터가 초기화됨",
                "이슈 검색 후 상세 화면에서 돌아오면 상태 필터가 사라진다.",
                "tester3", "dev2", Priority.MINOR, IssueStatus.RESOLVED,
                LocalDateTime.of(2026, 5, 19, 15, 30)));
        issues.add(solvedIssue("ISSUE-003", "JSON 저장 후 댓글 순서가 바뀜",
                "저장소를 다시 로드하면 댓글 history가 작성 시간 순서로 나오지 않는다.",
                "tester4", "dev5", Priority.CRITICAL, IssueStatus.FIXED,
                LocalDateTime.of(2026, 5, 20, 11, 40)));
        return issues;
    }

    private static Issue solvedIssue(String id, String title, String description, String reporterId,
                                     String fixerId, Priority priority, IssueStatus status,
                                     LocalDateTime reportedDate) {
        Issue issue = new Issue(id, "PROJECT-001", title, description, reporterId, reportedDate, priority, status);
        issue.setAssigneeId(fixerId);
        issue.setFixerId(fixerId);
        issue.addComment(new Comment("COMMENT-" + id.substring(id.length() - 3),
                id,
                reporterId,
                "재현 절차를 확인했습니다.",
                reportedDate.plusMinutes(5)));
        issue.addComment(new Comment("COMMENT-FIX-" + id.substring(id.length() - 3),
                id,
                fixerId,
                "수정 완료 후 fixed 처리했습니다.",
                reportedDate.plusHours(3)));
        return issue;
    }
}

