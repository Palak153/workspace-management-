package com.palak.workspace.task;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskResponseDTO {

    private String taskCode;

    private String projectCode;

    private String title;

    private String description;

    private String assignedToEmployeeId;

    private String createdByEmployeeId;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
