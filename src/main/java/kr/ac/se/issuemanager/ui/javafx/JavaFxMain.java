package kr.ac.se.issuemanager.ui.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kr.ac.se.issuemanager.controller.IssueManagementController;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.service.ServiceException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaFxMain extends Application {
    private IssueManagementController controller;
    private ComboBox<User> userBox;
    private ComboBox<Project> projectBox;
    private ComboBox<IssueStatus> statusBox;
    private ComboBox<kr.ac.se.issuemanager.model.Priority> priorityBox;
    private ComboBox<User> assigneeBox;
    private TextField keywordField;
    private TextField titleField;
    private TextField newUserNameField;
    private ComboBox<Role> newUserRoleBox;
    private TextField newProjectNameField;
    private TextArea descriptionArea;
    private TextArea commentArea;
    private TextArea newProjectDescriptionArea;
    private TextArea detailArea;
    private TextArea statisticsArea;
    private ListView<Issue> issueList;
    private Map<String, User> usersById;

    @Override
    public void start(Stage stage) {
        controller = IssueManagementController.bootstrapDefault();
        usersById = controller.listUsers().stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(topBar());
        root.setLeft(issueBrowser());
        root.setCenter(issueDetail());
        root.setRight(actionPanel());
        root.setBottom(adminPanel());

        refreshIssues();
        stage.setTitle("SE Term Project Issue Manager - JavaFX");
        stage.setScene(new Scene(root, 1180, 820));
        stage.show();
    }

    private HBox topBar() {
        userBox = new ComboBox<>(FXCollections.observableArrayList(controller.listUsers()));
        userBox.getSelectionModel().selectFirst();
        projectBox = new ComboBox<>(FXCollections.observableArrayList(controller.listProjects()));
        projectBox.getSelectionModel().selectFirst();
        Button refresh = new Button("새로고침");
        refresh.setOnAction(event -> refreshIssues());
        HBox bar = new HBox(10, new Label("사용자"), userBox, new Label("프로젝트"), projectBox, refresh);
        bar.setPadding(new Insets(0, 0, 12, 0));
        return bar;
    }

    private VBox issueBrowser() {
        statusBox = new ComboBox<>(FXCollections.observableArrayList(IssueStatus.values()));
        statusBox.setPromptText("상태");
        keywordField = new TextField();
        keywordField.setPromptText("키워드");
        Button search = new Button("검색");
        search.setOnAction(event -> refreshIssues());
        Button reset = new Button("초기화");
        reset.setOnAction(event -> resetFilters());
        HBox searchButtons = new HBox(6, search, reset);
        issueList = new ListView<>();
        issueList.setPrefWidth(360);
        issueList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> showIssue(newValue));
        VBox browser = new VBox(8, new Label("이슈 목록"), statusBox, keywordField, searchButtons, issueList);
        VBox.setVgrow(issueList, Priority.ALWAYS);
        browser.setPadding(new Insets(0, 12, 0, 0));
        return browser;
    }

    private VBox issueDetail() {
        detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        statisticsArea = new TextArea();
        statisticsArea.setEditable(false);
        statisticsArea.setPrefRowCount(8);
        VBox detail = new VBox(8, new Label("상세 정보와 댓글"), detailArea, new Label("통계"), statisticsArea);
        VBox.setVgrow(detailArea, Priority.ALWAYS);
        return detail;
    }

    private VBox actionPanel() {
        titleField = new TextField();
        titleField.setPromptText("이슈 제목");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("이슈 설명");
        descriptionArea.setPrefRowCount(5);
        priorityBox = new ComboBox<>(FXCollections.observableArrayList(kr.ac.se.issuemanager.model.Priority.values()));
        priorityBox.getSelectionModel().select(kr.ac.se.issuemanager.model.Priority.MAJOR);
        commentArea = new TextArea();
        commentArea.setPromptText("댓글 또는 상태 변경 사유");
        commentArea.setPrefRowCount(4);
        assigneeBox = new ComboBox<>(FXCollections.observableArrayList(controller.listDevelopers()));
        assigneeBox.setPromptText("담당 dev");

        Button register = new Button("등록");
        register.setOnAction(event -> runSafely(this::registerIssue));
        Button comment = new Button("댓글 추가");
        comment.setOnAction(event -> runSafely(this::addComment));
        Button assign = new Button("배정");
        assign.setOnAction(event -> runSafely(this::assignIssue));
        Button fixed = new Button("fixed");
        fixed.setOnAction(event -> runSafely(this::markFixed));
        Button resolved = new Button("resolved");
        resolved.setOnAction(event -> runSafely(this::resolveIssue));
        Button closed = new Button("closed");
        closed.setOnAction(event -> runSafely(this::closeIssue));
        Button reopened = new Button("reopened");
        reopened.setOnAction(event -> runSafely(this::reopenIssue));
        Button recommend = new Button("추천");
        recommend.setOnAction(event -> runSafely(this::recommendAssignees));

        GridPane buttons = new GridPane();
        buttons.setHgap(6);
        buttons.setVgap(6);
        buttons.addRow(0, register, comment);
        buttons.addRow(1, assign, recommend);
        buttons.addRow(2, fixed, resolved);
        buttons.addRow(3, closed, reopened);

        VBox panel = new VBox(8,
                new Label("작업"),
                titleField,
                descriptionArea,
                priorityBox,
                assigneeBox,
                commentArea,
                buttons);
        panel.setPrefWidth(280);
        panel.setPadding(new Insets(0, 0, 0, 12));
        return panel;
    }

    private VBox adminPanel() {
        newUserNameField = new TextField();
        newUserNameField.setPromptText("사용자 이름");
        newUserRoleBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        newUserRoleBox.setPromptText("역할");
        Button createUser = new Button("사용자 생성");
        createUser.setOnAction(event -> runSafely(this::createUser));
        HBox userForm = new HBox(8, new Label("사용자"), newUserNameField, newUserRoleBox, createUser);

        newProjectNameField = new TextField();
        newProjectNameField.setPromptText("프로젝트 이름");
        newProjectDescriptionArea = new TextArea();
        newProjectDescriptionArea.setPromptText("프로젝트 설명");
        newProjectDescriptionArea.setPrefRowCount(2);
        Button createProject = new Button("프로젝트 생성");
        createProject.setOnAction(event -> runSafely(this::createProject));
        HBox projectForm = new HBox(8, new Label("프로젝트"), newProjectNameField, newProjectDescriptionArea, createProject);
        HBox.setHgrow(newProjectDescriptionArea, Priority.ALWAYS);

        VBox panel = new VBox(8, new Label("관리"), userForm, projectForm);
        panel.setPadding(new Insets(12, 0, 0, 0));
        return panel;
    }

    private void refreshIssues() {
        Project project = projectBox.getSelectionModel().getSelectedItem();
        List<Issue> issues = controller.searchIssues(
                project == null ? null : project.getId(),
                statusBox == null ? null : statusBox.getSelectionModel().getSelectedItem(),
                keywordField == null ? null : keywordField.getText());
        issueList.setItems(FXCollections.observableArrayList(issues));
        refreshStatistics();
    }

    private void refreshStatistics() {
        Project project = projectBox.getSelectionModel().getSelectedItem();
        if (project == null || statisticsArea == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        appendStatSection(builder, "상태별", controller.countByStatus(project.getId()));
        appendUserStatSection(builder, "담당자별", controller.countByAssignee(project.getId()));
        appendUserStatSection(builder, "fixer별", controller.countByFixer(project.getId()));
        appendStatSection(builder, "일별", controller.countByDay(project.getId()));
        statisticsArea.setText(builder.toString());
    }

    private void resetFilters() {
        statusBox.getSelectionModel().clearSelection();
        keywordField.clear();
        refreshIssues();
    }

    private void appendStatSection(StringBuilder builder, String title, Map<?, Long> statistics) {
        builder.append(title).append(System.lineSeparator());
        if (statistics.isEmpty()) {
            builder.append("- 없음").append(System.lineSeparator()).append(System.lineSeparator());
            return;
        }
        statistics.forEach((label, count) -> appendStatLine(builder, String.valueOf(label), count));
        builder.append(System.lineSeparator());
    }

    private void appendUserStatSection(StringBuilder builder, String title, Map<String, Long> statistics) {
        builder.append(title).append(System.lineSeparator());
        if (statistics.isEmpty()) {
            builder.append("- 없음").append(System.lineSeparator()).append(System.lineSeparator());
            return;
        }
        statistics.forEach((userId, count) -> appendStatLine(builder, nameOf(userId), count));
        builder.append(System.lineSeparator());
    }

    private void appendStatLine(StringBuilder builder, String label, Long count) {
        builder.append("- ").append(label).append(": ").append(count).append("건").append(System.lineSeparator());
    }

    private void showIssue(Issue issue) {
        if (issue == null) {
            detailArea.clear();
            return;
        }
        String comments = issue.getComments().stream()
                .map(comment -> "- %s / %s: %s".formatted(comment.getCreatedAt(), nameOf(comment.getAuthorId()), comment.getContent()))
                .collect(Collectors.joining(System.lineSeparator()));
        detailArea.setText("""
                ID: %s
                제목: %s
                설명: %s
                reporter: %s
                assignee: %s
                fixer: %s
                reportedDate: %s
                priority: %s
                status: %s

                댓글
                %s
                """.formatted(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                nameOf(issue.getReporterId()),
                nameOf(issue.getAssigneeId()),
                nameOf(issue.getFixerId()),
                issue.getReportedDate(),
                issue.getPriority(),
                issue.getStatus(),
                comments.isBlank() ? "(없음)" : comments));
    }

    private void registerIssue() {
        User user = selectedUser();
        Project project = selectedProject();
        Issue issue = controller.registerIssue(project.getId(), user.getId(),
                titleField.getText(), descriptionArea.getText(), priorityBox.getSelectionModel().getSelectedItem());
        titleField.clear();
        descriptionArea.clear();
        refreshIssues();
        issueList.getSelectionModel().select(issue);
    }

    private void addComment() {
        Issue issue = selectedIssue();
        controller.addComment(issue.getId(), selectedUser().getId(), commentArea.getText());
        commentArea.clear();
        refreshIssues();
        showIssue(controller.findIssue(issue.getId()));
    }

    private void assignIssue() {
        Issue issue = selectedIssue();
        User assignee = assigneeBox.getSelectionModel().getSelectedItem();
        if (assignee == null) {
            throw new ServiceException("담당 dev를 선택하세요.");
        }
        controller.assignIssue(issue.getId(), selectedUser().getId(), assignee.getId(), commentArea.getText());
        commentArea.clear();
        refreshIssues();
    }

    private void markFixed() {
        Issue issue = selectedIssue();
        controller.markFixed(issue.getId(), selectedUser().getId(), commentArea.getText());
        commentArea.clear();
        refreshIssues();
    }

    private void resolveIssue() {
        Issue issue = selectedIssue();
        controller.resolveIssue(issue.getId(), selectedUser().getId(), commentArea.getText());
        commentArea.clear();
        refreshIssues();
    }

    private void closeIssue() {
        Issue issue = selectedIssue();
        controller.closeIssue(issue.getId(), selectedUser().getId(), commentArea.getText());
        commentArea.clear();
        refreshIssues();
    }

    private void reopenIssue() {
        Issue issue = selectedIssue();
        controller.reopenIssue(issue.getId(), selectedUser().getId(), commentArea.getText());
        commentArea.clear();
        refreshIssues();
    }

    private void recommendAssignees() {
        Issue issue = selectedIssue();
        String text = controller.recommendAssignees(issue.getId()).stream()
                .map(recommendation -> "%s (%.3f)".formatted(nameOf(recommendation.devId()), recommendation.score()))
                .collect(Collectors.joining(System.lineSeparator()));
        new Alert(Alert.AlertType.INFORMATION, text.isBlank() ? "추천 후보가 없습니다." : text).showAndWait();
    }

    private void createUser() {
        controller.createUser(selectedUser().getId(), newUserNameField.getText(),
                newUserRoleBox.getSelectionModel().getSelectedItem());
        newUserNameField.clear();
        newUserRoleBox.getSelectionModel().clearSelection();
        refreshUserControls();
    }

    private void createProject() {
        Project project = controller.createProject(selectedUser().getId(),
                newProjectNameField.getText(), newProjectDescriptionArea.getText());
        newProjectNameField.clear();
        newProjectDescriptionArea.clear();
        refreshProjectControls(project.getId());
    }

    private void refreshUserControls() {
        String selectedUserId = userBox.getSelectionModel().getSelectedItem() == null
                ? null : userBox.getSelectionModel().getSelectedItem().getId();
        String selectedAssigneeId = assigneeBox.getSelectionModel().getSelectedItem() == null
                ? null : assigneeBox.getSelectionModel().getSelectedItem().getId();
        List<User> users = controller.listUsers();
        usersById = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        userBox.setItems(FXCollections.observableArrayList(users));
        selectUser(userBox, selectedUserId);
        assigneeBox.setItems(FXCollections.observableArrayList(controller.listDevelopers()));
        selectUser(assigneeBox, selectedAssigneeId);
    }

    private void refreshProjectControls(String selectedProjectId) {
        projectBox.setItems(FXCollections.observableArrayList(controller.listProjects()));
        projectBox.getItems().stream()
                .filter(project -> project.getId().equals(selectedProjectId))
                .findFirst()
                .ifPresentOrElse(
                        project -> projectBox.getSelectionModel().select(project),
                        () -> projectBox.getSelectionModel().selectFirst());
        refreshIssues();
    }

    private void selectUser(ComboBox<User> comboBox, String userId) {
        if (userId != null) {
            comboBox.getItems().stream()
                    .filter(user -> user.getId().equals(userId))
                    .findFirst()
                    .ifPresent(user -> comboBox.getSelectionModel().select(user));
        }
        if (comboBox.getSelectionModel().getSelectedItem() == null) {
            comboBox.getSelectionModel().selectFirst();
        }
    }

    private User selectedUser() {
        User user = userBox.getSelectionModel().getSelectedItem();
        if (user == null) {
            throw new ServiceException("사용자를 선택하세요.");
        }
        return user;
    }

    private Project selectedProject() {
        Project project = projectBox.getSelectionModel().getSelectedItem();
        if (project == null) {
            throw new ServiceException("프로젝트를 선택하세요.");
        }
        return project;
    }

    private Issue selectedIssue() {
        Issue issue = issueList.getSelectionModel().getSelectedItem();
        if (issue == null) {
            throw new ServiceException("이슈를 선택하세요.");
        }
        return issue;
    }

    private String nameOf(String userId) {
        if (userId == null) {
            return "-";
        }
        return usersById.getOrDefault(userId, new User(userId, userId, null)).getUsername();
    }

    private void runSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException exception) {
            new Alert(Alert.AlertType.ERROR, exception.getMessage()).showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
