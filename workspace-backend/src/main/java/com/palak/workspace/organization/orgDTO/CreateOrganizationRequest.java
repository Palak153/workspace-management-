package com.palak.workspace.organization.orgDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Organization code is required")
    private String organizationCode;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;
}
