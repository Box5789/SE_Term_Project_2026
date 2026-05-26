package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.ServiceFixture;
import kr.ac.se.issuemanager.model.Project;
import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagementServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void createUserSavesRoleAndRejectsDuplicateName() {
        ServiceFixture fixture = new ServiceFixture(tempDir);

        User user = fixture.userService.createUser("newDev", Role.DEV);

        assertEquals(Role.DEV, fixture.userService.findById(user.getId()).getRole());
        assertThrows(ServiceException.class, () -> fixture.userService.createUser("newDev", Role.TESTER));
    }

    @Test
    void createProjectSavesDescriptionAndRejectsDuplicateName() {
        ServiceFixture fixture = new ServiceFixture(tempDir);

        Project project = fixture.projectService.createProject("project2", "추가 프로젝트");

        assertEquals("추가 프로젝트", fixture.projectService.findById(project.getId()).getDescription());
        assertThrows(ServiceException.class, () -> fixture.projectService.createProject("project2", "중복"));
    }
}
