package com.palak.workspace.auth;

import com.palak.workspace.user.UserDetailService;
import com.palak.workspace.user.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final UserDetailService userDetailService;
    private final JwtService jwtService;

    public JWTFilter(UserDetailService userDetailService, JwtService jwtService) {
        this.userDetailService = userDetailService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String tenantId =null;
        String employeeId = null;
        String role = null;
        Boolean isActive = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            email = jwtService.extractEmail(jwt);
            tenantId = jwtService.extractTenantId(jwt);
            role = jwtService.extractRole(jwt);
            employeeId = jwtService.extractEmployeeId(jwt);
            isActive = jwtService.extractIsActive(jwt);
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailService.loadUserByUsername(email);
            if (jwtService.validateToken(jwt)) {
                UserPrincipal principal = new UserPrincipal(email,tenantId, employeeId, UserRole.valueOf(role), isActive);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, userDetails.getAuthorities());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
