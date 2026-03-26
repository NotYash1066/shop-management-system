package com.shopmanagement.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class RoleAuthorities {

    private static final Map<String, List<String>> PERMISSIONS_BY_ROLE = Map.of(
            "ADMIN",
            List.of(
                    Permission.INVENTORY_READ,
                    Permission.INVENTORY_WRITE,
                    Permission.CATEGORY_READ,
                    Permission.CATEGORY_WRITE,
                    Permission.SUPPLIER_READ,
                    Permission.SUPPLIER_WRITE,
                    Permission.EMPLOYEE_READ,
                    Permission.EMPLOYEE_WRITE,
                    Permission.ORDER_CREATE,
                    Permission.ORDER_READ,
                    Permission.DASHBOARD_READ),
            "USER",
            List.of(
                    Permission.INVENTORY_READ,
                    Permission.CATEGORY_READ,
                    Permission.ORDER_CREATE,
                    Permission.ORDER_READ));

    private RoleAuthorities() {
    }

    public static Collection<? extends GrantedAuthority> forRole(String role) {
        String normalizedRole = normalize(role);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
        PERMISSIONS_BY_ROLE.getOrDefault(normalizedRole, List.of())
                .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        return authorities;
    }

    private static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalizedRole = role.toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith("ROLE_")) {
            return normalizedRole.substring("ROLE_".length());
        }
        return normalizedRole;
    }
}
