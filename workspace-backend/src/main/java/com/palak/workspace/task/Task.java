package com.palak.workspace.task;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "tasks")
@NoArgsConstructor
@Data
public class Task {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String projectCode;

    @Indexed(unique = true)
    private String taskCode;

    private String title;

    private String description;

    @Indexed
    private String assignedToEmployeeId;

    @Indexed
    private String createdByEmployeeId;

    @Indexed
    private TaskStatus status;

    @Indexed
    private TaskPriority priority;

    @Indexed
    private LocalDate dueDate;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
