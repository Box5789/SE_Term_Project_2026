package kr.ac.se.issuemanager.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Issue {
    private String id;
    private String projectId;
    private String title;
    private String description;
    private String reporterId;
    private LocalDateTime reportedDate;
    private String fixerId;
    private String assigneeId;
    private Priority priority;
    private IssueStatus status;
    private List<Comment> comments = new ArrayList<>();

    public Issue() {
    }

    public Issue(String id, String projectId, String title, String description, String reporterId,
                 LocalDateTime reportedDate, Priority priority, IssueStatus status) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.reporterId = reporterId;
        this.reportedDate = reportedDate;
        this.priority = Priority.defaultIfNull(priority);
        this.status = status;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comments.sort(Comparator.comparing(Comment::getCreatedAt));
    }

    public String searchableText() {
        return ((title == null ? "" : title) + " " + (description == null ? "" : description)).trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public LocalDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getFixerId() {
        return fixerId;
    }

    public void setFixerId(String fixerId) {
        this.fixerId = fixerId;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments == null ? new ArrayList<>() : new ArrayList<>(comments);
        this.comments.sort(Comparator.comparing(Comment::getCreatedAt));
    }

    @Override
    public String toString() {
        return id + " [" + status + "] " + title;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Issue issue)) {
            return false;
        }
        return Objects.equals(id, issue.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

