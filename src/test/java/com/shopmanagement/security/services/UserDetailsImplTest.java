package com.shopmanagement.security.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.shopmanagement.entity.Shop;
import com.shopmanagement.entity.User;
import com.shopmanagement.security.Permission;

class UserDetailsImplTest {

    @Test
    void buildShouldExposeAdminRoleAndPermissions() {
        User user = new User("admin", "encoded-password", "ADMIN");
        user.setId(1L);
        user.setEmail("admin@example.com");
        Shop shop = new Shop("Flagship", "owner@example.com");
        shop.setId(10L);
        user.setShop(shop);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        assertThat(userDetails.getShopId()).isEqualTo(10L);
        assertThat(userDetails.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("ROLE_ADMIN", Permission.INVENTORY_WRITE, Permission.DASHBOARD_READ);
    }

    @Test
    void buildShouldExposeUserRoleWithRestrictedPermissions() {
        User user = new User("staff", "encoded-password", "USER");
        user.setId(2L);
        user.setEmail("staff@example.com");

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        assertThat(userDetails.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("ROLE_USER", Permission.ORDER_CREATE, Permission.INVENTORY_READ)
                .doesNotContain(Permission.INVENTORY_WRITE, Permission.DASHBOARD_READ);
    }
}
