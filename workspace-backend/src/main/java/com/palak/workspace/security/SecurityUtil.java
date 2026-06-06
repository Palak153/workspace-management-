package com.palak.workspace.security;

import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRole;
import com.palak.workspace.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private final UserService userService;

    public SecurityUtil(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userService.findByEmail(email);
    }

    public String getCurrentTenantId(){
        return getCurrentUser().getTenantId();
    }

    public UserRole getCurrentUserRole(){
        return getCurrentUser().getRole();
    }

    public boolean isSuperAdmin(){
        return getCurrentUserRole() == UserRole.SUPER_ADMIN;
    }
}
