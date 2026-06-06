package com.palak.workspace.user;
import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationService;
import com.palak.workspace.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;
    private final OrganizationService organizationService;
    public UserController(UserService userService, SecurityUtil securityUtil, OrganizationService organizationService){
        this.userService =userService;
        this.securityUtil = securityUtil;
        this.organizationService = organizationService;
    }


    @PostMapping("/create-user")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> createUser(@RequestBody User user){

        try{
            User currentUser = securityUtil.getCurrentUser();

            if(!currentUser.getIsActive()){
                return new ResponseEntity<>("Inactive user cannot create users", HttpStatus.FORBIDDEN);
            }

            UserRole currentRole = currentUser.getRole();

            Organization organization = organizationService.findOrganizationByCode(user.getOrganizationCode());

            if(organization == null){
                return new ResponseEntity<>("Organization not found", HttpStatus.BAD_REQUEST);
            }

            // Tenant Isolation
            if(!securityUtil.isSuperAdmin() && !organization.getTenantId()
                    .equals(securityUtil.getCurrentTenantId())){
                return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
            }

            // ORG_ADMIN Restrictions
            if(currentRole == UserRole.ORG_ADMIN){

                if(user.getRole() == UserRole.SUPER_ADMIN){
                    return new ResponseEntity<>("ORG_ADMIN cannot create SUPER_ADMIN", HttpStatus.FORBIDDEN);
                }

                if(user.getRole() == UserRole.ORG_ADMIN){
                    return new ResponseEntity<>("ORG_ADMIN cannot create another ORG_ADMIN", HttpStatus.FORBIDDEN);
                }
            }

            User newUser = userService.createUser(user);
            return new ResponseEntity<>(userService.convertToDTO(newUser), HttpStatus.CREATED);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/email/{email}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> findUserByEmail(@PathVariable String email){

        User user = userService.findByEmail(email);

        if(user == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!securityUtil.isSuperAdmin() && !user.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(userService.convertToDTO(user), HttpStatus.OK);
    }

    @GetMapping("/employeeId/{empId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> findUserByEmployeeId(@PathVariable String empId) {

        User user = userService.findByEmployeeId(empId);
        if(user == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!securityUtil.isSuperAdmin() && !user.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(userService.convertToDTO(user), HttpStatus.OK);
    }

    @PutMapping("/employeeId/{empId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> updateUser(@PathVariable String empId, @RequestBody User newUser){
        User existingUser = userService.findByEmployeeId(empId);
        if(existingUser == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!securityUtil.isSuperAdmin() && !existingUser.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }

        if(newUser.getIsActive() != null && !newUser.getIsActive() && existingUser.getEmployeeId()
                .equals(securityUtil.getCurrentUser().getEmployeeId())){
            return new ResponseEntity<>("You cannot deactivate yourself", HttpStatus.BAD_REQUEST);
        }

        UserRole currentRole = securityUtil.getCurrentUserRole();

        // ORG_ADMIN Restrictions
        if(currentRole == UserRole.ORG_ADMIN){

            if(newUser.getRole() == UserRole.SUPER_ADMIN){
                return new ResponseEntity<>("ORG_ADMIN cannot update role to SUPER_ADMIN", HttpStatus.FORBIDDEN);
            }

            if(newUser.getRole() == UserRole.ORG_ADMIN){
                return new ResponseEntity<>("ORG_ADMIN cannot update role to ORG_ADMIN", HttpStatus.FORBIDDEN);
            }
        }

        if(existingUser.getRole() == UserRole.SUPER_ADMIN){
            return new ResponseEntity<>("Cannot modify SUPER_ADMIN", HttpStatus.FORBIDDEN);
        }

        if(currentRole == UserRole.MANAGER && existingUser.getRole() != UserRole.EMPLOYEE){
            return new ResponseEntity<>("Manager can only modify employees", HttpStatus.FORBIDDEN);
        }

        boolean canUpdateActiveStatus = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.ORG_ADMIN;

        User updatedUser = userService.updateUser(empId, newUser, canUpdateActiveStatus);
        return new ResponseEntity<>(userService.convertToDTO(updatedUser), HttpStatus.OK);
    }


    @GetMapping("/my-organization-users")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> allOrganizationUsers(){

        String tenantId = securityUtil.getCurrentTenantId();

        List<User> users = userService.allTenantUser(tenantId);

        return new ResponseEntity<>(userService.convertToDTOList(users), HttpStatus.OK);
    }

    @PatchMapping("/employeeId/{empId}/deactivate")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> deactivateUser(@PathVariable String empId){

        User existingUser = userService.findByEmployeeId(empId);

        if(existingUser == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!existingUser.getIsActive()){
            return new ResponseEntity<>("User is already inactive", HttpStatus.BAD_REQUEST);
        }

        // Tenant Isolation
        if(!securityUtil.isSuperAdmin() && !existingUser.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }

        UserRole currentRole = securityUtil.getCurrentUserRole();

        // Cannot deactivate yourself
        if(existingUser.getEmployeeId().equals(securityUtil.getCurrentUser().getEmployeeId())){
            return new ResponseEntity<>("You cannot deactivate yourself", HttpStatus.BAD_REQUEST);
        }

        // Protect SUPER_ADMIN
        if(existingUser.getRole() == UserRole.SUPER_ADMIN){
            return new ResponseEntity<>("Cannot deactivate SUPER_ADMIN", HttpStatus.FORBIDDEN);
        }

        // ORG_ADMIN restrictions
        if(currentRole == UserRole.ORG_ADMIN && existingUser.getRole() == UserRole.ORG_ADMIN){
            return new ResponseEntity<>("Cannot deactivate ORG_ADMIN", HttpStatus.FORBIDDEN);
        }

        User user = userService.deactivateUser(empId);

        return new ResponseEntity<>(userService.convertToDTO(user), HttpStatus.OK);
    }

    @PatchMapping("/employeeId/{empId}/activate")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> activateUser(@PathVariable String empId){

        User existingUser = userService.findByEmployeeId(empId);

        if(existingUser == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(existingUser.getIsActive()){
            return new ResponseEntity<>("User is already active", HttpStatus.BAD_REQUEST);
        }

        // Tenant Isolation
        if(!securityUtil.isSuperAdmin() && !existingUser.getTenantId().equals(securityUtil.getCurrentTenantId())){
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }

        UserRole currentRole = securityUtil.getCurrentUserRole();

        // Protect SUPER_ADMIN
        if(existingUser.getRole() == UserRole.SUPER_ADMIN){
            return new ResponseEntity<>("Cannot modify SUPER_ADMIN", HttpStatus.FORBIDDEN);
        }

        // ORG_ADMIN restrictions
        if(currentRole == UserRole.ORG_ADMIN && existingUser.getRole() == UserRole.ORG_ADMIN){
            return new ResponseEntity<>("Cannot activate ORG_ADMIN", HttpStatus.FORBIDDEN);
        }

        User user = userService.activateUser(empId);
        return new ResponseEntity<>(userService.convertToDTO(user), HttpStatus.OK);
    }

}

