package com.palak.workspace.organization;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService){
        this.organizationService = organizationService;
    }

    @PostMapping("/create-organization")
    @PreAuthorize(
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization newOrganization){
        try{
            Organization organization = organizationService.saveOrganization(newOrganization);
            return new ResponseEntity<>(organization, HttpStatus.CREATED);
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
        return new ResponseEntity<>(allOrganization,HttpStatus.OK);
    }

    @GetMapping("/org-code/{orgCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByOrganizationCode(@PathVariable String orgCode){
        Organization organization = organizationService.findOrganizationByCode(orgCode);
        if(organization != null){
            return new ResponseEntity<>(organization, HttpStatus.OK);
        }
        return new ResponseEntity<>(organization, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/tenant-id/{tenantId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> getByTenantID(@PathVariable String tenantId){
        Organization organization = organizationService.findOrganizationByTenantID(tenantId);
        if(organization != null){
            return new ResponseEntity<>(organization, HttpStatus.OK);
        }
        return new ResponseEntity<>(organization, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{orgCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> updateOrganization(@PathVariable String orgCode, @RequestBody Organization newOrganization){
        Organization organization = organizationService.updateOrganization(orgCode, newOrganization);
        if(organization != null){
            return new ResponseEntity<>(organization, HttpStatus.OK);
        }
        return new ResponseEntity<>(newOrganization, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{orgCode}")
    @PreAuthorize(
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<?> deleteOrganization(@PathVariable String orgCode){
        Boolean b = organizationService.deleteOrganization(orgCode);
        if(b){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
