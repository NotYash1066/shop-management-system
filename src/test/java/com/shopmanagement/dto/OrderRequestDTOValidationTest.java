package com.shopmanagement.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class OrderRequestDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectNegativeNestedItemQuantity() {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(10L);
        item.setQuantity(-5);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1L);
        request.setItems(List.of(item));

        Set<ConstraintViolation<OrderRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("items[0].quantity");
                    assertThat(violation.getMessage()).isEqualTo("Quantity must be at least 1");
                });
    }

    @Test
    void shouldAcceptValidNestedItemQuantity() {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(10L);
        item.setQuantity(2);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1L);
        request.setItems(List.of(item));

        assertThat(validator.validate(request)).isEmpty();
    }
}
