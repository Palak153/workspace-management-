package com.palak.workspace.organization;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrganizationService {


    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository){
        this.organizationRepository = organizationRepository;
    }

    public Organization saveOrganization (Organization organization){
        organization.setCreatedAt(LocalDateTime.now());
        organization.setStatus(OrganizationStatus.ACTIVE);
        String tenantId = "ORG_" + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
        organization.setTenantId(tenantId);
        return organizationRepository.save(organization);
    }

}
