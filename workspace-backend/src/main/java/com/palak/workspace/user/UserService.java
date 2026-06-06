package com.palak.workspace.user;

import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import com.palak.workspace.organization.OrganizationStatus;
import com.palak.workspace.security.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;

    public UserService(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository =organizationRepository;
    }

    public User createUser(User user){

        User userByEmail = findByEmail(user.getEmail());
        User userById = findByEmployeeId(user.getEmployeeId());

        if(userById != null || userByEmail != null){
            throw new RuntimeException("User already exists");
        }

        Organization organization = organizationRepository.findByOrganizationCode(user.getOrganizationCode())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if(organization.getStatus() == OrganizationStatus.INACTIVE){
            throw new RuntimeException("Cannot create user in inactive organization");
        }

        user.setTenantId(organization.getTenantId());
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()){
            return user.get();
        }else{
            return null;
        }
    }

    public User findByEmployeeId(String empId){
        Optional<User> user = userRepository.findByEmployeeId(empId);
        if(user.isPresent()){
            return user.get();
        }else{
            return null;
        }
    }

    public User updateUser(String empId, User user, boolean canUpdateActiveStatus){
        User oldUser = findByEmployeeId(empId);
        if (oldUser != null){

           oldUser.setFirstName(
                    user.getFirstName() != null && !user.getFirstName().isBlank() ?
                            user.getFirstName() : oldUser.getFirstName());
           oldUser.setLastName(
                   user.getLastName() != null && !user.getLastName().isBlank() ?
                           user.getLastName() : oldUser.getLastName());
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

           return userRepository.save(oldUser);
        }
        return null;
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public List<User> allTenantUser(String tenantId){
        return userRepository.findByTenantId(tenantId);
    }

    public User deactivateUser(String empId){

        User user = findByEmployeeId(empId);

        if(user != null){
            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }

    public User activateUser(String empId){

        User user = findByEmployeeId(empId);

        if(user != null){
            user.setIsActive(true);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
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
