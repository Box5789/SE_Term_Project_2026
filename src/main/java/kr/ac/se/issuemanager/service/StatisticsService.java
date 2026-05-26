package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.repository.IssueRepository;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {
    private final IssueRepository issueRepository;

    public StatisticsService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public Map<String, Long> countByDay(String projectId) {
        return issues(projectId).stream()
                .collect(Collectors.groupingBy(issue -> issue.getReportedDate().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> countByMonth(String projectId) {
        return issues(projectId).stream()
                .collect(Collectors.groupingBy(issue -> YearMonth.from(issue.getReportedDate()).toString(),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    public Map<IssueStatus, Long> countByStatus(String projectId) {
        return issues(projectId).stream()
                .collect(Collectors.groupingBy(Issue::getStatus,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> countByAssignee(String projectId) {
        return issues(projectId).stream()
                .filter(issue -> issue.getAssigneeId() != null)
                .collect(Collectors.groupingBy(Issue::getAssigneeId,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> countByFixer(String projectId) {
        return issues(projectId).stream()
                .filter(issue -> issue.getFixerId() != null)
                .collect(Collectors.groupingBy(Issue::getFixerId,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    private java.util.List<Issue> issues(String projectId) {
        if (projectId == null) {
            return issueRepository.findAll();
        }
        return issueRepository.findByProject(projectId);
    }
}

