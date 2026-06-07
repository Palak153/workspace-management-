package com.palak.workspace.user;

import com.palak.workspace.exception.AccessDeniedException;
import com.palak.workspace.exception.ResourceNotFoundException;
import com.palak.workspace.exception.ValidationException;
import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.security.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final SecurityUtil securityUtil;

    public UserService(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder, SecurityUtil securityUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository =organizationRepository;
        this.securityUtil = securityUtil;
    }

    private void validateUserCreationRole(UserRole creatorRole, UserRole targetRole){
        if(creatorRole == UserRole.ORG_ADMIN){
            if(targetRole == UserRole.SUPER_ADMIN){
                throw new ValidationException("ORG_ADMIN cannot create SUPER_ADMIN");
            }
            if(targetRole == UserRole.ORG_ADMIN){
                throw new ValidationException("ORG_ADMIN cannot create another ORG_ADMIN");
            }
        }
    }

    private void validateUserUpdateRole(UserRole currentRole, UserRole targetRole){
        if(currentRole == UserRole.ORG_ADMIN){
            if(targetRole == UserRole.SUPER_ADMIN){
                throw new ValidationException("ORG_ADMIN cannot update role to SUPER_ADMIN");
            }
            if(targetRole == UserRole.ORG_ADMIN){
                throw new ValidationException("ORG_ADMIN cannot update role to ORG_ADMIN");
            }
        }
    }


    public UserResponseDTO createUser(User user){

        securityUtil.validateActiveUser();

        Organization organization = organizationRepository
                .findByOrganizationCode(user.getOrganizationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        securityUtil.validateTenantAccess(organization.getTenantId());

        validateUserCreationRole(securityUtil.getCurrentUserRole(), user.getRole());

        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new ValidationException("Email already exists");
        }

        if(userRepository.findByEmployeeId(user.getEmployeeId()).isPresent()){
            throw new ValidationException("Employee ID already exists");
        }

        if(organization.getStatus() == OrganizationStatus.INACTIVE){
            throw new ValidationException("Cannot create user in inactive organization");
        }

        user.setTenantId(organization.getTenantId());
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
    public UserResponseDTO getUserByEmail(String email){
        User user = findByEmail(email);
        securityUtil.validateTenantAccess(user.getTenantId());
        return convertToDTO(user);
    }

    public User findByEmployeeId(String empId){
        return userRepository.findByEmployeeId(empId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
    public UserResponseDTO getUserByEmployeeId(String empId){
        User user = findByEmployeeId(empId);
        securityUtil.validateTenantAccess(user.getTenantId());
        return convertToDTO(user);
    }

    public UserResponseDTO updateUser(String empId, User user){
        securityUtil.validateActiveUser();
        User oldUser = findByEmployeeId(empId);
        securityUtil.validateTenantAccess(oldUser.getTenantId());

        UserRole currentRole = securityUtil.getCurrentUserRole();

        if(user.getIsActive() != null && !user.getIsActive()
                && oldUser.getEmployeeId().equals(securityUtil.getCurrentEmployeeId())){
            throw new ValidationException("You cannot deactivate yourself");
        }

        if(user.getRole() != null){
            validateUserUpdateRole(currentRole, user.getRole());
        }

        if(oldUser.getRole() == UserRole.SUPER_ADMIN){
            throw new ValidationException("Cannot modify SUPER_ADMIN");
        }

        if(currentRole == UserRole.MANAGER && oldUser.getRole() != UserRole.EMPLOYEE){
            throw new ValidationException("Manager can only modify employees");
        }

        boolean canUpdateActiveStatus = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.ORG_ADMIN;

        oldUser.setFirstName(
                user.getFirstName() != null && !user.getFirstName().isBlank() ?
                        user.getFirstName() : oldUser.getFirstName());
        oldUser.setLastName(
                user.getLastName() != null && !user.getLastName().isBlank() ?
                        user.getLastName() : oldUser.getLastName());

        if(user.getEmail() != null && !user.getEmail().equals(oldUser.getEmail())){

            if(userRepository.findByEmail(user.getEmail()).isPresent()){
                throw new ValidationException("Email already exists");
            }
        }
        oldUser.setEmail(
                user.getEmail() != null && !user.getEmail().isBlank() ?
                        user.getEmail() : oldUser.getEmail());

        oldUser.setDesignation(
                user.getDesignation() != null && !user.getDesignation().isBlank() ?
                        user.getDesignation() : oldUser.getDesignation());

        if(user.getRole() != null){
            oldUser.setRole(user.getRole());
        }

        if(canUpdateActiveStatus && user.getIsActive() != null){
            oldUser.setIsActive(user.getIsActive());
        }

        oldUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(oldUser);
        return convertToDTO(updatedUser);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public List<User> allTenantUser(String tenantId){
        return userRepository.findByTenantId(tenantId);
    }
    public List<UserResponseDTO> getMyOrganizationUsers(){
        securityUtil.validateActiveUser();
        String tenantId = securityUtil.getCurrentTenantId();
        List<User> users = allTenantUser(tenantId);
        return convertToDTOList(users);
    }

    public UserResponseDTO deactivateUser(String empId){
        User user = findByEmployeeId(empId);
        if(!user.getIsActive()){
            throw new ValidationException("User is already inactive");
        }

        securityUtil.validateActiveUser();
        securityUtil.validateTenantAccess(user.getTenantId());

        UserRole currentRole = securityUtil.getCurrentUserRole();

        // Cannot deactivate yourself
        if(user.getEmployeeId().equals(securityUtil.getCurrentEmployeeId())){
            throw new ValidationException("You cannot deactivate yourself");
        }

        // Protect SUPER_ADMIN
        if(user.getRole() == UserRole.SUPER_ADMIN){
            throw new ValidationException("Cannot deactivate SUPER_ADMIN");
        }

        // ORG_ADMIN restrictions
        if(currentRole == UserRole.ORG_ADMIN && user.getRole() == UserRole.ORG_ADMIN){
            throw new ValidationException("Cannot deactivate ORG_ADMIN");
        }

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserResponseDTO activateUser(String empId){
        User user = findByEmployeeId(empId);

        if(user.getIsActive()){
            throw new ValidationException("User is already active");
        }
        securityUtil.validateActiveUser();
        securityUtil.validateTenantAccess(user.getTenantId());

        UserRole currentRole = securityUtil.getCurrentUserRole();

        // ORG_ADMIN restrictions
        if(currentRole == UserRole.ORG_ADMIN && user.getRole() == UserRole.ORG_ADMIN){
            throw new ValidationException("Cannot activate ORG_ADMIN");
        }

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserResponseDTO convertToDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setEmployeeId(user.getEmployeeId());
        response.setDesignation(user.getDesignation());
        response.setRole(user.getRole());
        response.setOrganizationCode(user.getOrganizationCode());
        response.setIsActive(user.getIsActive());
        return response;
    }

    public List<UserResponseDTO> convertToDTOList(List<User> users) {
        List<UserResponseDTO> responseList = new ArrayList<>();
        for (User user : users) {
            responseList.add(convertToDTO(user));
        }
        return responseList;
    }
}
