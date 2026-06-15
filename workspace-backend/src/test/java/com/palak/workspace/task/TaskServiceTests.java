package com.palak.workspace.task;

import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.notification.EmailService;
import com.palak.workspace.project.Project;
import com.palak.workspace.project.ProjectRepository;
import com.palak.workspace.project.ProjectStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.task.taskDTO.TaskResponseDTO;
import com.palak.workspace.task.taskDTO.UpdateTaskRequest;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import com.palak.workspace.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void updateTask_startedAtAutoSet() {

        Task task = new Task();
        task.setTaskCode("TASK001");
        task.setTenantId("TENANT001");
        task.setProjectCode("PROJ001");
        task.setStatus(TaskStatus.TODO);
        task.setAssignedToEmployeeId("EMP001");

        Project project = new Project();
        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setProjectManagerEmployeeId("MGR001");

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole()).thenReturn(UserRole.ORG_ADMIN);

        when(taskRepository.findByTaskCode("TASK001"))
                .thenReturn(Optional.of(task));

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(i -> i.getArgument(0));

        TaskResponseDTO response = taskService.updateTask("TASK001", request);

        assertEquals(
                TaskStatus.IN_PROGRESS,
                response.getStatus());

        assertNotNull(task.getStartedAt());

        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_completedAtAutoSet() {

        Task task = new Task();
        task.setTaskCode("TASK001");
        task.setTenantId("TENANT001");
        task.setProjectCode("PROJ001");
        task.setTitle("API Development");
        task.setStatus(TaskStatus.IN_PROGRESS);

        Project project = new Project();
        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setProjectManagerEmployeeId("MGR001");
        project.setProjectName("Workspace");

        User manager = new User();
        manager.setEmployeeId("MGR001");
        manager.setEmail("manager@test.com");

        UpdateTaskRequest request =
                new UpdateTaskRequest();

        request.setStatus(TaskStatus.DONE);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        when(taskRepository.findByTaskCode("TASK001"))
                .thenReturn(Optional.of(task));

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        when(userRepository.findByEmployeeId("MGR001"))
                .thenReturn(Optional.of(manager));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(i -> i.getArgument(0));

        TaskResponseDTO response =
                taskService.updateTask("TASK001", request);

        assertEquals(
                TaskStatus.DONE,
                response.getStatus());

        assertNotNull(task.getCompletedAt());

        verify(emailService)
                .sendTaskCompletedEmail(
                        "manager@test.com",
                        "API Development",
                        "Workspace");
    }

    @Test
    void updateTask_todoToDoneNotAllowed() {

        Task task = new Task();

        task.setTaskCode("TASK001");
        task.setTenantId("TENANT001");
        task.setProjectCode("PROJ001");
        task.setStatus(TaskStatus.TODO);

        Project project = new Project();
        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.IN_PROGRESS);

        UpdateTaskRequest request =
                new UpdateTaskRequest();

        request.setStatus(TaskStatus.DONE);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        when(taskRepository.findByTaskCode("TASK001"))
                .thenReturn(Optional.of(task));

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> taskService.updateTask(
                                "TASK001",
                                request)
                );

        assertEquals(
                "Task must be started before completion",
                exception.getMessage());

        verify(taskRepository, never())
                .save(any(Task.class));
    }

    @Test
    void deleteTask_success() {

        Task task = new Task();

        task.setTaskCode("TASK001");
        task.setTenantId("TENANT001");
        task.setProjectCode("PROJ001");
        task.setStatus(TaskStatus.TODO);

        Project project = new Project();

        project.setProjectCode("PROJ001");
        project.setTenantId("TENANT001");
        project.setStatus(ProjectStatus.IN_PROGRESS);

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        when(taskRepository.findByTaskCode("TASK001"))
                .thenReturn(Optional.of(task));

        when(projectRepository.findByProjectCode("PROJ001"))
                .thenReturn(Optional.of(project));

        taskService.deleteTask("TASK001");

        verify(taskRepository)
                .delete(task);
    }

}
