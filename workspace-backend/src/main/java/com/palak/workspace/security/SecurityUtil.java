package com.palak.workspace.security;

import com.palak.workspace.auth.UserPrincipal;
import com.palak.workspace.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private UserPrincipal getPrincipal(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (UserPrincipal) authentication.getPrincipal();
    }

    public String getCurrentEmail(){
        return getPrincipal().getEmail();
    }

    public String getCurrentTenantId(){
        return getPrincipal().getTenantId();
    }

    public String getCurrentEmployeeId(){
        return getPrincipal().getEmployeeId();
    }

    public UserRole getCurrentUserRole(){
        return getPrincipal().getRole();
    }

    public boolean isSuperAdmin(){
        return getCurrentUserRole() == UserRole.SUPER_ADMIN;
    }

    public boolean hasTenantAccess(String tenantId){
        return isSuperAdmin()
                || tenantId.equals(getCurrentTenantId());
    }

    public void validateTenantAccess(String tenantId){
        if(!hasTenantAccess(tenantId)){
            throw new RuntimeException("Access denied");
        }
    }

    public void validateActiveUser(){
        if(!Boolean.TRUE.equals(
                getPrincipal().getIsActive())){
            throw new RuntimeException("Inactive user");
        }
    }
}