package com.palak.workspace.project;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectResponseDTO {
    private String projectName;
    private String projectCode;
    private String description;
    private String projectManagerEmployeeId;
    private List<String> memberIds;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progressPercentage;
}
