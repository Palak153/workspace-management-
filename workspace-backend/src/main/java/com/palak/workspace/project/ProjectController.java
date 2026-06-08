package com.palak.workspace.project;

import com.palak.workspace.project.projectDTO.CreateProjectRequest;
import com.palak.workspace.project.projectDTO.UpdateProjectRequest;
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
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequest project){
        return new ResponseEntity<>(projectService.createProject(project), HttpStatus.CREATED);
    }

    @GetMapping("/project-code/{projectCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByProjectCode(@PathVariable String projectCode){
        return new ResponseEntity<>(projectService.getProjectByCode(projectCode), HttpStatus.OK);
    }

    @GetMapping("/my-projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProjects(){
        return new ResponseEntity<>(projectService.getMyProjects(), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getProjectsByStatus(@PathVariable ProjectStatus status){
        return new ResponseEntity<>(projectService.findByStatus(status), HttpStatus.OK);
    }

    @GetMapping("/project-manager/{employeeId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getProjectsByManager(@PathVariable String employeeId){
        return new ResponseEntity<>(projectService.getProjectsByManager(employeeId), HttpStatus.OK);
    }

    @PutMapping("/{projectCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> updateProject(@PathVariable String projectCode, @RequestBody UpdateProjectRequest newProject){
        return new ResponseEntity<>(projectService.updateProject(projectCode, newProject), HttpStatus.OK);
    }

    @PatchMapping("/{projectCode}/cancel")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> cancelProject(
            @PathVariable String projectCode){
        return new ResponseEntity<>(
                projectService.cancelProject(projectCode),
                HttpStatus.OK);
    }

}
