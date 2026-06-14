package com.IusCloud.messaging.config;

import com.IusCloud.messaging.config.redis.PermissionRedisService;
import com.IusCloud.messaging.config.security.JwtService;
import com.IusCloud.messaging.config.security.TenantAuthenticationDetails;
import com.IusCloud.messaging.shared.tenant.RequestContext;
import com.IusCloud.messaging.shared.tenant.TenantContext;
import com.IusCloud.messaging.shared.tenant.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PermissionRedisService permissionRedisService;

    public TenantFilter(JwtService jwtService,
                        PermissionRedisService permissionRedisService) {
        this.jwtService = jwtService;
        this.permissionRedisService = permissionRedisService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        RequestContext.set(resolveClientIp(request), request.getHeader("User-Agent"));

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractClaims(token);

            String tenantId = claims.get("tenantId", String.class);
            if (tenantId == null) {
                throw new SecurityException("JWT missing tenantId claim");
            }

            TenantContext.setTenantId(UUID.fromString(tenantId));
            log.debug("[TenantFilter] tenantId={} sub={} uri={}", tenantId, claims.getSubject(), request.getRequestURI());

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles", List.class);

            String userIdStr = claims.getSubject();
            UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;

            if (userId != null) {
                UserContext.setUserId(userId);

                boolean isAdministrator = roles != null && roles.contains("ADMINISTRATOR");
                UserContext.setRestricted(!isAdministrator);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if (roles != null) {
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            if (userId != null) {
                List<String> permissions = permissionRedisService.getPermissions(
                        UUID.fromString(tenantId), userId);
                for (String permission : permissions) {
                    authorities.add(new SimpleGrantedAuthority(permission));
                }
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities);
            authentication.setDetails(new TenantAuthenticationDetails(tenantId));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            log.warn("JWT authentication failed for {}: {}", request.getRequestURI(), ex.getMessage());
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            UserContext.clear();
            RequestContext.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
