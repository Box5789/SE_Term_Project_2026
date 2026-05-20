package kr.ac.se.issuemanager.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Comment {
    private String id;
    private String issueId;
    private String authorId;
    private String content;
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(String id, String issueId, String authorId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.issueId = issueId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Comment comment)) {
            return false;
        }
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

