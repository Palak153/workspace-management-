package com.palak.workspace.task;

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
    public ResponseEntity<?> createTask(@RequestBody Task task){
        try{
            Task createdTask = taskService.createTask(task);
            return new ResponseEntity<>(taskService.convertToDTO(createdTask), HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/task-code/{taskCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByTaskCode(@PathVariable String taskCode){

        Task task = taskService.findByTaskCode(taskCode);
        if(task != null){
            return new ResponseEntity<>(taskService.convertToDTO(task), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/project/{projectCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByProjectCode(@PathVariable String projectCode){
        List<Task> tasks = taskService.findByProjectCode(projectCode);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByTenantId(@PathVariable String tenantId){
        List<Task> tasks = taskService.findByTenantId(tenantId);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @GetMapping("/assignee/{employeeId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByAssignee(@PathVariable String employeeId){
        List<Task> tasks = taskService.findByAssignedEmployee(employeeId);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByStatus(@PathVariable TaskStatus status){
        List<Task> tasks = taskService.findByStatus(status);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @GetMapping("/priority/{priority}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByPriority(@PathVariable TaskPriority priority){
        List<Task> tasks = taskService.findByPriority(priority);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @GetMapping("/due-date/{dueDate}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByDueDate(@PathVariable LocalDate dueDate){
        List<Task> tasks = taskService.findByDueDate(dueDate);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @GetMapping("/project/{projectCode}/status/{status}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByProjectAndStatus(@PathVariable String projectCode, @PathVariable TaskStatus status){
        List<Task> tasks = taskService.findByProjectCodeAndStatus(projectCode, status);
        return new ResponseEntity<>(taskService.convertToDTOList(tasks), HttpStatus.OK);
    }

    @PutMapping("/{taskCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> updateTask(@PathVariable String taskCode, @RequestBody Task newTask){

        try{
            Task task = taskService.updateTask(taskCode, newTask);
            if(task != null){
                return new ResponseEntity<>(taskService.convertToDTO(task), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{taskCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> deleteTask(@PathVariable String taskCode){

        Boolean deleted = taskService.deleteTask(taskCode);
        if(deleted){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }



}
