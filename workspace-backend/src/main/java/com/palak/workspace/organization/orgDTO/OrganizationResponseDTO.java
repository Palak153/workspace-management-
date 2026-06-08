package com.palak.workspace.organization.orgDTO;

import com.palak.workspace.organization.OrganizationStatus;
import lombok.Data;

@Data
public class OrganizationResponseDTO {
    private String tenantId;
    private String organizationName;
    private String organizationCode;
    private String email;
    private String phone;
    private String address;
    private OrganizationStatus status;
}
