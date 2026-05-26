package kr.ac.se.issuemanager;

import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.repository.IssueRepository;
import kr.ac.se.issuemanager.repository.ProjectRepository;
import kr.ac.se.issuemanager.repository.UserRepository;
import kr.ac.se.issuemanager.repository.json.JsonIssueRepository;
import kr.ac.se.issuemanager.repository.json.JsonProjectRepository;
import kr.ac.se.issuemanager.repository.json.JsonUserRepository;
import kr.ac.se.issuemanager.service.IssueService;
import kr.ac.se.issuemanager.service.ProjectService;
import kr.ac.se.issuemanager.service.RecommendationService;
import kr.ac.se.issuemanager.service.StatisticsService;
import kr.ac.se.issuemanager.service.UserService;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public final class ServiceFixture {
    public final UserRepository userRepository;
    public final ProjectRepository projectRepository;
    public final IssueRepository issueRepository;
    public final UserService userService;
    public final ProjectService projectService;
    public final IssueService issueService;
    public final StatisticsService statisticsService;
    public final RecommendationService recommendationService;
    public final User pl;
    public final User dev1;
    public final User dev2;
    public final User tester;
    public final Project project;

    public ServiceFixture(Path dataDirectory) {
        userRepository = new JsonUserRepository(dataDirectory);
        projectRepository = new JsonProjectRepository(dataDirectory);
        issueRepository = new JsonIssueRepository(dataDirectory);
        userService = new UserService(userRepository);
        projectService = new ProjectService(projectRepository);
        issueService = new IssueService(issueRepository, userRepository, projectRepository);
        statisticsService = new StatisticsService(issueRepository);
        recommendationService = new RecommendationService(issueRepository, userRepository);

        pl = new User("PL1", "PL1", Role.PL);
        dev1 = new User("dev1", "dev1", Role.DEV);
        dev2 = new User("dev2", "dev2", Role.DEV);
        tester = new User("tester1", "tester1", Role.TESTER);
        project = new Project("PROJECT-001", "project1", "테스트 프로젝트",
                LocalDateTime.of(2026, 5, 13, 10, 0));
        userRepository.saveAll(List.of(pl, dev1, dev2, tester));
        projectRepository.save(project);
    }
}
