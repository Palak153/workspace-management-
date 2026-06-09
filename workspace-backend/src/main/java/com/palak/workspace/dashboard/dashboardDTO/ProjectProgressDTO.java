package com.palak.workspace.dashboard.dashboardDTO;

import com.palak.workspace.project.ProjectStatus;
import lombok.Data;

@Data
public class ProjectProgressDTO {

    private String projectCode;

    private String projectName;

    private Integer progressPercentage;

    private ProjectStatus status;
}
