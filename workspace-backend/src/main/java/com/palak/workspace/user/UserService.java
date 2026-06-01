package com.palak.workspace.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public User createUser(User user){
        User userByEmail = findByEmail(user.getEmail());
        User userById = findByEmployeeId(user.getEmployeeId());

        if(userById != null || userByEmail != null){
            throw new RuntimeException("User already Exists");
        }else{
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);
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
}
