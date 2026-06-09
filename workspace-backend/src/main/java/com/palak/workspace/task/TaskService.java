package com.palak.workspace.task;

import com.palak.workspace.common.DTO.PageResponseDTO;
import com.palak.workspace.common.exception.ResourceNotFoundException;
import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.project.Project;
import com.palak.workspace.project.ProjectRepository;
import com.palak.workspace.project.ProjectStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.task.taskDTO.CreateTaskRequest;
import com.palak.workspace.task.taskDTO.TaskResponseDTO;
import com.palak.workspace.task.taskDTO.UpdateTaskRequest;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import com.palak.workspace.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "dueDate",
            "priority",
            "status",
            "title"
    );

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {

        if(page < 0){
            throw new ValidationException("Page number cannot be negative");
        }

        if(size < 1 || size > 100){
            throw new ValidationException("Page size must be between 1 and 100");
        }

        if(!ALLOWED_SORT_FIELDS.contains(sortBy)){
            throw new ValidationException("Invalid sort field");
        }

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    public PageResponseDTO<TaskResponseDTO> getTasks(int page, int size, String sortBy, String direction) {

        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Task> taskPage;
        if (securityUtil.getCurrentUserRole() == UserRole.EMPLOYEE) {
            taskPage = taskRepository.findByAssignedToEmployeeId(securityUtil.getCurrentEmployeeId(), pageable);
        } else {
            taskPage = taskRepository.findByTenantId(tenantId, pageable);
        }

        PageResponseDTO<TaskResponseDTO> response = new PageResponseDTO<>();

        response.setContent(convertToDTOList(taskPage.getContent()));

        response.setPage(taskPage.getNumber());

        response.setSize(taskPage.getSize());

        response.setTotalElements(taskPage.getTotalElements());

        response.setTotalPages(taskPage.getTotalPages());

        response.setLast(taskPage.isLast());

        return response;
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
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

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

    private void validateTaskModificationAccess(Project project){
        UserRole role = securityUtil.getCurrentUserRole();
        if(role == UserRole.MANAGER){
            if(!project.getProjectManagerEmployeeId().equals(securityUtil.getCurrentEmployeeId())){
                throw new ValidationException("Managers can only modify tasks in projects they manage");
            }
        }
    }

    private Project findProjectByCode(String projectCode){
        return projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    public TaskResponseDTO createTask(CreateTaskRequest request) {

        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        String creatorId = securityUtil.getCurrentEmployeeId();

        // Validate project
        Project project = validateProject(request.getProjectCode(),tenantId);

        // Validate assignee
        validateTaskAssignee(request.getAssignedToEmployeeId(), tenantId, project);

        // Validate creator
        validateTaskCreator(creatorId, tenantId, project);

        String taskCode = "TASK_" + UUID.randomUUID()
                        .toString()
                        .substring(0,6)
                        .toUpperCase();

        Task task = new Task();
        task.setProjectCode(request.getProjectCode());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedToEmployeeId(request.getAssignedToEmployeeId());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
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
        Project project = findProjectByCode(projectCode);

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
        List<Task> tasks = taskRepository.findByStatus(status);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByPriority(TaskPriority priority){

        securityUtil.validateActiveUser();
        List<Task> tasks = taskRepository.findByPriority(priority);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByDueDate(LocalDate dueDate){

        securityUtil.validateActiveUser();
        List<Task> tasks = taskRepository.findByDueDate(dueDate);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    public List<TaskResponseDTO> getTasksByProjectAndStatus(String projectCode, TaskStatus status){

        securityUtil.validateActiveUser();
        Project project = findProjectByCode(projectCode);

        securityUtil.validateTenantAccess(project.getTenantId());
        List<Task> tasks = taskRepository.findByProjectCodeAndStatus(projectCode, status);
        tasks = filterTasksForCurrentUser(tasks);
        return convertToDTOList(tasks);
    }

    private void updateProjectProgress(String projectCode){

        long totalTasks = taskRepository.countByProjectCode(projectCode);
        long completedTasks = taskRepository.countByProjectCodeAndStatus(projectCode, TaskStatus.DONE);

        int progress = totalTasks == 0 ? 0 : (int)((completedTasks * 100) / totalTasks);

        Project project = findProjectByCode(projectCode);

        project.setProgressPercentage(progress);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    public TaskResponseDTO updateTask(String taskCode, UpdateTaskRequest newTask){
        securityUtil.validateActiveUser();
        Task oldTask = findByTaskCode(taskCode);
        securityUtil.validateTenantAccess(oldTask.getTenantId());

        if(oldTask.getStatus() == TaskStatus.DONE){
            throw new ValidationException("Completed task cannot be modified");
        }

        Project project = validateProject(oldTask.getProjectCode(), oldTask.getTenantId());

        validateTaskModificationAccess(project);

        oldTask.setTitle(
                newTask.getTitle() != null && !newTask.getTitle().isBlank()
                        ? newTask.getTitle() : oldTask.getTitle());

        oldTask.setDescription(
                newTask.getDescription() != null && !newTask.getDescription().isBlank()
                        ? newTask.getDescription() : oldTask.getDescription());

        oldTask.setPriority(
                newTask.getPriority() != null ? newTask.getPriority() : oldTask.getPriority());

        oldTask.setDueDate(
                newTask.getDueDate() != null ? newTask.getDueDate() : oldTask.getDueDate());

        if(newTask.getAssignedToEmployeeId() != null){
            validateTaskAssignee(newTask.getAssignedToEmployeeId(), oldTask.getTenantId(), project);
            oldTask.setAssignedToEmployeeId(newTask.getAssignedToEmployeeId());
        }

        // Status handling
        if(newTask.getStatus() != null ){

            if(oldTask.getStatus() == TaskStatus.TODO && newTask.getStatus() == TaskStatus.DONE){
                throw new ValidationException("Task must be started before completion");
            }

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

        Project project = findProjectByCode(projectCode);
        validateTaskModificationAccess(project);

        taskRepository.delete(task);
        updateProjectProgress(projectCode);
    }
}
