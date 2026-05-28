package kr.ac.se.issuemanager.ui.swing;

import kr.ac.se.issuemanager.controller.IssueManagementController;
import kr.ac.se.issuemanager.model.Issue;
import kr.ac.se.issuemanager.model.IssueStatus;
import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.Priority;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.service.Recommendation;
import kr.ac.se.issuemanager.service.ServiceException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwingMain {
    private final IssueManagementController controller = IssueManagementController.bootstrapDefault();
    
    private Map<String, User> usersById = controller.listUsers().stream()
            .collect(Collectors.toMap(User::getId, user -> user));
            
    private final JComboBox<User> userBox = new JComboBox<>();
    private final JComboBox<Project> projectBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "제목", "상태", "담당자"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextArea detailArea = new JTextArea();
    private final JTextArea commentArea = new JTextArea(4, 30);
    private List<Issue> currentIssues = List.of();


    private final JComboBox<IssueStatus> statusFilterBox = new JComboBox<>();
    private final JTextField keywordField = new JTextField(14);
    private final JTextArea statisticsArea = new JTextArea(8, 30);
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingMain().show());
    }
 
    private void show() {
        controller.listUsers().forEach(userBox::addItem);
        controller.listProjects().forEach(projectBox::addItem);
 
        JFrame frame = new JFrame("SE Term Project Issue Manager - Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.add(topPanel(),      BorderLayout.NORTH);
        frame.add(searchPanel(),   BorderLayout.WEST);   
        frame.add(detailPanel(),   BorderLayout.CENTER);
        frame.add(actionPanel(),   BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(event -> showSelectedIssue());
        refreshIssues();
        frame.setSize(1100, 680);
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
        
        JButton commentBtn  = new JButton("댓글 추가");
        commentBtn.addActionListener(event -> runSafely(this::addComment));
 
        JButton assignBtn   = new JButton("배정");
        assignBtn.addActionListener(event -> runSafely(this::assignIssue));
 
        JButton recommendBtn = new JButton("담당자 추천");
        recommendBtn.addActionListener(event -> runSafely(this::recommendAssignees));
 
        JPanel buttons = new JPanel(new GridLayout(2, 4, 6, 6));
        buttons.add(commentBtn);
        buttons.add(fixed);
        buttons.add(resolved);
        buttons.add(reopened);
        buttons.add(assignBtn);
        buttons.add(closed);
        buttons.add(recommendBtn);
 
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("댓글 / 상태 변경"));
        panel.add(new JScrollPane(commentArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }


    private JPanel searchPanel() {
        statusFilterBox.addItem(null);
        for (IssueStatus s : IssueStatus.values()) statusFilterBox.addItem(s);
        statusFilterBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                setText(value == null ? "── 전체 상태 ──" : value.toString());
                return this;
            }
        });
        JButton searchBtn = new JButton("검색");
        searchBtn.addActionListener(e -> refreshIssues());
        JButton resetBtn  = new JButton("초기화");
        resetBtn.addActionListener(e -> { statusFilterBox.setSelectedIndex(0); keywordField.setText(""); refreshIssues(); });
 
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        filterRow.add(new JLabel("상태:")); filterRow.add(statusFilterBox);
        filterRow.add(new JLabel("키워드:")); filterRow.add(keywordField);
        filterRow.add(searchBtn); filterRow.add(resetBtn);
 
        JScrollPane tableScroll = new JScrollPane(table);
 
        JTextField titleField      = new JTextField(12);
        JTextArea  descField       = new JTextArea(2, 12);
        JComboBox<Priority> prioBox = new JComboBox<>(Priority.values());
        prioBox.setSelectedItem(Priority.MAJOR);
        JButton registerBtn = new JButton("이슈 등록");
        registerBtn.addActionListener(e -> runSafely(() -> {
            Issue issue = controller.registerIssue(selectedProject().getId(), selectedUser().getId(),
                    titleField.getText(), descField.getText(), (Priority) prioBox.getSelectedItem());
            titleField.setText(""); descField.setText("");
            refreshIssues();
        }));
 
        JPanel registerForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        registerForm.setBorder(BorderFactory.createTitledBorder("신규 이슈 등록"));
        registerForm.add(new JLabel("제목:")); registerForm.add(titleField);
        registerForm.add(new JLabel("설명:")); registerForm.add(new JScrollPane(descField));
        registerForm.add(new JLabel("우선순위:")); registerForm.add(prioBox);
        registerForm.add(registerBtn);
 
        JComboBox<User> assigneeBox = new JComboBox<>();
        controller.listDevelopers().forEach(assigneeBox::addItem);
        this.sharedAssigneeBox = assigneeBox;
 
        JPanel assignRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        assignRow.setBorder(BorderFactory.createTitledBorder("배정 Dev 선택"));
        assignRow.add(new JLabel("담당 Dev:")); assignRow.add(assigneeBox);
 
        statisticsArea.setEditable(false);
        JPanel statPanel = new JPanel(new BorderLayout());
        statPanel.setBorder(BorderFactory.createTitledBorder("이슈 통계"));
        statPanel.add(new JScrollPane(statisticsArea), BorderLayout.CENTER);
 
        JPanel adminPanel = buildAdminPanel();
 
        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(filterRow,    BorderLayout.NORTH);
        top.add(tableScroll,  BorderLayout.CENTER);
        top.add(registerForm, BorderLayout.SOUTH);
 
        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.add(top,        BorderLayout.NORTH);
        left.add(assignRow,  BorderLayout.CENTER);
        left.add(statPanel,  BorderLayout.SOUTH);
 
        JPanel wrapper = new JPanel(new BorderLayout(4, 4));
        wrapper.add(left,       BorderLayout.CENTER);
        wrapper.add(adminPanel, BorderLayout.SOUTH);
        return wrapper;
    }
 
    private JPanel buildAdminPanel() {
        JTextField newUserNameField  = new JTextField(8);
        JComboBox<Role> newRoleBox   = new JComboBox<>(Role.values());
        JButton createUserBtn        = new JButton("계정 생성");
        createUserBtn.addActionListener(e -> runSafely(() -> {
            controller.createUser(selectedUser().getId(), newUserNameField.getText(),
                    (Role) newRoleBox.getSelectedItem());
            newUserNameField.setText("");
            usersById = controller.listUsers().stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            User cur = (User) userBox.getSelectedItem();
            userBox.removeAllItems();
            controller.listUsers().forEach(userBox::addItem);
            if (cur != null) for (int i = 0; i < userBox.getItemCount(); i++)
                if (userBox.getItemAt(i).getId().equals(cur.getId())) { userBox.setSelectedIndex(i); break; }
            if (sharedAssigneeBox != null) {
                User selA = (User) sharedAssigneeBox.getSelectedItem();
                sharedAssigneeBox.removeAllItems();
                controller.listDevelopers().forEach(sharedAssigneeBox::addItem);
                if (selA != null) for (int i = 0; i < sharedAssigneeBox.getItemCount(); i++)
                    if (sharedAssigneeBox.getItemAt(i).getId().equals(selA.getId())) { sharedAssigneeBox.setSelectedIndex(i); break; }
            }
            JOptionPane.showMessageDialog(null, "계정이 생성되었습니다.");
        }));
 
        JTextField newProjNameField  = new JTextField(8);
        JTextArea  newProjDescField  = new JTextArea(1, 10);
        JButton createProjBtn        = new JButton("프로젝트 생성");
        createProjBtn.addActionListener(e -> runSafely(() -> {
            Project p = controller.createProject(selectedUser().getId(),
                    newProjNameField.getText(), newProjDescField.getText());
            newProjNameField.setText(""); newProjDescField.setText("");
            projectBox.addItem(p);
            projectBox.setSelectedItem(p);
            refreshIssues();
            JOptionPane.showMessageDialog(null, "프로젝트가 생성되었습니다.");
        }));
 
        JPanel userForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        userForm.add(new JLabel("이름:")); userForm.add(newUserNameField);
        userForm.add(new JLabel("역할:")); userForm.add(newRoleBox);
        userForm.add(createUserBtn);
 
        JPanel projForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        projForm.add(new JLabel("이름:")); projForm.add(newProjNameField);
        projForm.add(new JLabel("설명:")); projForm.add(new JScrollPane(newProjDescField));
        projForm.add(createProjBtn);
 
        JPanel panel = new JPanel(new GridLayout(1, 2, 6, 0));
        panel.setBorder(BorderFactory.createTitledBorder("시스템 관리"));
        panel.add(userForm);
        panel.add(projForm);
        return panel;
    }
 
    private JComboBox<User> sharedAssigneeBox;
 
    private void addComment() {
        Issue issue = selectedIssue();
        controller.addComment(issue.getId(), selectedUser().getId(), commentArea.getText());
        commentArea.setText("");
        refreshIssues();
        showSelectedIssue();
    }
 
    private void assignIssue() {
        Issue issue = selectedIssue();
        if (sharedAssigneeBox == null || sharedAssigneeBox.getSelectedItem() == null)
            throw new ServiceException("배정할 Dev를 선택하세요.");
        User assignee = (User) sharedAssigneeBox.getSelectedItem();
        controller.assignIssue(issue.getId(), selectedUser().getId(), assignee.getId(), commentArea.getText());
        commentArea.setText("");
        refreshIssues();
    }
 
    private void recommendAssignees() {
        Issue issue = selectedIssue();
        List<Recommendation> recs = controller.recommendAssignees(issue.getId());
        if (recs.isEmpty()) {
            JOptionPane.showMessageDialog(null, "추천 후보가 없습니다.", "추천 결과", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String text = recs.stream()
                .map(r -> "• %s  (score: %.3f)".formatted(nameOf(r.devId()), r.score()))
                .collect(Collectors.joining("\n"));
        JOptionPane.showMessageDialog(null, text, "추천 담당자", JOptionPane.INFORMATION_MESSAGE);
    }



    private void refreshIssues() {
        Project project = (Project) projectBox.getSelectedItem();
        IssueStatus status = (IssueStatus) statusFilterBox.getSelectedItem();
        String keyword = keywordField.getText();
    
        currentIssues = controller.searchIssues(project == null ? null : project.getId(), status, keyword);
    
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

    private Project selectedProject() {
        return (Project) projectBox.getSelectedItem();
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