package com.palak.workspace.dashboard;

import com.palak.workspace.exception.ValidationException;
import com.palak.workspace.organization.OrganizationService;
import com.palak.workspace.project.Project;
import com.palak.workspace.project.ProjectRepository;
import com.palak.workspace.project.ProjectStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.task.Task;
import com.palak.workspace.task.TaskRepository;
import com.palak.workspace.task.TaskStatus;
import com.palak.workspace.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtil securityUtil;
    private final OrganizationService organizationService;

    public DashboardService(ProjectRepository projectRepository, TaskRepository taskRepository, SecurityUtil securityUtil, OrganizationService organizationService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.securityUtil = securityUtil;
        this.organizationService = organizationService;
    }

    private DashboardSummaryDTO getSuperAdminSummary(String tenantId){
        if(tenantId == null){
            throw new ValidationException("tenantId is required");
        }
        organizationService.findOrganizationByTenantID(tenantId);
        return getOrganizationSummary(tenantId);
    }

    private DashboardSummaryDTO getOrganizationSummary(String tenantId){
        List<Project> projects = projectRepository.findByTenantId(tenantId);
        List<Task> tasks = taskRepository.findByTenantId(tenantId);
        return buildSummary(projects,tasks);
    }

    private DashboardSummaryDTO getManagerSummary(String employeeId){
        List<Project> projects = projectRepository.findByProjectManagerEmployeeId(employeeId);

        Set<String> projectCodes = new HashSet<>();
        for(Project project : projects){
            projectCodes.add(project.getProjectCode());
        }
        List<Task> tasks = taskRepository.findByProjectCodeIn(new ArrayList<>(projectCodes));
        return buildSummary(projects,tasks);
    }

    private DashboardSummaryDTO getEmployeeSummary(String employeeId){
        List<Project> projects = projectRepository.findByTenantId(securityUtil.getCurrentTenantId());
        projects = projects.stream().filter(project ->
                project.getMemberIds() != null && project.getMemberIds().contains(employeeId)).toList();
        List<Task> tasks = taskRepository.findByAssignedToEmployeeId(employeeId);
        return buildSummary(projects,tasks);
    }

    private DashboardSummaryDTO buildSummary(List<Project> projects, List<Task> tasks){

        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        dto.setTotalProjects(projects.size());

        dto.setPlannedProjects(projects.stream().filter
                (p -> p.getStatus() == ProjectStatus.PLANNED).count());

        dto.setActiveProjects(projects.stream().filter
                (p -> p.getStatus() == ProjectStatus.IN_PROGRESS).count());

        dto.setCompletedProjects(projects.stream().filter
                (p -> p.getStatus() == ProjectStatus.COMPLETED).count());

        dto.setCancelledProjects(projects.stream().filter
                (p -> p.getStatus() == ProjectStatus.CANCELLED).count());

        dto.setTotalTasks(tasks.size());

        dto.setTodoTasks(tasks.stream().filter
                (t -> t.getStatus() == TaskStatus.TODO).count());

        dto.setInProgressTasks(tasks.stream().filter
                (t -> t.getStatus() == TaskStatus.IN_PROGRESS).count());

        dto.setDoneTasks(tasks.stream().filter
                (t -> t.getStatus() == TaskStatus.DONE).count());

        dto.setOverdueTasks(tasks.stream().filter
                (t -> t.getStatus() != TaskStatus.DONE && t.getDueDate() != null
                        && t.getDueDate().isBefore(LocalDate.now())).count());
        return dto;
    }

    public DashboardSummaryDTO getSummary(String tenantId) {

        securityUtil.validateActiveUser();
        UserRole role = securityUtil.getCurrentUserRole();

        if(role == UserRole.SUPER_ADMIN){
            return getSuperAdminSummary(tenantId);
        }

        if(role == UserRole.ORG_ADMIN){
            return getOrganizationSummary(securityUtil.getCurrentTenantId());
        }

        if(role == UserRole.MANAGER){
            return getManagerSummary(securityUtil.getCurrentEmployeeId());
        }

        return getEmployeeSummary(securityUtil.getCurrentEmployeeId());
    }

    public List<ProjectProgressDTO> getProjectProgress(String tenantId){
        securityUtil.validateActiveUser();
        UserRole role = securityUtil.getCurrentUserRole();

        if(role == UserRole.SUPER_ADMIN){

            if(tenantId == null){
                throw new ValidationException(
                        "tenantId is required");
            }
            organizationService.findOrganizationByTenantID(tenantId);
            return getOrganizationProjectProgress(tenantId);
        }

        if(role == UserRole.ORG_ADMIN){
            return getOrganizationProjectProgress(securityUtil.getCurrentTenantId());
        }

        if(role == UserRole.MANAGER){
            return getManagerProjectProgress(securityUtil.getCurrentEmployeeId());
        }

        return getEmployeeProjectProgress(securityUtil.getCurrentEmployeeId());
    }

    private ProjectProgressDTO convertToProjectProgressDTO(Project project){
        ProjectProgressDTO dto = new ProjectProgressDTO();

        dto.setProjectCode(project.getProjectCode());

        dto.setProjectName(project.getProjectName());

        dto.setProgressPercentage(project.getProgressPercentage());

        dto.setStatus(project.getStatus());

        return dto;
    }

    private List<ProjectProgressDTO> convertToProjectProgressDTOList(List<Project> projects){

        List<ProjectProgressDTO> response = new ArrayList<>();

        for(Project project : projects){
            response.add(convertToProjectProgressDTO(project));
        }
        return response;
    }

    private List<ProjectProgressDTO> getOrganizationProjectProgress(String tenantId){
        List<Project> projects = projectRepository.findByTenantId(tenantId);
        return convertToProjectProgressDTOList(projects);
    }

    private List<ProjectProgressDTO> getManagerProjectProgress(String employeeId){
        List<Project> projects = projectRepository.findByProjectManagerEmployeeId(employeeId);
        return convertToProjectProgressDTOList(projects);
    }

    private List<ProjectProgressDTO> getEmployeeProjectProgress(String employeeId){
        List<Project> projects = projectRepository.findByTenantId(securityUtil.getCurrentTenantId());
        projects = projects.stream().filter
                        (project -> project.getMemberIds() != null
                                && project.getMemberIds().contains(employeeId)).toList();
        return convertToProjectProgressDTOList(projects);
    }

}
