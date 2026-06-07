package com.palak.workspace.auth;

import com.palak.workspace.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {

    private String email;
    private String tenantId;
    private String employeeId;
    private UserRole role;
    private Boolean isActive;
}
