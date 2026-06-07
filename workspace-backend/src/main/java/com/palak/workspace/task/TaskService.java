package com.palak.workspace.task;

import com.palak.workspace.exception.ResourceNotFoundException;
import com.palak.workspace.exception.ValidationException;
import com.palak.workspace.project.Project;
import com.palak.workspace.project.ProjectRepository;
import com.palak.workspace.project.ProjectStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import com.palak.workspace.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, SecurityUtil securityUtil) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    private void validateDueDate(LocalDate dueDate){

        if(dueDate != null && dueDate.isBefore(LocalDate.now())){
            throw new ValidationException("Due date cannot be in the past");
        }
    }

    public TaskResponseDTO convertToDTO(Task task){
        TaskResponseDTO response = new TaskResponseDTO();
        response.setTaskCode(task.getTaskCode());
        response.setProjectCode(task.getProjectCode());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setAssignedToEmployeeId(task.getAssignedToEmployeeId());
        response.setCreatedByEmployeeId(task.getCreatedByEmployeeId());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());
        return response;
    }

    public List<TaskResponseDTO> convertToDTOList(List<Task> tasks){
        List<TaskResponseDTO> responseList = new ArrayList<>();
        for(Task task : tasks){
            responseList.add(convertToDTO(task));
        }
        return responseList;
    }

    private List<Task> filterTasksForCurrentUser(List<Task> tasks){
        if(securityUtil.getCurrentUserRole() == UserRole.EMPLOYEE){
            tasks = tasks.stream().filter(task ->
                    task.getAssignedToEmployeeId()
                            .equals(securityUtil.getCurrentEmployeeId())).toList();
        }
        return tasks;
    }

    private Project validateProject(String projectCode, String tenantId){
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new ValidationException("Invalid project code"));

        if(project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.CANCELLED){
            throw new ValidationException("Cannot create task in completed or cancelled project");
        }

        // Validate tenant
        if (!project.getTenantId().equals(tenantId)) {
            throw new ValidationException("Project does not belong to tenant");
        }
        return project;
    }

    private void validateTaskAssignee(String employeeId, String tenantId, Project project){
        User assignee = userRepository.findByEmployeeId(employeeId).orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));

        if (!assignee.getTenantId().equals(tenantId)) {
            throw new ValidationException("Assigned user does not belong to this organization");
        }

        if(!assignee.getIsActive()){
            throw new ValidationException("Cannot assign task to inactive user");
        }

        // Assignee must belong to project
        if (!project.getMemberIds().contains(employeeId)) {
            throw new ValidationException("Assigned user is not a member of the project");
        }
    }

    private void validateTaskCreator(String creatorId, String tenantId, Project project){
        User creator = userRepository.findByEmployeeId(creatorId).orElseThrow(() -> new ResourceNotFoundException("Task creator not found"));

        if (!creator.getTenantId().equals(tenantId)) {
            throw new ValidationException("Task creator does not belong to this organization");
        }

        if(!creator.getIsActive()){
            throw new ValidationException("Inactive user cannot create task");
        }

        if(creator.getRole() == UserRole.EMPLOYEE){
            throw new ValidationException("Employee cannot create task");
        }

        if(creator.getRole() == UserRole.MANAGER && !project.getProjectManagerEmployeeId().equals(creatorId)){
            throw new ValidationException("Manager can only create tasks in projects they manage");
        }
    }

    public TaskResponseDTO createTask(Task task) {

        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        String creatorId = securityUtil.getCurrentEmployeeId();

        // Validate project
        Project project = validateProject(task.getProjectCode(),tenantId);

        // Validate assignee
        validateTaskAssignee(task.getAssignedToEmployeeId(), tenantId, project);

        // Validate creator
        validateTaskCreator(creatorId, tenantId, project);

        validateDueDate(task.getDueDate());

        String taskCode = "TASK_" + UUID.randomUUID()
                        .toString()
                        .substring(0,6)
                        .toUpperCase();

        // System fields
        task.setTenantId(tenantId);
        task.setCreatedByEmployeeId(creatorId);
        task.setTaskCode(taskCode);
        task.setStatus(TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        updateProjectProgress(savedTask.getProjectCode());
        return convertToDTO(savedTask);
    }

    public Task findByTaskCode(String taskCode){
        return taskRepository.findByTaskCode(taskCode).orElseThrow(() ->
                new ResourceNotFoundException("Task not found"));
    }
    public TaskResponseDTO getTaskByCode(String taskCode){
        securityUtil.validateActiveUser();
        Task task = findByTaskCode(taskCode);
        securityUtil.validateTenantAccess(task.getTenantId());

        if(securityUtil.getCurrentUserRole() == UserRole.EMPLOYEE){
            String employeeId = securityUtil.getCurrentEmployeeId();
            if(!task.getAssignedToEmployeeId().equals(employeeId)){
                throw new ValidationException("Employees can only view their own tasks");
            }
        }
        return convertToDTO(task);
    }

    public List<TaskResponseDTO> getTasksByProject(String projectCode){
        securityUtil.validateActiveUser();
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        securityUtil.validateTenantAccess(project.getTenantId());

        List<Task> tasks = taskRepository.findByProjectCode(projectCode);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getMyOrganizationTasks(){
        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        List<Task> tasks = taskRepository.findByTenantId(tenantId);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByAssignee(String employeeId){
        securityUtil.validateActiveUser();
        User assignee = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        securityUtil.validateTenantAccess(assignee.getTenantId());
        List<Task> tasks = taskRepository.findByAssignedToEmployeeId(employeeId);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status){
        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();

        List<Task> tasks = taskRepository.findByTenantId(tenantId)
                .stream().filter(task -> task.getStatus() == status).toList();

        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByPriority(TaskPriority priority){

        securityUtil.validateActiveUser();

        String tenantId = securityUtil.getCurrentTenantId();

        List<Task> tasks = taskRepository.findByTenantId(tenantId).stream()
                .filter(task -> task.getPriority() == priority).toList();
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByDueDate(LocalDate dueDate){

        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        List<Task> tasks = taskRepository
                .findByTenantId(tenantId)
                .stream()
                .filter(task ->
                        dueDate.equals(task.getDueDate()))
                .toList();

        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByProjectAndStatus(String projectCode, TaskStatus status){

        securityUtil.validateActiveUser();
        Project project = projectRepository
                .findByProjectCode(projectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        securityUtil.validateTenantAccess(project.getTenantId());
        List<Task> tasks = taskRepository.findByProjectCodeAndStatus(projectCode, status);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    private void updateProjectProgress(String projectCode){

        long totalTasks = taskRepository.countByProjectCode(projectCode);
        long completedTasks = taskRepository.countByProjectCodeAndStatus(projectCode, TaskStatus.DONE);

        int progress = totalTasks == 0 ? 0 : (int)((completedTasks * 100) / totalTasks);

        Project project = projectRepository.findByProjectCode(projectCode).orElse(null);

        if(project != null){
            project.setProgressPercentage(progress);
            project.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project);
        }
    }

    public TaskResponseDTO updateTask(String taskCode, Task newTask){
        securityUtil.validateActiveUser();
        Task oldTask = findByTaskCode(taskCode);
        securityUtil.validateTenantAccess(oldTask.getTenantId());

        if(oldTask.getStatus() == TaskStatus.DONE){
            throw new ValidationException("Completed task cannot be modified");
        }

        Project project = validateProject(oldTask.getProjectCode(), oldTask.getTenantId());

        oldTask.setTitle(
                newTask.getTitle() != null && !newTask.getTitle().isBlank()
                        ? newTask.getTitle() : oldTask.getTitle());

        oldTask.setDescription(
                newTask.getDescription() != null && !newTask.getDescription().isBlank()
                        ? newTask.getDescription() : oldTask.getDescription());

        oldTask.setPriority(
                newTask.getPriority() != null ? newTask.getPriority() : oldTask.getPriority());

        validateDueDate(newTask.getDueDate());

        oldTask.setDueDate(
                newTask.getDueDate() != null ? newTask.getDueDate() : oldTask.getDueDate());

        if(newTask.getAssignedToEmployeeId() != null){
            validateTaskAssignee(newTask.getAssignedToEmployeeId(), oldTask.getTenantId(), project);
            oldTask.setAssignedToEmployeeId(newTask.getAssignedToEmployeeId());
        }

        // Status handling
        if(newTask.getStatus() != null ){

            if(newTask.getStatus() == TaskStatus.IN_PROGRESS && oldTask.getStartedAt() == null){
                oldTask.setStartedAt(LocalDateTime.now());
            }

            if(newTask.getStatus() == TaskStatus.DONE && oldTask.getCompletedAt() == null){
                oldTask.setCompletedAt(LocalDateTime.now());
            }

            oldTask.setStatus(newTask.getStatus());
        }

        oldTask.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(oldTask);
        updateProjectProgress(savedTask.getProjectCode());
        return convertToDTO(savedTask);
    }

    public void deleteTask(String taskCode){

        securityUtil.validateActiveUser();
        Task task = findByTaskCode(taskCode);
        securityUtil.validateTenantAccess(task.getTenantId());
        if(task.getStatus() == TaskStatus.DONE){
            throw new ValidationException("Completed tasks cannot be deleted");
        }
        String projectCode = task.getProjectCode();
        taskRepository.delete(task);
        updateProjectProgress(projectCode);
    }
}
