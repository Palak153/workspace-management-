package com.palak.workspace.organization;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrganizationService {


    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository){
        this.organizationRepository = organizationRepository;
    }

    public Organization saveOrganization (Organization organization){
        Organization organizationByCode = findOrganizationByCode(organization.getOrganizationCode());
        if (organizationByCode == null){
            organization.setCreatedAt(LocalDateTime.now());
            organization.setStatus(OrganizationStatus.ACTIVE);
            String tenantId = "ORG_" + UUID.randomUUID()
                    .toString()
                    .substring(0, 6)
                    .toUpperCase();
            organization.setTenantId(tenantId);
            return organizationRepository.save(organization);
        }
        throw new RuntimeException("Organization code already exists");
    }
    
    public Organization findOrganizationByCode(String organizationCode){
        Optional<Organization> organization = organizationRepository.findByOrganizationCode(organizationCode);
        if(organization.isPresent()){
            return organization.get();
        }
        return null;
    }

    public Organization findOrganizationByTenantID(String tenantId){
        Optional<Organization> organization = organizationRepository.findByTenantId(tenantId);
        if(organization.isPresent()){
            return organization.get();
        }
        return null;
    }

}
