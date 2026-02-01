package com.shopmanagement.services;

import com.shopmanagement.dto.OrderRequestDTO;
import com.shopmanagement.dto.OrderResponseDTO;
import com.shopmanagement.entity.Order;
import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);
    List<Order> getOrdersByUserId(Long userId);
}
