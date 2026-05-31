package com.palak.workspace.organization;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService){
        this.organizationService = organizationService;
    }

    @PostMapping("/create-organization")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization newOrganization){
        try{
            Organization organization = organizationService.saveOrganization(newOrganization);
            return new ResponseEntity<>(organization, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(newOrganization, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/org-code/{orgCode}")
    public ResponseEntity<?> getByOrganizationCode(@PathVariable String orgCode){
        Organization organization = organizationService.findOrganizationByCode(orgCode);
        if(organization != null){
            return new ResponseEntity<>(organization, HttpStatus.OK);
        }
        return new ResponseEntity<>(organization, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/tenant-id/{tenantId}")
    public ResponseEntity<?> getByTenantID(@PathVariable String tenantId){
        Organization organization = organizationService.findOrganizationByTenantID(tenantId);
        if(organization != null){
            return new ResponseEntity<>(organization, HttpStatus.OK);
        }
        return new ResponseEntity<>(organization, HttpStatus.NOT_FOUND);
    }
}
