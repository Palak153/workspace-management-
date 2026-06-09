package com.palak.workspace.organization;

import com.palak.workspace.common.exception.ResourceNotFoundException;
import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.organization.orgDTO.CreateOrganizationRequest;
import com.palak.workspace.organization.orgDTO.OrganizationResponseDTO;
import com.palak.workspace.organization.orgDTO.UpdateOrganizationRequest;
import com.palak.workspace.security.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {


    private final OrganizationRepository organizationRepository;
    private final SecurityUtil securityUtil;

    public OrganizationService(OrganizationRepository organizationRepository, SecurityUtil securityUtil){
        this.organizationRepository = organizationRepository;
        this.securityUtil = securityUtil;
    }

    public OrganizationResponseDTO saveOrganization(CreateOrganizationRequest request) {

        if (organizationRepository.findByOrganizationCode(request.getOrganizationCode()).isPresent()) {
            throw new ValidationException("Organization code already exists");
        }

        Organization organization = new Organization();
        organization.setOrganizationName(request.getOrganizationName());
        organization.setOrganizationCode(request.getOrganizationCode());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setAddress(request.getAddress());

        organization.setCreatedAt(LocalDateTime.now());
        organization.setStatus(OrganizationStatus.ACTIVE);
        String tenantId = "ORG_" + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
        organization.setTenantId(tenantId);
        Organization savedOrganization = organizationRepository.save(organization);
        return convertToDTO(savedOrganization);
    }

    public OrganizationResponseDTO updateOrganization(String organizationCode, UpdateOrganizationRequest newOrganization){

        securityUtil.validateActiveUser();
        Organization oldOrganization = findOrganizationByCode(organizationCode);

        securityUtil.validateTenantAccess(oldOrganization.getTenantId());

        if(!securityUtil.isSuperAdmin() && newOrganization.getStatus() != null){
            throw new ValidationException("Only SUPER_ADMIN can change organization status");
        }

        if(securityUtil.isSuperAdmin() && newOrganization.getStatus() != null){
            oldOrganization.setStatus(newOrganization.getStatus());
        }

        oldOrganization.setOrganizationName(
                newOrganization.getOrganizationName() != null
                        && !newOrganization.getOrganizationName().isBlank()
                        ? newOrganization.getOrganizationName()
                        : oldOrganization.getOrganizationName());

        oldOrganization.setEmail(
                newOrganization.getEmail() != null
                        && !newOrganization.getEmail().isBlank()
                        ? newOrganization.getEmail()
                        : oldOrganization.getEmail());

        oldOrganization.setPhone(
                newOrganization.getPhone() != null
                        && !newOrganization.getPhone().isBlank()
                        ? newOrganization.getPhone()
                        : oldOrganization.getPhone());

        oldOrganization.setAddress(
                newOrganization.getAddress() != null
                        && !newOrganization.getAddress().isBlank()
                        ? newOrganization.getAddress()
                        : oldOrganization.getAddress());

        oldOrganization.setUpdatedAt(LocalDateTime.now());
        Organization updatedOrganization = organizationRepository.save(oldOrganization);
        return convertToDTO(updatedOrganization);
    }

    public Organization findOrganizationByCode(String organizationCode){
        return organizationRepository.findByOrganizationCode(organizationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    public OrganizationResponseDTO getOrganizationByCode(String orgCode){
        Organization organization = findOrganizationByCode(orgCode);
        securityUtil.validateTenantAccess(organization.getTenantId());
        return convertToDTO(organization);
    }

    public Organization findOrganizationByTenantID(String tenantId){
        return organizationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }
    public OrganizationResponseDTO getMyOrganization() {
        String tenantId = securityUtil.getCurrentTenantId();
        Organization organization = findOrganizationByTenantID(tenantId);
        return convertToDTO(organization);
    }

    public List<OrganizationResponseDTO> getAllOrganization(){
        return convertToDTOList(organizationRepository.findAll());
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
