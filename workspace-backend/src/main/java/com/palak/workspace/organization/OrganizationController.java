package com.palak.workspace.organization;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            organizationService.saveOrganization(newOrganization);
            return new ResponseEntity<>(newOrganization, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(newOrganization, HttpStatus.BAD_REQUEST);
        }
    }

}
