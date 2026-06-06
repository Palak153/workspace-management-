package com.palak.workspace.organization;

import com.palak.workspace.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final SecurityUtil securityUtil;

    public OrganizationController(OrganizationService organizationService, SecurityUtil securityUtil){
        this.organizationService = organizationService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/create-organization")
    @PreAuthorize(
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<?> createOrganization(@RequestBody Organization newOrganization){
        try{
            Organization organization = organizationService.saveOrganization(newOrganization);
            return new ResponseEntity<>(organizationService.convertToDTO(organization), HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(newOrganization, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @PreAuthorize(
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<?> getAllOrganization(){
        List<Organization> allOrganization = organizationService.getAllOrganization();
        return new ResponseEntity<>(organizationService.convertToDTOList(allOrganization),HttpStatus.OK);
    }


    @GetMapping("/org-code/{orgCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByOrganizationCode(@PathVariable String orgCode){

        Organization organization = organizationService.findOrganizationByCode(orgCode);
        if(organization == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!securityUtil.isSuperAdmin() && !organization.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(organizationService.convertToDTO(organization), HttpStatus.OK);
    }

    @GetMapping("/my-organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyOrganization(){

        String tenantId = securityUtil.getCurrentTenantId();

        Organization organization = organizationService.findOrganizationByTenantID(tenantId);

        if(organization == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(organizationService.convertToDTO(organization), HttpStatus.OK);
    }

    @PutMapping("/{orgCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> updateOrganization(@PathVariable String orgCode, @RequestBody Organization newOrganization){

        Organization existingOrganization = organizationService.findOrganizationByCode(orgCode);

        if(existingOrganization == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!securityUtil.isSuperAdmin() && !existingOrganization.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }

        if(!securityUtil.isSuperAdmin() && newOrganization.getStatus() != null){
            return new ResponseEntity<>("Only SUPER_ADMIN can change organization status", HttpStatus.FORBIDDEN);
        }

        Organization updatedOrganization = organizationService.updateOrganization(orgCode, newOrganization,securityUtil.isSuperAdmin());

        if(updatedOrganization == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(organizationService.convertToDTO(updatedOrganization), HttpStatus.OK);
    }

}
