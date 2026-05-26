package kr.ac.se.issuemanager.controller;

import kr.ac.se.issuemanager.app.ApplicationContext;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IssueManagementControllerTest {
    @TempDir
    Path tempDir;

    @Test
    void adminCreatesUsersAndProjectsThroughController() {
        ApplicationContext context = new ApplicationContext(tempDir);
        context.userService().createUser("admin", Role.ADMIN);
        IssueManagementController controller = new IssueManagementController(context);

        User developer = controller.createUser("USER-001", "newDev", Role.DEV);
        controller.createProject("USER-001", "project-admin", "admin이 생성한 프로젝트");

        assertEquals(Role.DEV, context.userService().findById(developer.getId()).getRole());
        assertEquals(1, controller.listProjects().size());
    }

    @Test
    void nonAdminCannotCreateUsersThroughController() {
        ApplicationContext context = new ApplicationContext(tempDir);
        User tester = context.userService().createUser("tester", Role.TESTER);
        IssueManagementController controller = new IssueManagementController(context);

        assertThrows(ServiceException.class,
                () -> controller.createUser(tester.getId(), "blockedUser", Role.DEV));
    }
}
