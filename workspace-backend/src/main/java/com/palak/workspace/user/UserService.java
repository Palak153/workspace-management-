package com.palak.workspace.user;

import com.palak.workspace.organization.Organization;
import com.palak.workspace.organization.OrganizationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        Organization organization =
                organizationRepository.findByOrganizationCode(user.getOrganizationCode())
                        .orElseThrow(() -> new RuntimeException("Organization not found"));
        if(userById != null || userByEmail != null){
            throw new RuntimeException("User already Exists");
        }else{
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);
            user.setTenantId(organization.getTenantId());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }
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

    public User updateUser(String empId, User user){
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

           oldUser.setUpdatedAt(LocalDateTime.now());

           return userRepository.save(oldUser);
        }
        return null;
    }

    public List<User> allTenantUser(String tenantId){
        return userRepository.findByTenantId(tenantId);
    }

    public Boolean deleteUser(String empId){
        User user = findByEmployeeId(empId);
        if(user != null){
            userRepository.delete(user);
            return true;
        }
        return false;
    }
}
