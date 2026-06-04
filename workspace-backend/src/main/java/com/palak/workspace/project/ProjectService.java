package com.palak.workspace.project;

import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
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
    public ProjectService(
            ProjectRepository projectRepository, OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
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

    public Project createProject(Project project) {
        Project existingProject = projectRepository.findByProjectCode(project.getProjectCode()).orElse(null);
        if (existingProject != null) {
            throw new RuntimeException("Project code already exists");
        }

        // Validate tenant
        Organization organization = organizationRepository.findByTenantId(project.getTenantId()).orElse(null);
        if (organization == null) {
            throw new RuntimeException("Invalid tenant id");
        }

        // Validate manager
        User manager = userRepository.findByEmployeeId(project.getProjectManagerEmployeeId()).orElse(null);

        if (manager == null) {
            throw new RuntimeException("Project manager not found");
        }

        if (manager.getRole() != UserRole.MANAGER) {
            throw new RuntimeException("Selected user is not a manager");
        }

        if (!manager.getTenantId().equals(project.getTenantId())) {
            throw new RuntimeException("Manager does not belong to this organization");
        }

        // Validate members
        if (project.getMemberIds() != null) {
            for (String memberId : project.getMemberIds()) {

                User member = userRepository.findByEmployeeId(memberId).orElse(null);
                if (member == null) {
                    throw new RuntimeException("Invalid member id : " + memberId);
                }
                if (!member.getTenantId().equals(project.getTenantId())) {
                    throw new RuntimeException("Member does not belong to this organization");
                }
            }
        }

        String projectCode = "PROJ_" + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        project.setProjectCode(projectCode);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus(ProjectStatus.PLANNED);
        project.setProgressPercentage(0);
        return projectRepository.save(project);
    }

    public Project updateProject(String projectCode, Project newProject){

        Project oldProject = projectRepository.findByProjectCode(projectCode).orElse(null);
        if(oldProject != null){

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
            return projectRepository.save(oldProject);
        }
        return null;
    }

    public Boolean deleteProject(String projectCode){
        Project project = projectRepository.findByProjectCode(projectCode).orElse(null);
        if(project != null){
            projectRepository.delete(project);
            return true;
        }
        return false;
    }

    public List<Project> findByProjectManagerEmployeeId(String employeeId){
        return projectRepository.findByProjectManagerEmployeeId(employeeId);
    }

    public List<Project> findByStatus(ProjectStatus status){
        return projectRepository.findByStatus(status);
    }

    public List<Project> findByTenantId(String tenantId){
        return projectRepository.findByTenantId(tenantId);
    }

    public Project findByProjectCode(String projectCode){
        return projectRepository.findByProjectCode(projectCode).orElse(null);
    }

}
