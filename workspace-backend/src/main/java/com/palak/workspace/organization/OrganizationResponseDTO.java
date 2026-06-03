package com.palak.workspace.organization;

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
