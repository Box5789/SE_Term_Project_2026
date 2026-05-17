package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Priority;

public class IssueSearchCriteria {
    private String projectId;
    private String reporterId;
    private String assigneeId;
    private IssueStatus status;
    private Priority priority;
    private String keyword;

    public String getProjectId() {
        return projectId;
    }

    public IssueSearchCriteria setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getReporterId() {
        return reporterId;
    }

    public IssueSearchCriteria setReporterId(String reporterId) {
        this.reporterId = reporterId;
        return this;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public IssueSearchCriteria setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
        return this;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public IssueSearchCriteria setStatus(IssueStatus status) {
        this.status = status;
        return this;
    }

    public Priority getPriority() {
        return priority;
    }

    public IssueSearchCriteria setPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public IssueSearchCriteria setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }
}

