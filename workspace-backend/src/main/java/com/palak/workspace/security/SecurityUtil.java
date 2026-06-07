package com.palak.workspace.security;

import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRole;
import com.palak.workspace.user.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private final UserService userService;

    public SecurityUtil(@Lazy UserService userService) {
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

    public boolean hasTenantAccess(String tenantId){
        return isSuperAdmin() || tenantId.equals(getCurrentTenantId());
    }

    public void validateTenantAccess(String tenantId){
        if(!hasTenantAccess(tenantId)){
            throw new RuntimeException("Access denied");
        }
    }

    public void validateActiveUser(){
        if(!getCurrentUser().getIsActive()){
            throw new RuntimeException("Inactive user");
        }
    }
}
