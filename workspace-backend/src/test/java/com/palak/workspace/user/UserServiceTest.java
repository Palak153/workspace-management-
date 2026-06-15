package com.palak.workspace.user;

import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.security.SecurityUtil;
import com.palak.workspace.user.UserDTO.CreateUserRequest;
import com.palak.workspace.user.UserDTO.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {

        CreateUserRequest request = new CreateUserRequest();

        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setEmployeeId("EMP001");
        request.setDesignation("Developer");
        request.setRole(UserRole.EMPLOYEE);
        request.setOrganizationCode("ORG001");

        Organization organization = new Organization();

        organization.setOrganizationCode("ORG001");
        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        when(organizationRepository.findByOrganizationCode("ORG001"))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmployeeId("EMP001"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        doNothing()
                .when(securityUtil)
                .validateActiveUser();

        doNothing()
                .when(securityUtil)
                .validateTenantAccess("TENANT001");

        User savedUser = new User();

        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setEmail("john@test.com");
        savedUser.setEmployeeId("EMP001");
        savedUser.setRole(UserRole.EMPLOYEE);
        savedUser.setOrganizationCode("ORG001");
        savedUser.setTenantId("TENANT001");
        savedUser.setIsActive(true);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponseDTO response = userService.createUser(request);

        assertNotNull(response);

        assertEquals("john@test.com", response.getEmail());

        assertEquals(UserRole.EMPLOYEE, response.getRole());

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail() {

        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("john@test.com");
        request.setOrganizationCode("ORG001");
        request.setEmployeeId("EMP001");
        request.setRole(UserRole.EMPLOYEE);

        Organization organization = new Organization();

        organization.setOrganizationCode("ORG001");
        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        when(organizationRepository.findByOrganizationCode("ORG001"))
                .thenReturn(Optional.of(organization));

        User existingUser = new User();

        existingUser.setEmail("john@test.com");

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(existingUser));

        doNothing().when(securityUtil)
                .validateActiveUser();

        doNothing().when(securityUtil)
                .validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.createUser(request));

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmployeeId() {

        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("john@test.com");
        request.setEmployeeId("EMP001");
        request.setOrganizationCode("ORG001");
        request.setRole(UserRole.EMPLOYEE);

        Organization organization = new Organization();

        organization.setOrganizationCode("ORG001");
        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.ACTIVE);

        when(organizationRepository.findByOrganizationCode("ORG001"))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        User existingUser = new User();
        existingUser.setEmployeeId("EMP001");

        when(userRepository.findByEmployeeId("EMP001"))
                .thenReturn(Optional.of(existingUser));

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request));

        assertEquals(
                "Employee ID already exists",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void createUser_inactiveOrganization() {

        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("john@test.com");
        request.setEmployeeId("EMP001");
        request.setOrganizationCode("ORG001");
        request.setRole(UserRole.EMPLOYEE);

        Organization organization = new Organization();

        organization.setOrganizationCode("ORG001");
        organization.setTenantId("TENANT001");
        organization.setStatus(OrganizationStatus.INACTIVE);

        when(organizationRepository.findByOrganizationCode("ORG001"))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmployeeId("EMP001"))
                .thenReturn(Optional.empty());

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request));

        assertEquals(
                "Cannot create user in inactive organization",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void deactivateUser_success() {

        User user = new User();

        user.setEmployeeId("EMP001");
        user.setTenantId("TENANT001");
        user.setRole(UserRole.EMPLOYEE);
        user.setIsActive(true);

        when(userRepository.findByEmployeeId("EMP001"))
                .thenReturn(Optional.of(user));

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentUserRole())
                .thenReturn(UserRole.ORG_ADMIN);

        when(securityUtil.getCurrentEmployeeId())
                .thenReturn("ADMIN001");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponseDTO response = userService.deactivateUser("EMP001");

        assertNotNull(response);

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void deactivateUser_selfDeactivation() {

        User user = new User();

        user.setEmployeeId("EMP001");
        user.setTenantId("TENANT001");
        user.setRole(UserRole.EMPLOYEE);
        user.setIsActive(true);

        when(userRepository.findByEmployeeId("EMP001"))
                .thenReturn(Optional.of(user));

        doNothing().when(securityUtil).validateActiveUser();
        doNothing().when(securityUtil).validateTenantAccess("TENANT001");

        when(securityUtil.getCurrentEmployeeId())
                .thenReturn("EMP001");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.deactivateUser("EMP001"));

        assertEquals(
                "You cannot deactivate yourself",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }
}
