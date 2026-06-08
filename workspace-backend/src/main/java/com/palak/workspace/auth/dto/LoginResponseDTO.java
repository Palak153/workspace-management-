package com.palak.workspace.auth.dto;

import com.palak.workspace.user.UserRole;
import lombok.Data;

@Data
public class LoginResponseDTO {

    private String token;

    private String email;

    private String firstName;
    private String lastName;

    private UserRole role;

    private String tenantId;

    private String organizationCode;
    private String organizationName;
}
