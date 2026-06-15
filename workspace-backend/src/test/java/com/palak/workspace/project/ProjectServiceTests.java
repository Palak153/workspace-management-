package com.palak.workspace.project;

import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.project.projectDTO.CreateProjectRequest;
import com.palak.workspace.project.projectDTO.ProjectResponseDTO;
import com.palak.workspace.project.projectDTO.UpdateProjectRequest;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import com.palak.workspace.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTests {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_success() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Workspace");
        request.setDescription("Project Management System");
        request.setProjectManagerEmployeeId("MGR001");
        request.setMemberIds(List.of("MGR001", "EMP001"));

        Organization organization = new Organization();

        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        when(organizationRepository.findByTenantId("TENANT001"))
                .thenReturn(Optional.of(organization));

        User manager = new User();

        manager.setEmployeeId("MGR001");
        manager.setRole(UserRole.MANAGER);
        manager.setTenantId("TENANT001");
        manager.setIsActive(true);

        when(userRepository.findByEmployeeId("MGR001"))
                .thenReturn(Optional.of(manager));

        User employee =
                new User();

        employee.setEmployeeId("EMP001");
        employee.setTenantId("TENANT001");
        employee.setIsActive(true);

        when(userRepository
                .findByEmployeeId("EMP001"))
                .thenReturn(Optional.of(employee));

        doNothing()
                .when(securityUtil)
                .validateActiveUser();

        when(securityUtil
                .getCurrentTenantId())
                .thenReturn("TENANT001");
        Project savedProject =
                new Project();

        savedProject.setProjectName("Workspace");
        savedProject.setProjectCode("PROJ123");
        savedProject.setProjectManagerEmployeeId("MGR001");
        savedProject.setMemberIds(
                List.of("MGR001", "EMP001"));
        savedProject.setStatus(ProjectStatus.PLANNED);
        savedProject.setProgressPercentage(0);

        when(projectRepository.save(any(Project.class)))
                .thenReturn(savedProject);
        ProjectResponseDTO response = projectService.createProject(request);
        assertNotNull(response);

        assertEquals(
                "Workspace",
                response.getProjectName());

        assertEquals(
                "PROJ123",
                response.getProjectCode());

        assertEquals(
                ProjectStatus.PLANNED,
                response.getStatus());

        assertEquals(0, response.getProgressPercentage());

        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_managerNotInMembers() {

        CreateProjectRequest request =
                new CreateProjectRequest();

        request.setProjectName("Workspace");
        request.setDescription("Project Management System");
        request.setProjectManagerEmployeeId("MGR001");

        // Manager missing intentionally
        request.setMemberIds(
                List.of("EMP001", "EMP002"));

        Organization organization =
                new Organization();

        organization.setTenantId("TENANT001");
        organization.setStatus(
                OrganizationStatus.ACTIVE);

        when(organizationRepository
                .findByTenantId("TENANT001"))
                .thenReturn(Optional.of(organization));

        User manager =
                new User();

        manager.setEmployeeId("MGR001");
        manager.setRole(UserRole.MANAGER);
        manager.setTenantId("TENANT001");
        manager.setIsActive(true);

        when(userRepository
                .findByEmployeeId("MGR001"))
                .thenReturn(Optional.of(manager));

        User employee1 =
                new User();

        employee1.setEmployeeId("EMP001");
        employee1.setTenantId("TENANT001");
        employee1.setIsActive(true);

        when(userRepository
                .findByEmployeeId("EMP001"))
                .thenReturn(Optional.of(employee1));

        User employee2 =
                new User();

        employee2.setEmployeeId("EMP002");
        employee2.setTenantId("TENANT001");
        employee2.setIsActive(true);

        when(userRepository
                .findByEmployeeId("EMP002"))
                .thenReturn(Optional.of(employee2));

        doNothing()
                .when(securityUtil)
                .validateActiveUser();

        when(securityUtil
                .getCurrentTenantId())
                .thenReturn("TENANT001");

        ValidationException exception = assertThrows(
                        ValidationException.class,
                        () -> projectService.createProject(request)
                );

        assertEquals(
                "Project manager must be part of project members",
                exception.getMessage());

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void updateProject_startDateAutoSet() {

        Project project = new Project();

        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.PLANNED);
        project.setProjectManagerEmployeeId("MGR001");
        project.setMemberIds(List.of("MGR001", "EMP001"));

        Organization organization = new Organization();
        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        UpdateProjectRequest request = new UpdateProjectRequest();

        request.setStatus(ProjectStatus.IN_PROGRESS);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole()).thenReturn(UserRole.ORG_ADMIN);

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        when(organizationRepository.findByTenantId("TENANT001"))
                .thenReturn(Optional.of(organization));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponseDTO response = projectService.updateProject("PROJ001", request);

        assertEquals(
                ProjectStatus.IN_PROGRESS,
                response.getStatus());

        assertNotNull(project.getStartDate());

        verify(projectRepository)
                .save(project);
    }

    @Test
    void updateProject_endDateAutoSet() {

        Project project = new Project();

        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setProjectManagerEmployeeId("MGR001");
        project.setMemberIds(List.of("MGR001", "EMP001"));

        Organization organization = new Organization();
        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        UpdateProjectRequest request =
                new UpdateProjectRequest();

        request.setStatus(ProjectStatus.COMPLETED);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        when(organizationRepository.findByTenantId("TENANT001"))
                .thenReturn(Optional.of(organization));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponseDTO response =
                projectService.updateProject(
                        "PROJ001",
                        request);

        assertEquals(
                ProjectStatus.COMPLETED,
                response.getStatus());

        assertNotNull(project.getEndDate());

        verify(projectRepository)
                .save(project);
    }

    @Test
    void cancelProject_success() {

        Project project = new Project();

        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setProjectManagerEmployeeId("MGR001");

        Organization organization = new Organization();

        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        when(organizationRepository.findByTenantId("TENANT001"))
                .thenReturn(Optional.of(organization));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponseDTO response =
                projectService.cancelProject("PROJ001");

        assertEquals(
                ProjectStatus.CANCELLED,
                response.getStatus());

        verify(projectRepository)
                .save(project);
    }
}
