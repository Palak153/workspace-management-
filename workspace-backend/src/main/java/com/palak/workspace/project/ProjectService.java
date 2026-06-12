package com.palak.workspace.project;

import com.palak.workspace.common.exception.AccessDeniedException;
import com.palak.workspace.common.exception.ResourceNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    public ProjectService(ProjectRepository projectRepository, OrganizationRepository organizationRepository, UserRepository userRepository, SecurityUtil securityUtil) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public ProjectResponseDTO convertToDTO(Project project){
        ProjectResponseDTO response = new ProjectResponseDTO();
        response.setProjectName(project.getProjectName());
        response.setProjectCode(project.getProjectCode());
        response.setDescription(project.getDescription());
        response.setProjectManagerEmployeeId(project.getProjectManagerEmployeeId());
        response.setMemberIds(project.getMemberIds());
        response.setStatus(project.getStatus());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setProgressPercentage(project.getProgressPercentage());
        return response;
    }

    public List<ProjectResponseDTO> convertToDTOList(List<Project> projects){
        List<ProjectResponseDTO> responseList = new ArrayList<>();
        for(Project project : projects){
            responseList.add(convertToDTO(project));
        }
        return responseList;
    }

    private void validateProjectOwnership(Project project){
        if(securityUtil.getCurrentUserRole() == UserRole.MANAGER){
            if(!project.getProjectManagerEmployeeId().equals(securityUtil.getCurrentEmployeeId())){
                throw new AccessDeniedException("You can only access your own projects");
            }
        }
    }

    private void validateActiveOrganization(String tenantId){

        Organization organization = organizationRepository
                .findByTenantId(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Organization not found"));

        if(organization.getStatus() == OrganizationStatus.INACTIVE){
            throw new ValidationException("Organization is inactive");
        }
    }

    private void validateProjectManager(String employeeId, String tenantId){

        User manager = userRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project manager not found"));

        if(!manager.getIsActive()){
            throw new ValidationException("Project manager is inactive");
        }

        if (manager.getRole() != UserRole.MANAGER && manager.getRole() != UserRole.ORG_ADMIN) {
            throw new ValidationException("Selected user cannot be a manager");
        }

        if (!manager.getTenantId().equals(tenantId)) {
            throw new ValidationException("Manager does not belong to this organization");
        }
    }

    private void validateProjectMembers(List<String> memberIds, String tenantId) {

        if(memberIds == null){
            return;
        }
        for(String memberId : memberIds){

            User member = userRepository.findByEmployeeId(memberId)
                    .orElseThrow(() -> new ValidationException("Invalid member id : " + memberId));

            if(!member.getTenantId().equals(tenantId)){
                throw new ValidationException("Member does not belong to this organization");
            }

            if(!member.getIsActive()){
                throw new ValidationException("Inactive member cannot be assigned to project");
            }
        }
    }

    public ProjectResponseDTO  createProject(CreateProjectRequest request) {
        securityUtil.validateActiveUser();

        String tenantId = securityUtil.getCurrentTenantId();

        // Validate tenant
        validateActiveOrganization(tenantId);

        // Validate manager
        validateProjectManager(request.getProjectManagerEmployeeId(), tenantId);

        // Validate members
        validateProjectMembers(request.getMemberIds(),tenantId);

        if(request.getMemberIds() == null || !request.getMemberIds().contains(request.getProjectManagerEmployeeId())){
            throw new ValidationException("Project manager must be part of project members");
        }

        String projectCode = "PROJ_" + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        Project project = new Project();

        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setProjectManagerEmployeeId(request.getProjectManagerEmployeeId());
        project.setMemberIds(request.getMemberIds());
        project.setTenantId(tenantId);
        project.setProjectCode(projectCode);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus(ProjectStatus.PLANNED);
        project.setProgressPercentage(0);
        Project savedProject = projectRepository.save(project);
        log.info(
                "Project created: {}",
                savedProject.getProjectCode()
        );

        return convertToDTO(savedProject);
    }

    @CacheEvict(value = "projectsByCode", allEntries = true)
    public ProjectResponseDTO updateProject(String projectCode, UpdateProjectRequest newProject){

        securityUtil.validateActiveUser();
        Project oldProject = findByProjectCode(projectCode);
        securityUtil.validateTenantAccess(oldProject.getTenantId());
        validateProjectOwnership(oldProject);
        validateActiveOrganization(oldProject.getTenantId());

        if(oldProject.getStatus() == ProjectStatus.COMPLETED){
            throw new ValidationException("Completed project cannot be modified");
        }
        if(oldProject.getStatus() == ProjectStatus.CANCELLED){
            throw new ValidationException("Cancelled project cannot be modified");
        }

        if(newProject.getStatus() != null){

            ProjectStatus currentStatus = oldProject.getStatus();
            ProjectStatus newStatus = newProject.getStatus();

            if(currentStatus == ProjectStatus.PLANNED && newStatus == ProjectStatus.IN_PROGRESS){
                if(oldProject.getStartDate() == null){
                    oldProject.setStartDate(LocalDate.now());
                }
            }

            if(currentStatus == ProjectStatus.PLANNED && newStatus == ProjectStatus.COMPLETED){
                throw new ValidationException("Project must be started before completion");
            }

            if(newStatus == ProjectStatus.COMPLETED){
                if(oldProject.getEndDate() == null){
                    oldProject.setEndDate(LocalDate.now());
                }
            }

            oldProject.setStatus(newStatus);
        }

        if(newProject.getProjectManagerEmployeeId() != null && !newProject.getProjectManagerEmployeeId().isBlank()){
            validateProjectManager(newProject.getProjectManagerEmployeeId(), oldProject.getTenantId());
            oldProject.setProjectManagerEmployeeId(newProject.getProjectManagerEmployeeId());
        }

        if(newProject.getMemberIds() != null){
            validateProjectMembers(newProject.getMemberIds(), oldProject.getTenantId());
            oldProject.setMemberIds(newProject.getMemberIds());
        }

        oldProject.setProjectName(
                newProject.getProjectName() != null && !newProject.getProjectName().isBlank()
                        ? newProject.getProjectName() : oldProject.getProjectName());

        oldProject.setDescription(
                newProject.getDescription() != null && !newProject.getDescription().isBlank()
                        ? newProject.getDescription() : oldProject.getDescription());

        String managerId = newProject.getProjectManagerEmployeeId() != null
                        ? newProject.getProjectManagerEmployeeId()
                        : oldProject.getProjectManagerEmployeeId();

        List<String> members = newProject.getMemberIds() != null
                        ? newProject.getMemberIds()
                        : oldProject.getMemberIds();

        if(members == null || !members.contains(managerId)){
            throw new ValidationException("Project manager must be part of project members");
        }

        oldProject.setUpdatedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(oldProject);
        log.info(
                "Project updated: {}",
                projectCode
        );

        return convertToDTO(updatedProject);
    }

    @CacheEvict(value = "projectsByCode", allEntries = true)
    public ProjectResponseDTO cancelProject(String projectCode){

        securityUtil.validateActiveUser();
        Project project = findByProjectCode(projectCode);
        securityUtil.validateTenantAccess(project.getTenantId());
        validateProjectOwnership(project);
        validateActiveOrganization(project.getTenantId());

        if(project.getStatus() == ProjectStatus.COMPLETED){
            throw new ValidationException("Completed project cannot be cancelled");
        }
        project.setStatus(ProjectStatus.CANCELLED);
        project.setUpdatedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(project);
        log.warn(
                "Project cancelled: {}",
                projectCode
        );

        return convertToDTO(updatedProject);
    }

    public List<ProjectResponseDTO> getProjectsByManager(String employeeId){
        securityUtil.validateActiveUser();
        User manager = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        if(manager.getRole() != UserRole.MANAGER && manager.getRole() != UserRole.ORG_ADMIN){
            throw new ValidationException("Selected user is not a project manager");
        }
        if(securityUtil.getCurrentUserRole() == UserRole.MANAGER && !employeeId.equals(securityUtil.getCurrentEmployeeId())){
            throw new AccessDeniedException("You can only view your own projects");
        }
        securityUtil.validateTenantAccess(manager.getTenantId());
        List<Project> projects = projectRepository.findByProjectManagerEmployeeId(employeeId);
        return convertToDTOList(projects);
    }

    public List<ProjectResponseDTO> findByStatus(ProjectStatus status){
        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        List<Project> projects = projectRepository.findByTenantId(tenantId)
                .stream().filter(project -> project.getStatus() == status).toList();
        if(securityUtil.getCurrentUserRole() == UserRole.MANAGER){
            String managerId = securityUtil.getCurrentEmployeeId();
            projects = projects.stream().filter(project ->
                    managerId.equals(project.getProjectManagerEmployeeId())).toList();
        }
        return convertToDTOList(projects);
    }

    public List<ProjectResponseDTO> getMyProjects(){
        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        List<Project> projects = projectRepository.findByTenantId(tenantId);
        if(securityUtil.getCurrentUserRole() == UserRole.MANAGER){
            String managerId = securityUtil.getCurrentEmployeeId();
            projects = projects.stream().filter(project ->
                    managerId.equals(project.getProjectManagerEmployeeId())).toList();
        }
        if(securityUtil.getCurrentUserRole() == UserRole.EMPLOYEE){
            String employeeId = securityUtil.getCurrentEmployeeId();
            projects = projects.stream().filter(project ->
                    project.getMemberIds() != null && project.getMemberIds().contains(employeeId)).toList();
        }
        return convertToDTOList(projects);
    }

    public Project findByProjectCode(String projectCode){
        return projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    @Cacheable(value = "projectsByCode", key = "#projectCode")
    public ProjectResponseDTO getProjectByCode(String projectCode){
        securityUtil.validateActiveUser();
        Project project = findByProjectCode(projectCode);
        securityUtil.validateTenantAccess(project.getTenantId());
        validateProjectOwnership(project);
        return convertToDTO(project);
    }

}
