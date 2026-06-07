package com.shopmanagement.services.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.shopmanagement.dto.OrderItemRequestDTO;
import com.shopmanagement.dto.OrderRequestDTO;
import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.services.AuditService;
import com.shopmanagement.services.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldRejectNegativeQuantityBeforePersistence() {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(10L);
        item.setQuantity(-5);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1L);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be at least 1");

        verify(userRepository, never()).findById(1L);
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
