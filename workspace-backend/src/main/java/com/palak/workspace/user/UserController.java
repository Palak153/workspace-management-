package com.palak.workspace.user;

import com.palak.workspace.user.UserDTO.CreateUserRequest;
import com.palak.workspace.user.UserDTO.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService){
        this.userService =userService;
    }

    @PostMapping("/create-user")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserRequest user){
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> findUserByEmail(@PathVariable String email){
        return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
    }

    @GetMapping("/employeeId/{empId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> findUserByEmployeeId(@PathVariable String empId) {
        return new ResponseEntity<>(userService.getUserByEmployeeId(empId), HttpStatus.OK);
    }

    @PutMapping("/employeeId/{empId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> updateUser(@PathVariable String empId, @Valid @RequestBody UpdateUserRequest newUser){
        return new ResponseEntity<>(userService.updateUser(empId,newUser), HttpStatus.OK);
    }


    @GetMapping("/my-organization-users")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> allOrganizationUsers(){
        return new ResponseEntity<>(userService.getMyOrganizationUsers(), HttpStatus.OK);
    }

    @PatchMapping("/employeeId/{empId}/deactivate")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> deactivateUser(@PathVariable String empId){
        return new ResponseEntity<>(userService.deactivateUser(empId), HttpStatus.OK);
    }

    @PatchMapping("/employeeId/{empId}/activate")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> activateUser(@PathVariable String empId){
        return new ResponseEntity<>(userService.activateUser(empId), HttpStatus.OK);
    }
}

