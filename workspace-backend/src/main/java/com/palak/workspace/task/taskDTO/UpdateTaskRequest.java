package com.palak.workspace.task.taskDTO;

import com.palak.workspace.task.TaskPriority;
import com.palak.workspace.task.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    private String title;

    private String description;

    private String assignedToEmployeeId;

    private TaskPriority priority;

    private LocalDate dueDate;

    private TaskStatus status;
}
