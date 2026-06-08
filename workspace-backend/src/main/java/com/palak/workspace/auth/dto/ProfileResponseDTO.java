package com.palak.workspace.auth.dto;

import com.palak.workspace.user.UserRole;
import lombok.Data;

@Data
public class ProfileResponseDTO {
    private String email;

    private String firstName;

    private String lastName;

    private UserRole role;

    private String tenantId;

    private String employeeId;

    private String designation;

    private String organizationCode;

    private String organizationName;

    private Boolean isActive;
}
