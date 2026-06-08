package com.palak.workspace.organization.orgDTO;

import com.palak.workspace.organization.OrganizationStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateOrganizationRequest {

    private String organizationName;

    @Email(message = "Invalid email")
    private String email;

    private String phone;

    private String address;

    private OrganizationStatus status;
}
