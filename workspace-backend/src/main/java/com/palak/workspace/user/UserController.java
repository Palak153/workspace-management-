package com.palak.workspace.user;

import com.palak.workspace.organization.Organization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> createUser(@RequestBody User user){
        try{
            User newUser = userService.createUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/email/{email}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> findUserByEmail(@PathVariable String email){
        User user = userService.findByEmail(email);
        if(user != null){
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
            return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/employeeId/{empId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> findUserByEmployeeId(@PathVariable String empId){
        User user = userService.findByEmployeeId(empId);
        if(user != null){
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/employeeId/{empId}")
    public ResponseEntity<?> updateUser(@PathVariable String empId, @RequestBody User newUser){
        User user = userService.updateUser(empId, newUser);
        if(user != null){
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(newUser, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN','MANAGER')"
    )
    public ResponseEntity<?> allTenantUser(@PathVariable String tenantId){
        List<User> users = userService.allTenantUser(tenantId);
        return new ResponseEntity<>(users,HttpStatus.OK);
    }

    @DeleteMapping("/employeeId/{empId}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN','ORG_ADMIN')"
    )
    public ResponseEntity<?> deleteUser(@PathVariable String empId){
        Boolean b = userService.deleteUser(empId);
        if(b){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}

