package com.palak.workspace.auth;

import com.palak.workspace.auth.dto.LoginRequestDTO;
import com.palak.workspace.auth.dto.LoginResponseDTO;
import com.palak.workspace.auth.dto.ProfileResponseDTO;
import com.palak.workspace.exception.ValidationException;
import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationService;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserService userService,
            OrganizationService organizationService,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.organizationService = organizationService;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO request){

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userService.findByEmail(request.getEmail());

        if(!user.getIsActive()){
            throw new ValidationException("User account is inactive");
        }

        Organization organization = organizationService.findOrganizationByCode(user.getOrganizationCode());

        if(organization.getStatus() == OrganizationStatus.INACTIVE){
            throw new ValidationException("Organization is inactive");
        }

        user.setLastLogin(LocalDateTime.now());
        userService.saveUser(user);

        String token = jwtService.generateToken(user);

        return buildLoginResponse(user, organization, token);
    }

    private LoginResponseDTO buildLoginResponse(User user, Organization organization, String token){

        LoginResponseDTO response = new LoginResponseDTO();

        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setTenantId(user.getTenantId());
        response.setOrganizationCode(user.getOrganizationCode());
        response.setOrganizationName(organization.getOrganizationName());
        return response;
    }

    public ProfileResponseDTO getProfile(String email){
        User user = userService.findByEmail(email);
        Organization organization = organizationService.findOrganizationByCode(user.getOrganizationCode());
        return buildProfileResponse(user, organization);
    }

    private ProfileResponseDTO buildProfileResponse(User user, Organization organization){

        ProfileResponseDTO response = new ProfileResponseDTO();

        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setTenantId(user.getTenantId());
        response.setEmployeeId(user.getEmployeeId());
        response.setDesignation(user.getDesignation());
        response.setOrganizationCode(user.getOrganizationCode());
        response.setOrganizationName(organization.getOrganizationName());
        response.setIsActive(user.getIsActive());
        return response;
    }

}
