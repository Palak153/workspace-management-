package com.palak.workspace.task;

import com.palak.workspace.task.taskDTO.CreateTaskRequest;
import com.palak.workspace.task.taskDTO.UpdateTaskRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/create-task")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest task){
        return new ResponseEntity<>(taskService.createTask(task), HttpStatus.CREATED);
    }

    @GetMapping("/task-code/{taskCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByTaskCode(@PathVariable String taskCode){
        return new ResponseEntity<>(taskService.getTaskByCode(taskCode), HttpStatus.OK);
    }

    @GetMapping("/project/{projectCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByProjectCode(@PathVariable String projectCode){
        return new ResponseEntity<>(taskService.getTasksByProject(projectCode), HttpStatus.OK);
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByTenantId(){
        return new ResponseEntity<>(taskService.getMyOrganizationTasks(), HttpStatus.OK);
    }

    @GetMapping("/assignee/{employeeId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByAssignee(@PathVariable String employeeId){
        return new ResponseEntity<>(taskService.getTasksByAssignee(employeeId), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByStatus(@PathVariable TaskStatus status){
        return new ResponseEntity<>(taskService.getTasksByStatus(status), HttpStatus.OK);
    }

    @GetMapping("/priority/{priority}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByPriority(@PathVariable TaskPriority priority){
        return new ResponseEntity<>(taskService.getTasksByPriority(priority), HttpStatus.OK);
    }

    @GetMapping("/due-date/{dueDate}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByDueDate(@PathVariable LocalDate dueDate){
        return new ResponseEntity<>(taskService.getTasksByDueDate(dueDate), HttpStatus.OK);
    }

    @GetMapping("/project/{projectCode}/status/{status}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByProjectAndStatus(@PathVariable String projectCode, @PathVariable TaskStatus status){
        return new ResponseEntity<>(taskService.getTasksByProjectAndStatus(projectCode, status), HttpStatus.OK);
    }

    @PutMapping("/{taskCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> updateTask(@PathVariable String taskCode, @RequestBody UpdateTaskRequest newTask){
        return new ResponseEntity<>(taskService.updateTask(taskCode,newTask), HttpStatus.OK);
    }

    @DeleteMapping("/{taskCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> deleteTask(@PathVariable String taskCode){
        taskService.deleteTask(taskCode);
        return ResponseEntity.noContent().build();
    }

}
