package com.palak.workspace.auth;

import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationService;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final OrganizationService organizationService;

    public AuthController(JwtService jwtService, AuthenticationManager authenticationManager, UserService userService, OrganizationService organizationService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try{
            User user = userService.findByEmail(request.getEmail());

            if(user == null){
                return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
            }

            if (!user.getIsActive()){
                return new ResponseEntity<>("Inactive user", HttpStatus.FORBIDDEN);
            }

            Organization organization = organizationService.findOrganizationByCode(user.getOrganizationCode());

            if(organization.getStatus() == OrganizationStatus.INACTIVE){
                return new ResponseEntity<>("Organization is inactive",HttpStatus.FORBIDDEN);
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));

            user.setLastLogin(LocalDateTime.now());
            userService.saveUser(user);
            String token = jwtService.generateToken(user);
            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setRole(user.getRole());
            response.setTenantId(user.getTenantId());
            response.setOrganizationCode(user.getOrganizationCode());
            response.setOrganizationName(organization.getOrganizationName());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Exception occurred while createAuthenticationToken ", e);
            return new ResponseEntity<>("Incorrect username or password ", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        String email = authentication.getName();

        User user = userService.findByEmail(email);

        Organization organization =
                organizationService.findOrganizationByCode(
                        user.getOrganizationCode());

        ProfileResponseDTO response =
                new ProfileResponseDTO();

        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setTenantId(user.getTenantId());
        response.setEmployeeId(user.getEmployeeId());
        response.setDesignation(user.getDesignation());
        response.setOrganizationCode(user.getOrganizationCode());
        response.setOrganizationName(
                organization.getOrganizationName());
        response.setIsActive(user.getIsActive());

        return ResponseEntity.ok(response);
    }
}
