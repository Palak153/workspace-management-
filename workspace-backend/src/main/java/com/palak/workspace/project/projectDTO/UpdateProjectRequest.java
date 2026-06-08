package com.palak.workspace.project.projectDTO;

import com.palak.workspace.project.ProjectStatus;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProjectRequest {

    private String projectName;

    private String description;

    private String projectManagerEmployeeId;

    private List<String> memberIds;

    private ProjectStatus status;
}
