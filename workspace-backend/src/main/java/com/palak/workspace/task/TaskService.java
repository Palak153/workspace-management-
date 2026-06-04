package com.palak.workspace.task;

import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.project.Project;
import com.palak.workspace.project.ProjectRepository;
import com.palak.workspace.project.ProjectStatus;
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
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
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

    public Task createTask(Task task) {

        // Validate project
        Project project = projectRepository.findByProjectCode(task.getProjectCode()).orElse(null);
        if (project == null) {
            throw new RuntimeException("Invalid project code");
        }
        if(project.getStatus() == ProjectStatus.COMPLETED){
            throw new RuntimeException("Cannot create task in completed project");
        }

        // Validate tenant
        if (!project.getTenantId().equals(task.getTenantId())) {
            throw new RuntimeException("Project does not belong to tenant");
        }

        // Validate assignee
        User assignee = userRepository.findByEmployeeId(task.getAssignedToEmployeeId()).orElse(null);

        if (assignee == null) {
            throw new RuntimeException("Assigned user not found");
        }

        if (!assignee.getTenantId().equals(task.getTenantId())) {
            throw new RuntimeException("Assigned user does not belong to this organization");
        }

        if(!assignee.getIsActive()){
            throw new RuntimeException("Cannot assign task to inactive user");
        }

        // Assignee must belong to project
        if (!project.getMemberIds().contains(task.getAssignedToEmployeeId())) {
            throw new RuntimeException("Assigned user is not a member of the project");
        }

        // Validate creator
        User creator = userRepository.findByEmployeeId(task.getCreatedByEmployeeId()).orElse(null);

        if (creator == null) {
            throw new RuntimeException("Task creator not found");
        }

        if (!creator.getTenantId().equals(task.getTenantId())) {
            throw new RuntimeException("Task creator does not belong to this organization");
        }

        if(!creator.getIsActive()){
            throw new RuntimeException("Inactive user cannot create task");
        }

        if(creator.getRole() == UserRole.EMPLOYEE){
            throw new RuntimeException("Employee cannot create task");
        }

        if(!project.getMemberIds().contains(creator.getEmployeeId())) {
            throw new RuntimeException("Task creator is not a project member");
        }

        if(task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Due date cannot be in the past");
        }


        String taskCode = "TASK_" + UUID.randomUUID()
                        .toString()
                        .substring(0,6)
                        .toUpperCase();

        // System fields
        task.setTaskCode(taskCode);
        task.setStatus(TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        updateProjectProgress(savedTask.getProjectCode());
        return savedTask;
    }

    public Task findByTaskCode(String taskCode){
        return taskRepository.findByTaskCode(taskCode).orElse(null);
    }

    public List<Task> findByProjectCode(String projectCode){
        return taskRepository.findByProjectCode(projectCode);
    }

    public List<Task> findByTenantId(String tenantId){
        return taskRepository.findByTenantId(tenantId);
    }

    public List<Task> findByAssignedEmployee(String employeeId){
        return taskRepository.findByAssignedToEmployeeId(employeeId);
    }

    public List<Task> findByStatus(TaskStatus status){
        return taskRepository.findByStatus(status);
    }

    public List<Task> findByPriority(TaskPriority priority){
        return taskRepository.findByPriority(priority);
    }

    public List<Task> findByDueDate(LocalDate dueDate){
        return taskRepository.findByDueDate(dueDate);
    }

    public List<Task> findByProjectCodeAndStatus(String projectCode, TaskStatus status){
        return taskRepository.findByProjectCodeAndStatus(projectCode, status);
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

    public Task updateTask(String taskCode, Task newTask){

        Task oldTask = findByTaskCode(taskCode);
        if(oldTask == null){
            return null;
        }

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

            User assignee = userRepository.findByEmployeeId(newTask.getAssignedToEmployeeId()).orElse(null);

            if(assignee == null){
                throw new RuntimeException("Assigned user not found");
            }

            if(!assignee.getTenantId().equals(oldTask.getTenantId())){
                throw new RuntimeException("Assigned user does not belong to this organization");
            }

            Project project = projectRepository.findByProjectCode(oldTask.getProjectCode()).orElse(null);

            if(project != null && !project.getMemberIds().contains(newTask.getAssignedToEmployeeId())) {
                throw new RuntimeException("Assigned user is not a project member");
            }
            oldTask.setAssignedToEmployeeId(newTask.getAssignedToEmployeeId());
        }

        // Status handling
        if(newTask.getStatus() != null ){

            if(oldTask.getStatus() == TaskStatus.DONE && newTask.getStatus() != TaskStatus.DONE){
                throw new RuntimeException("Completed task cannot be moved back");
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
        return savedTask;
    }

    public Boolean deleteTask(String taskCode){
        Task task = findByTaskCode(taskCode);
        if(task != null){
            String projectCode = task.getProjectCode();
            taskRepository.delete(task);
            updateProjectProgress(projectCode);
            return true;
        }
        return false;
    }

}
