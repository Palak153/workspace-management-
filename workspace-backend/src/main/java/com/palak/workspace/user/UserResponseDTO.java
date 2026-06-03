package com.palak.workspace.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String employeeId;
    private String designation;
    private UserRole role;
    private String organizationCode;
    private Boolean isActive;
}
