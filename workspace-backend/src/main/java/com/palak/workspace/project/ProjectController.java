package com.palak.workspace.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create-project")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> createProject(@RequestBody Project project){
        try{
            Project createdProject = projectService.createProject(project);
            return new ResponseEntity<>(projectService.convertToDTO(createdProject), HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/project-code/{projectCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByProjectCode(@PathVariable String projectCode){
        Project project = projectService.findByProjectCode(projectCode);
        if(project != null){
            return new ResponseEntity<>(projectService.convertToDTO(project), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getProjectsByTenant(@PathVariable String tenantId){
        List<Project> projects = projectService.findByTenantId(tenantId);
        return new ResponseEntity<>(projectService.convertToDTOList(projects), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getProjectsByStatus(@PathVariable ProjectStatus status){
        List<Project> projects = projectService.findByStatus(status);
        return new ResponseEntity<>(projectService.convertToDTOList(projects), HttpStatus.OK);
    }

    @GetMapping("/project-manager/{employeeId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getProjectsByManager(@PathVariable String employeeId){
        List<Project> projects = projectService.findByProjectManagerEmployeeId(employeeId);
        return new ResponseEntity<>(projectService.convertToDTOList(projects), HttpStatus.OK);
    }

    @PutMapping("/{projectCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> updateProject(@PathVariable String projectCode, @RequestBody Project newProject){
        Project project = projectService.updateProject(projectCode, newProject);
        if(project != null){
            return new ResponseEntity<>(projectService.convertToDTO(project), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{projectCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> deleteProject(@PathVariable String projectCode){
        Boolean deleted = projectService.deleteProject(projectCode);
        if(deleted){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
