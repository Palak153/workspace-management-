package com.palak.workspace.user;

import com.palak.workspace.common.exception.AccessDeniedException;
import com.palak.workspace.common.exception.ResourceNotFoundException;
import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.user.UserDTO.CreateUserRequest;
import com.palak.workspace.user.UserDTO.UpdateUserRequest;
import com.palak.workspace.user.UserDTO.UserResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
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
            if(targetRole == UserRole.ORG_ADMIN){
                throw new ValidationException("ORG_ADMIN cannot create another ORG_ADMIN");
            }
        }
    }

    private void validateUserUpdateRole(UserRole currentRole, UserRole targetRole){
        if(currentRole == UserRole.ORG_ADMIN){
            if(targetRole == UserRole.ORG_ADMIN){
                throw new ValidationException("ORG_ADMIN cannot update role to ORG_ADMIN");
            }
        }
    }


    public UserResponseDTO createUser(CreateUserRequest request){

        securityUtil.validateActiveUser();

        Organization organization = organizationRepository
                .findByOrganizationCode(request.getOrganizationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        securityUtil.validateTenantAccess(organization.getTenantId());

        validateUserCreationRole(securityUtil.getCurrentUserRole(), request.getRole());

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new ValidationException("Email already exists");
        }

        if(userRepository.findByEmployeeId(request.getEmployeeId()).isPresent()){
            throw new ValidationException("Employee ID already exists");
        }

        if(organization.getStatus() == OrganizationStatus.INACTIVE){
            throw new ValidationException("Cannot create user in inactive organization");
        }

        if(request.getRole() == UserRole.SUPER_ADMIN){
            throw new ValidationException("SUPER_ADMIN cannot be created through API");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setDesignation(request.getDesignation());
        user.setEmployeeId(request.getEmployeeId());

        user.setOrganizationCode(request.getOrganizationCode());
        user.setTenantId(organization.getTenantId());
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info(
                "User created. employeeId={}, role={}, tenantId={}",
                savedUser.getEmployeeId(),
                savedUser.getRole(),
                savedUser.getTenantId());
        return convertToDTO(savedUser);
    }


    public User findByEmail(String email){
        log.info("Fetching user from DB: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
    @Cacheable(value = "usersByEmail", key = "#email")
    public UserResponseDTO getUserByEmail(String email){
        User user = findByEmail(email);
        securityUtil.validateTenantAccess(user.getTenantId());
        return convertToDTO(user);
    }


    public User findByEmployeeId(String empId){
        log.info("Fetching user from DB: {}", empId);
        return userRepository.findByEmployeeId(empId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    @Cacheable(value = "usersByEmployeeId", key = "#empId")
    public UserResponseDTO getUserByEmployeeId(String empId){
        User user = findByEmployeeId(empId);
        securityUtil.validateTenantAccess(user.getTenantId());
        return convertToDTO(user);
    }

    @CacheEvict(value = {"usersByEmail", "usersByEmployeeId"}, allEntries = true)
    public UserResponseDTO updateUser(String empId, UpdateUserRequest user){
        securityUtil.validateActiveUser();
        User oldUser = findByEmployeeId(empId);
        securityUtil.validateTenantAccess(oldUser.getTenantId());

        UserRole currentRole = securityUtil.getCurrentUserRole();

        if(user.getRole() == UserRole.SUPER_ADMIN){
            throw new ValidationException("Role cannot be changed to SUPER_ADMIN");
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

        String currentEmpId = securityUtil.getCurrentEmployeeId();

        if(currentRole == UserRole.ORG_ADMIN && currentEmpId.equals(oldUser.getEmployeeId())
                && user.getRole() != null) {

            throw new ValidationException(
                    "ORG_ADMIN cannot update his/her own role");
        }

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

        oldUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(oldUser);
        log.info(
                "User updated. employeeId={}",
                updatedUser.getEmployeeId());
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

    @CacheEvict(value = {"usersByEmail", "usersByEmployeeId"}, allEntries = true)
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
        log.warn(
                "User deactivated. employeeId={}, deactivatedBy={}",
                user.getEmployeeId(),
                securityUtil.getCurrentEmployeeId());
        return convertToDTO(updatedUser);
    }

    @CacheEvict(value = {"usersByEmail", "usersByEmployeeId"}, allEntries = true)
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
        log.info(
                "User activated. employeeId={}, activatedBy={}",
                user.getEmployeeId(),
                securityUtil.getCurrentEmployeeId());
        return convertToDTO(updatedUser);
    }

    public List<UserResponseDTO> getAllUsers(){
        if(!securityUtil.isSuperAdmin()){
            throw new AccessDeniedException("Only SUPER_ADMIN can view all users");
        }
        return convertToDTOList(userRepository.findAll());
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
