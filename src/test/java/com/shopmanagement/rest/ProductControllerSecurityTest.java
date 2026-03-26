package com.shopmanagement.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.shopmanagement.config.SecurityConfig;
import com.shopmanagement.config.RateLimitInterceptor;
import com.shopmanagement.dto.ProductResponseDTO;
import com.shopmanagement.security.Permission;
import com.shopmanagement.security.jwt.AuthEntryPointJwt;
import com.shopmanagement.security.jwt.JwtUtils;
import com.shopmanagement.security.services.UserDetailsServiceImpl;
import com.shopmanagement.services.InventoryService;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(jwtUtils.validateJwtToken(any())).thenReturn(false);
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void shouldAllowInventoryReadersToGetProducts() throws Exception {
        when(inventoryService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products")
                        .with(user("staff").authorities(new SimpleGrantedAuthority(Permission.INVENTORY_READ))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidProductCreationWithoutWritePermission() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(user("staff").authorities(new SimpleGrantedAuthority(Permission.INVENTORY_READ)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Laptop",
                                  "price": 999.0,
                                  "sku": "SKU-1"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowProductCreationWithWritePermission() throws Exception {
        when(inventoryService.createProduct(any())).thenReturn(new ProductResponseDTO(
                1L,
                "Laptop",
                999.0,
                20,
                "SKU-1",
                5,
                null,
                null,
                null,
                null,
                1L));

        mockMvc.perform(post("/api/products")
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority(Permission.INVENTORY_READ),
                                new SimpleGrantedAuthority(Permission.INVENTORY_WRITE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Laptop",
                                  "price": 999.0,
                                  "sku": "SKU-1"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
