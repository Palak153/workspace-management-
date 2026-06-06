package com.palak.workspace.organization;

import com.palak.workspace.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService, SecurityUtil securityUtil){
        this.organizationService = organizationService;
    }


    @PostMapping("/create-organization")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createOrganization(@RequestBody Organization newOrganization){
        return new ResponseEntity<>(organizationService.saveOrganization(newOrganization), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllOrganization(){
        return new ResponseEntity<>(organizationService.getAllOrganization(),HttpStatus.OK);
    }


    @GetMapping("/org-code/{orgCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')")
    public ResponseEntity<?> getByOrganizationCode(@PathVariable String orgCode){
        return new ResponseEntity<>(organizationService.getOrganizationByCode(orgCode), HttpStatus.OK);
    }

    @GetMapping("/my-organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyOrganization(){
        return new ResponseEntity<>(organizationService.getMyOrganization(), HttpStatus.OK);
    }

    @PutMapping("/{orgCode}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> updateOrganization(@PathVariable String orgCode, @RequestBody Organization newOrganization){
        return new ResponseEntity<>(organizationService.updateOrganization(orgCode, newOrganization), HttpStatus.OK);
    }

}
