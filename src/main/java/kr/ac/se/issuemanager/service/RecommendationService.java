package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.repository.IssueRepository;
import kr.ac.se.issuemanager.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RecommendationService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public RecommendationService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public List<Recommendation> recommendAssignees(String issueId) {
        Issue target = issueRepository.findById(issueId)
                .orElseThrow(() -> new ServiceException("이슈를 찾을 수 없습니다: " + issueId));
        Map<String, Double> scoreByFixer = new HashMap<>();
        Map<String, LocalDateTime> latestByFixer = new HashMap<>();

        for (Issue solved : issueRepository.findAll()) {
            if (solved.getId().equals(target.getId()) || solved.getFixerId() == null || !isSolved(solved)) {
                continue;
            }
            double score = calculateSimilarity(target, solved);
            if (score <= 0) {
                continue;
            }
            scoreByFixer.merge(solved.getFixerId(), score, Double::sum);
            latestByFixer.merge(solved.getFixerId(), solved.getReportedDate(), (left, right) ->
                    left.isAfter(right) ? left : right);
        }

        if (scoreByFixer.isEmpty()) {
            return fallbackDevelopers();
        }

        return scoreByFixer.entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<String, Double>>comparingDouble(Map.Entry::getValue).reversed()
                        .thenComparing(entry -> latestByFixer.get(entry.getKey()), Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(3)
                .map(entry -> new Recommendation(entry.getKey(), entry.getValue()))
                .toList();
    }

    public double calculateSimilarity(Issue left, Issue right) {
        Set<String> leftTokens = tokenize(left.searchableText());
        Set<String> rightTokens = tokenize(right.searchableText());
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);
        Set<String> union = new HashSet<>(leftTokens);
        union.addAll(rightTokens);
        return (double) intersection.size() / union.size();
    }

    private boolean isSolved(Issue issue) {
        return issue.getStatus() == IssueStatus.FIXED
                || issue.getStatus() == IssueStatus.RESOLVED
                || issue.getStatus() == IssueStatus.CLOSED;
    }

    private Set<String> tokenize(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ");
        Set<String> tokens = new HashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (token.length() > 1) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private List<Recommendation> fallbackDevelopers() {
        List<Recommendation> fallback = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.getRole() == Role.DEV) {
                fallback.add(new Recommendation(user.getId(), 0.0));
            }
        }
        return fallback.stream().limit(3).toList();
    }
}

