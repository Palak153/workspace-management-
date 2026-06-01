package com.palak.workspace.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService){
        this.userService =userService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody User user){
        try{
            User newUser = userService.createUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> findUserByEmail(@PathVariable String email){
        User user = userService.findByEmail(email);
        if(user != null){
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
            return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/employeeId/{empId}")
    public ResponseEntity<?> findUserByEmployeeId(@PathVariable String empId){
        User user = userService.findByEmployeeId(empId);
        if(user != null){
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
    }


}

