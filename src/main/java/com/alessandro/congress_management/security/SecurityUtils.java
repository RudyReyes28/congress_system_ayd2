package com.alessandro.congress_management.security;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
@Component
public class SecurityUtils {


    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        return authentication.getName();
    }


    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails.getIdUser();
    }


    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        throw new ClassCastException(
                "El principal no es una instancia de CustomUserDetails. " +
                        "Tipo encontrado: " + principal.getClass().getName()
        );
    }


    public static boolean hasRole(String roleName) {
        try {
            Authentication authentication = getAuthentication();

            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }

            final String roleToCheck = roleName;

            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(roleToCheck));
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isOwner(Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            return currentUserId.equals(userId);
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated();
        } catch (Exception e) {
            return false;
        }
    }

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }

        return authentication;
    }
}