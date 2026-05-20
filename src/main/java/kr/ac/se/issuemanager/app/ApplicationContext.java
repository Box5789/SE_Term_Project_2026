package kr.ac.se.issuemanager.app;

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

public class ApplicationContext {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final IssueService issueService;
    private final StatisticsService statisticsService;
    private final RecommendationService recommendationService;

    public ApplicationContext(Path dataDirectory) {
        this.userRepository = new JsonUserRepository(dataDirectory);
        this.projectRepository = new JsonProjectRepository(dataDirectory);
        this.issueRepository = new JsonIssueRepository(dataDirectory);
        this.userService = new UserService(userRepository);
        this.projectService = new ProjectService(projectRepository);
        this.issueService = new IssueService(issueRepository, userRepository, projectRepository);
        this.statisticsService = new StatisticsService(issueRepository);
        this.recommendationService = new RecommendationService(issueRepository, userRepository);
    }

    public static ApplicationContext bootstrapDefault() {
        Path dataDirectory = Path.of(System.getProperty("issue.data.dir", "data"));
        ApplicationContext context = new ApplicationContext(dataDirectory);
        SampleDataLoader.ensureDemoData(context);
        return context;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public ProjectRepository projectRepository() {
        return projectRepository;
    }

    public IssueRepository issueRepository() {
        return issueRepository;
    }

    public UserService userService() {
        return userService;
    }

    public ProjectService projectService() {
        return projectService;
    }

    public IssueService issueService() {
        return issueService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }

    public RecommendationService recommendationService() {
        return recommendationService;
    }
}

