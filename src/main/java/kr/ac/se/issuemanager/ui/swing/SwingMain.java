package kr.ac.se.issuemanager.ui.swing;

import kr.ac.se.issuemanager.controller.IssueManagementController;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwingMain {
    private final IssueManagementController controller = IssueManagementController.bootstrapDefault();
    private final Map<String, User> usersById = controller.listUsers().stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    private final JComboBox<User> userBox = new JComboBox<>();
    private final JComboBox<Project> projectBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "제목", "상태", "담당자"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextArea detailArea = new JTextArea();
    private final JTextArea commentArea = new JTextArea(4, 30);
    private List<Issue> currentIssues = List.of();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingMain().show());
    }

    private void show() {
        controller.listUsers().forEach(userBox::addItem);
        controller.listProjects().forEach(projectBox::addItem);

        JFrame frame = new JFrame("SE Term Project Issue Manager - Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.add(topPanel(), BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.WEST);
        frame.add(detailPanel(), BorderLayout.CENTER);
        frame.add(actionPanel(), BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(event -> showSelectedIssue());
        refreshIssues();
        frame.setSize(960, 620);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel topPanel() {
        JButton refresh = new JButton("새로고침");
        refresh.addActionListener(event -> refreshIssues());
        JPanel panel = new JPanel();
        panel.add(userBox);
        panel.add(projectBox);
        panel.add(refresh);
        return panel;
    }

    private JScrollPane detailPanel() {
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        return new JScrollPane(detailArea);
    }

    private JPanel actionPanel() {
        JButton fixed = new JButton("fixed");
        fixed.addActionListener(event -> runSafely(() -> {
            Issue issue = selectedIssue();
            controller.markFixed(issue.getId(), selectedUser().getId(), commentArea.getText());
            commentArea.setText("");
            refreshIssues();
        }));
        JButton resolved = new JButton("resolved");
        resolved.addActionListener(event -> runSafely(() -> {
            Issue issue = selectedIssue();
            controller.resolveIssue(issue.getId(), selectedUser().getId(), commentArea.getText());
            commentArea.setText("");
            refreshIssues();
        }));
        JButton closed = new JButton("closed");
        closed.addActionListener(event -> runSafely(() -> {
            Issue issue = selectedIssue();
            controller.closeIssue(issue.getId(), selectedUser().getId(), commentArea.getText());
            commentArea.setText("");
            refreshIssues();
        }));
        JButton reopened = new JButton("reopened");
        reopened.addActionListener(event -> runSafely(() -> {
            Issue issue = selectedIssue();
            controller.reopenIssue(issue.getId(), selectedUser().getId(), commentArea.getText());
            commentArea.setText("");
            refreshIssues();
        }));
        JPanel buttons = new JPanel(new GridLayout(1, 4, 6, 6));
        buttons.add(fixed);
        buttons.add(resolved);
        buttons.add(closed);
        buttons.add(reopened);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("상태 변경"));
        panel.add(new JScrollPane(commentArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private void refreshIssues() {
        Project project = (Project) projectBox.getSelectedItem();
        currentIssues = controller.searchIssues(project == null ? null : project.getId(), null, null);
        tableModel.setRowCount(0);
        for (Issue issue : currentIssues) {
            tableModel.addRow(new Object[]{issue.getId(), issue.getTitle(), issue.getStatus(), nameOf(issue.getAssigneeId())});
        }
        if (!currentIssues.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void showSelectedIssue() {
        int index = table.getSelectedRow();
        if (index < 0 || index >= currentIssues.size()) {
            detailArea.setText("");
            return;
        }
        Issue issue = currentIssues.get(index);
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
                priority: %s
                status: %s

                댓글
                %s
                """.formatted(issue.getId(), issue.getTitle(), issue.getDescription(),
                nameOf(issue.getReporterId()), nameOf(issue.getAssigneeId()), nameOf(issue.getFixerId()),
                issue.getPriority(), issue.getStatus(), comments.isBlank() ? "(없음)" : comments));
    }

    private User selectedUser() {
        return (User) userBox.getSelectedItem();
    }

    private Issue selectedIssue() {
        int index = table.getSelectedRow();
        if (index < 0 || index >= currentIssues.size()) {
            throw new IllegalStateException("이슈를 선택하세요.");
        }
        return currentIssues.get(index);
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
            JOptionPane.showMessageDialog(null, exception.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
