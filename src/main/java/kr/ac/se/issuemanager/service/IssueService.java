package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.model.Comment;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.repository.IssueRepository;
import kr.ac.se.issuemanager.repository.ProjectRepository;
import kr.ac.se.issuemanager.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class IssueService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public IssueService(IssueRepository issueRepository, UserRepository userRepository,
                        ProjectRepository projectRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public Issue registerIssue(String projectId, String reporterId, String title,
                               String description, Priority priority) {
        UserService.requireText(title, "이슈 제목");
        UserService.requireText(description, "이슈 설명");
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ServiceException("프로젝트를 찾을 수 없습니다: " + projectId));
        User reporter = requireUser(reporterId);
        requireRole(reporter, Role.TESTER, "tester만 이슈를 등록할 수 있습니다.");

        Issue issue = new Issue(
                IdGenerator.nextId("ISSUE", issueRepository.findAll(), Issue::getId),
                projectId,
                title.trim(),
                description.trim(),
                reporterId,
                LocalDateTime.now(),
                Priority.defaultIfNull(priority),
                IssueStatus.NEW
        );
        return issueRepository.save(issue);
    }

    public List<Issue> searchIssues(IssueSearchCriteria criteria) {
        IssueSearchCriteria safeCriteria = criteria == null ? new IssueSearchCriteria() : criteria;
        String keyword = safeCriteria.getKeyword() == null ? "" : safeCriteria.getKeyword().trim().toLowerCase();
        return issueRepository.findAll().stream()
                .filter(issue -> safeCriteria.getProjectId() == null || issue.getProjectId().equals(safeCriteria.getProjectId()))
                .filter(issue -> safeCriteria.getReporterId() == null || issue.getReporterId().equals(safeCriteria.getReporterId()))
                .filter(issue -> safeCriteria.getAssigneeId() == null || safeCriteria.getAssigneeId().equals(issue.getAssigneeId()))
                .filter(issue -> safeCriteria.getStatus() == null || issue.getStatus() == safeCriteria.getStatus())
                .filter(issue -> safeCriteria.getPriority() == null || issue.getPriority() == safeCriteria.getPriority())
                .filter(issue -> keyword.isEmpty() || issue.searchableText().toLowerCase().contains(keyword))
                .sorted(Comparator.comparing(Issue::getReportedDate).reversed())
                .toList();
    }

    public Issue findIssue(String issueId) {
        return requireIssue(issueId);
    }

    public Issue addComment(String issueId, String authorId, String content) {
        Issue issue = requireIssue(issueId);
        requireUser(authorId);
        addCommentIfPresent(issue, authorId, content, true);
        return issueRepository.save(issue);
    }

    public Issue assignIssue(String issueId, String plId, String assigneeId, String comment) {
        Issue issue = requireIssue(issueId);
        User pl = requireUser(plId);
        User assignee = requireUser(assigneeId);
        requireRole(pl, Role.PL, "PL만 이슈를 배정할 수 있습니다.");
        requireRole(assignee, Role.DEV, "assignee는 dev 계정이어야 합니다.");
        if (issue.getStatus() != IssueStatus.NEW && issue.getStatus() != IssueStatus.REOPENED) {
            throw new ServiceException("new 또는 reopened 상태의 이슈만 배정할 수 있습니다.");
        }
        issue.setAssigneeId(assigneeId);
        issue.setStatus(IssueStatus.ASSIGNED);
        addCommentIfPresent(issue, plId, comment, false);
        return issueRepository.save(issue);
    }

    public Issue markFixed(String issueId, String devId, String comment) {
        Issue issue = requireIssue(issueId);
        User dev = requireUser(devId);
        requireRole(dev, Role.DEV, "dev만 fixed 처리를 할 수 있습니다.");
        if (issue.getStatus() != IssueStatus.ASSIGNED) {
            throw new ServiceException("assigned 상태의 이슈만 fixed 처리할 수 있습니다.");
        }
        if (!devId.equals(issue.getAssigneeId())) {
            throw new ServiceException("현재 assignee인 dev만 fixed 처리할 수 있습니다.");
        }
        addCommentIfPresent(issue, devId, comment, true);
        issue.setFixerId(devId);
        issue.setStatus(IssueStatus.FIXED);
        return issueRepository.save(issue);
    }

    public Issue resolveIssue(String issueId, String testerId, String comment) {
        Issue issue = requireIssue(issueId);
        User tester = requireUser(testerId);
        requireRole(tester, Role.TESTER, "tester만 resolved 처리할 수 있습니다.");
        if (issue.getStatus() != IssueStatus.FIXED) {
            throw new ServiceException("fixed 상태의 이슈만 resolved 처리할 수 있습니다.");
        }
        if (!testerId.equals(issue.getReporterId())) {
            throw new ServiceException("reporter tester만 resolved 처리할 수 있습니다.");
        }
        addCommentIfPresent(issue, testerId, comment, true);
        issue.setStatus(IssueStatus.RESOLVED);
        return issueRepository.save(issue);
    }

    public Issue closeIssue(String issueId, String plId, String comment) {
        Issue issue = requireIssue(issueId);
        User pl = requireUser(plId);
        requireRole(pl, Role.PL, "PL만 closed 처리할 수 있습니다.");
        if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new ServiceException("resolved 상태의 이슈만 closed 처리할 수 있습니다.");
        }
        addCommentIfPresent(issue, plId, comment, false);
        issue.setStatus(IssueStatus.CLOSED);
        return issueRepository.save(issue);
    }

    public Issue reopenIssue(String issueId, String userId, String reason) {
        UserService.requireText(reason, "재오픈 사유");
        Issue issue = requireIssue(issueId);
        User user = requireUser(userId);
        if (issue.getStatus() != IssueStatus.FIXED
                && issue.getStatus() != IssueStatus.RESOLVED
                && issue.getStatus() != IssueStatus.CLOSED) {
            throw new ServiceException("fixed, resolved, closed 상태만 reopened 처리할 수 있습니다.");
        }
        boolean reporterTester = user.getRole() == Role.TESTER && userId.equals(issue.getReporterId())
                && issue.getStatus() != IssueStatus.CLOSED;
        boolean projectLeader = user.getRole() == Role.PL;
        if (!reporterTester && !projectLeader) {
            throw new ServiceException("reporter tester 또는 PL만 reopened 처리할 수 있습니다.");
        }
        addCommentIfPresent(issue, userId, reason, true);
        issue.setStatus(IssueStatus.REOPENED);
        return issueRepository.save(issue);
    }

    private void addCommentIfPresent(Issue issue, String authorId, String content, boolean required) {
        if (content == null || content.trim().isEmpty()) {
            if (required) {
                throw new ServiceException("댓글 내용은 필수입니다.");
            }
            return;
        }
        Comment comment = new Comment(
                IdGenerator.nextId("COMMENT", issue.getComments(), Comment::getId),
                issue.getId(),
                authorId,
                content.trim(),
                LocalDateTime.now()
        );
        issue.addComment(comment);
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("사용자를 찾을 수 없습니다: " + userId));
    }

    private Issue requireIssue(String issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ServiceException("이슈를 찾을 수 없습니다: " + issueId));
    }

    private void requireRole(User user, Role role, String message) {
        if (user.getRole() != role) {
            throw new ServiceException(message);
        }
    }
}

