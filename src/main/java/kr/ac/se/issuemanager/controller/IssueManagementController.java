package kr.ac.se.issuemanager.controller;

import kr.ac.se.issuemanager.app.ApplicationContext;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;
import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.service.IssueSearchCriteria;
import kr.ac.se.issuemanager.service.Recommendation;
import kr.ac.se.issuemanager.service.ServiceException;

import java.util.List;
import java.util.Map;

public class IssueManagementController {
    private final ApplicationContext context;

    public IssueManagementController(ApplicationContext context) {
        this.context = context;
    }

    public static IssueManagementController bootstrapDefault() {
        return new IssueManagementController(ApplicationContext.bootstrapDefault());
    }

    public List<User> listUsers() {
        return context.userService().listUsers();
    }

    public List<User> listDevelopers() {
        return context.userService().listUsers().stream()
                .filter(user -> user.getRole() == Role.DEV)
                .toList();
    }

    public List<Project> listProjects() {
        return context.projectService().listProjects();
    }

    public List<Issue> searchIssues(String projectId, IssueStatus status, String keyword) {
        IssueSearchCriteria criteria = new IssueSearchCriteria()
                .setProjectId(projectId)
                .setStatus(status)
                .setKeyword(keyword);
        return context.issueService().searchIssues(criteria);
    }

    public Issue findIssue(String issueId) {
        return context.issueService().findIssue(issueId);
    }

    public Issue registerIssue(String projectId, String reporterId, String title,
                               String description, Priority priority) {
        return context.issueService().registerIssue(projectId, reporterId, title, description, priority);
    }

    public Issue addComment(String issueId, String authorId, String content) {
        return context.issueService().addComment(issueId, authorId, content);
    }

    public Issue assignIssue(String issueId, String plId, String assigneeId, String comment) {
        return context.issueService().assignIssue(issueId, plId, assigneeId, comment);
    }

    public Issue markFixed(String issueId, String devId, String comment) {
        return context.issueService().markFixed(issueId, devId, comment);
    }

    public Issue resolveIssue(String issueId, String testerId, String comment) {
        return context.issueService().resolveIssue(issueId, testerId, comment);
    }

    public Issue closeIssue(String issueId, String plId, String comment) {
        return context.issueService().closeIssue(issueId, plId, comment);
    }

    public Issue reopenIssue(String issueId, String userId, String reason) {
        return context.issueService().reopenIssue(issueId, userId, reason);
    }

    public List<Recommendation> recommendAssignees(String issueId) {
        return context.recommendationService().recommendAssignees(issueId);
    }

    public User createUser(String actorId, String username, Role role) {
        requireAdmin(actorId);
        return context.userService().createUser(username, role);
    }

    public Project createProject(String actorId, String name, String description) {
        requireAdmin(actorId);
        return context.projectService().createProject(name, description);
    }

    public Map<IssueStatus, Long> countByStatus(String projectId) {
        return context.statisticsService().countByStatus(projectId);
    }

    public Map<String, Long> countByAssignee(String projectId) {
        return context.statisticsService().countByAssignee(projectId);
    }

    public Map<String, Long> countByFixer(String projectId) {
        return context.statisticsService().countByFixer(projectId);
    }

    public Map<String, Long> countByDay(String projectId) {
        return context.statisticsService().countByDay(projectId);
    }

    private void requireAdmin(String actorId) {
        User actor = context.userService().findById(actorId);
        if (actor.getRole() != Role.ADMIN) {
            throw new ServiceException("admin 사용자만 생성할 수 있습니다.");
        }
    }
}
