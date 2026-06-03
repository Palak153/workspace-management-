package com.palak.workspace.organization;

import com.palak.workspace.user.User;
import com.palak.workspace.user.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public Organization updateOrganization(String organizationCode, Organization newOrganization){
        Organization oldOrganization = findOrganizationByCode(organizationCode);
        if (oldOrganization != null){
             oldOrganization.setOrganizationName(
                    newOrganization.getOrganizationName() != null && !newOrganization.getOrganizationName().isBlank() ?
                            newOrganization.getOrganizationName() : oldOrganization.getOrganizationName());

            oldOrganization.setEmail(
                    newOrganization.getEmail() != null && !newOrganization.getEmail().isBlank() ?
                            newOrganization.getEmail() : oldOrganization.getEmail());

            oldOrganization.setPhone(
                    newOrganization.getPhone() != null && !newOrganization.getPhone().isBlank() ?
                            newOrganization.getPhone() : oldOrganization.getPhone());

            oldOrganization.setAddress(
                    newOrganization.getAddress() != null && !newOrganization.getAddress().isBlank() ?
                            newOrganization.getAddress() : oldOrganization.getAddress());

            oldOrganization.setUpdatedAt(LocalDateTime.now());

            return organizationRepository.save(oldOrganization);
        }
        return null;
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

    public List<Organization> getAllOrganization(){
        return organizationRepository.findAll();
    }

    public Boolean deleteOrganization(String organizationCode){
        Organization organization = findOrganizationByCode(organizationCode);
        if(organization != null){
            organizationRepository.delete(organization);
            return true;
        }
        return false;
    }

    public OrganizationResponseDTO convertToDTO(Organization organization) {
        OrganizationResponseDTO response = new OrganizationResponseDTO();
        response.setTenantId(organization.getTenantId());
        response.setOrganizationName(organization.getOrganizationName());
        response.setOrganizationCode(organization.getOrganizationCode());
        response.setEmail(organization.getEmail());
        response.setPhone(organization.getPhone());
        response.setAddress(organization.getAddress());
        response.setStatus(organization.getStatus());
        return response;
    }

    public List<OrganizationResponseDTO> convertToDTOList(List<Organization> organizations) {
        List<OrganizationResponseDTO> responseList = new ArrayList<>();
        for (Organization org : organizations) {
            responseList.add(convertToDTO(org));
        }
        return responseList;
    }



}
