package com.palak.workspace.user.UserDTO;

import com.palak.workspace.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Organization code is required")
    private String organizationCode;

    @NotNull(message = "Role is required")
    private UserRole role;
}
