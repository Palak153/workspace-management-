package com.palak.workspace.task.taskDTO;

import com.palak.workspace.task.TaskPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotBlank(message = "Assignee is required")
    private String assignedToEmployeeId;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;
}
