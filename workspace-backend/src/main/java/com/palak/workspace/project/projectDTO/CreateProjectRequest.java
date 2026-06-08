package com.palak.workspace.project.projectDTO;

import com.palak.workspace.project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;

    @NotBlank(message = "Project manager is required")
    private String projectManagerEmployeeId;

    @NotEmpty(message = "At least one project member is required")
    private List<String> memberIds;
}
