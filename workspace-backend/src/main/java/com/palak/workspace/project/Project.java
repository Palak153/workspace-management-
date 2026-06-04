package com.palak.workspace.project;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "projects")
@NoArgsConstructor
@Data
public class Project {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String projectName;

    @Indexed(unique = true)
    private String projectCode;

    private String description;

    private List<String> memberIds;

    @Indexed
    private ProjectStatus status;

    @Indexed
    private String projectManagerEmployeeId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer progressPercentage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
