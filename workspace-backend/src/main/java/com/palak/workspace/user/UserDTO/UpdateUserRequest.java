package com.palak.workspace.user.UserDTO;

import com.palak.workspace.user.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String designation;

    private UserRole role;

    private Boolean isActive;
}
