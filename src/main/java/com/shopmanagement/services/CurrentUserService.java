package com.shopmanagement.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shopmanagement.security.services.UserDetailsImpl;

@Service
public class CurrentUserService {

    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new AccessDeniedException("Authenticated user context is required");
        }
        return userDetails;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Long getCurrentShopId() {
        Long shopId = getCurrentUser().getShopId();
        if (shopId == null) {
            throw new AccessDeniedException("Authenticated user is not linked to a shop");
        }
        return shopId;
    }

    public boolean hasAuthority(String authority) {
        return getCurrentUser().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
