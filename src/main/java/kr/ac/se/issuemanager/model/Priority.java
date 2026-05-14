package kr.ac.se.issuemanager.model;

public enum Priority {
    BLOCKER,
    CRITICAL,
    MAJOR,
    MINOR,
    TRIVIAL;

    public static Priority defaultIfNull(Priority priority) {
        return priority == null ? MAJOR : priority;
    }
}

