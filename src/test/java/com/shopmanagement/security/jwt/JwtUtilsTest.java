package com.shopmanagement.security.jwt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilsTest {

    @Test
    void shouldRejectBundledDefaultJwtSecretAtStartup() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", JwtUtils.INSECURE_DEFAULT_SECRET);

        assertThatThrownBy(jwtUtils::validateJwtSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("APP_JWT_SECRET must not use the bundled default secret");
    }

    @Test
    void shouldRejectBlankJwtSecretAtStartup() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", " ");

        assertThatThrownBy(jwtUtils::validateJwtSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("APP_JWT_SECRET must be set");
    }
}
