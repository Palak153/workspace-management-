package com.palak.workspace.project;

import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import com.palak.workspace.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
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

    private void validateActiveOrganization(String tenantId){

        Organization organization = organizationRepository
                .findByTenantId(tenantId)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found"));

        if(organization.getStatus() == OrganizationStatus.INACTIVE){
            throw new RuntimeException(
                    "Organization is inactive");
        }
    }

    private void validateProjectManager(String employeeId, String tenantId){

        User manager = userRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Project manager not found"));

        if(!manager.getIsActive()){
            throw new RuntimeException("Project manager is inactive");
        }

        if (manager.getRole() != UserRole.MANAGER) {
            throw new RuntimeException("Selected user is not a manager");
        }

        if (!manager.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Manager does not belong to this organization");
        }
    }

    private void validateProjectMembers(List<String> memberIds, String tenantId) {

        if(memberIds == null){
            return;
        }
        for(String memberId : memberIds){

            User member = userRepository.findByEmployeeId(memberId)
                    .orElseThrow(() -> new RuntimeException("Invalid member id : " + memberId));

            if(!member.getTenantId().equals(tenantId)){
                throw new RuntimeException("Member does not belong to this organization");
            }

            if(!member.getIsActive()){
                throw new RuntimeException("Inactive member cannot be assigned to project");
            }
        }
    }

    public ProjectResponseDTO  createProject(Project project) {
        securityUtil.validateActiveUser();

        String tenantId = securityUtil.getCurrentTenantId();

        // Validate tenant
        validateActiveOrganization(tenantId);

        // Validate manager
        validateProjectManager(project.getProjectManagerEmployeeId(), tenantId);

        // Validate members
        validateProjectMembers(project.getMemberIds(),tenantId);

        String projectCode = "PROJ_" + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        project.setTenantId(tenantId);
        project.setProjectCode(projectCode);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus(ProjectStatus.PLANNED);
        project.setProgressPercentage(0);
        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    public ProjectResponseDTO updateProject(String projectCode, Project newProject){

        securityUtil.validateActiveUser();
        Project oldProject = findByProjectCode(projectCode);
        securityUtil.validateTenantAccess(oldProject.getTenantId());
        validateActiveOrganization(oldProject.getTenantId());
        if(oldProject.getStatus() == ProjectStatus.COMPLETED){
            throw new RuntimeException("Completed project cannot be modified");
        }

        if(newProject.getStatus() != null){
            oldProject.setStatus(newProject.getStatus());
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

        oldProject.setStartDate(
                newProject.getStartDate() != null ? newProject.getStartDate() : oldProject.getStartDate());

        oldProject.setEndDate(
                newProject.getEndDate() != null ? newProject.getEndDate() : oldProject.getEndDate());

        oldProject.setUpdatedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(oldProject);
        return convertToDTO(updatedProject);
    }

    public ProjectResponseDTO cancelProject(String projectCode){

        securityUtil.validateActiveUser();
        Project project = findByProjectCode(projectCode);
        securityUtil.validateTenantAccess(project.getTenantId());
        validateActiveOrganization(project.getTenantId());

        if(project.getStatus() == ProjectStatus.COMPLETED){
            throw new RuntimeException("Completed project cannot be cancelled");
        }
        project.setStatus(ProjectStatus.CANCELLED);
        project.setUpdatedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(project);
        return convertToDTO(updatedProject);
    }

    public List<ProjectResponseDTO> getProjectsByManager(String employeeId){
        securityUtil.validateActiveUser();
        User manager = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        if(manager.getRole() != UserRole.MANAGER){
            throw new RuntimeException("Selected user is not a manager");
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
        return convertToDTOList(projects);
    }

    public List<ProjectResponseDTO> getMyProjects(){
        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        List<Project> projects = projectRepository.findByTenantId(tenantId);
        if(securityUtil.getCurrentUserRole() == UserRole.EMPLOYEE){
            String employeeId = securityUtil.getCurrentEmployeeId();
            projects = projects.stream().filter(project ->
                            project.getMemberIds() != null && project.getMemberIds().contains(employeeId)).toList();
        }
        return convertToDTOList(projects);
    }

    public Project findByProjectCode(String projectCode){
        return projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
    public ProjectResponseDTO getProjectByCode(String projectCode){
        securityUtil.validateActiveUser();
        Project project = findByProjectCode(projectCode);
        securityUtil.validateTenantAccess(project.getTenantId());
        return convertToDTO(project);
    }

}
