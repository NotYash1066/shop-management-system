package com.shopmanagement.services.impl;

import com.shopmanagement.dto.OrderItemRequestDTO;
import com.shopmanagement.dto.OrderRequestDTO;
import com.shopmanagement.dto.OrderResponseDTO;
import com.shopmanagement.entity.*;
import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.security.Permission;
import com.shopmanagement.services.CurrentUserService;
import com.shopmanagement.event.StockReconciliationEvent;
import com.shopmanagement.services.OrderService;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.shopmanagement.services.AuditService auditService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ensure the operation is for the correct shop
        Long currentShopId = currentUserService.getCurrentShopId();
        if (!user.getShop().getId().equals(currentShopId)) {
             throw new RuntimeException("Operation not allowed for this shop");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShop(user.getShop()); // Link order to user's shop
        order.setTotalAmount(0.0);
        order.setStatus(OrderStatus.PENDING); // Explicitly set to PENDING

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequestDTO itemRequest : orderRequest.getItems()) {
            // Find product by ID AND Shop ID to prevent buying another shop's product
            Product product = productRepository.findByIdAndShopId(itemRequest.getProductId(), currentShopId)
                    .orElseThrow(() -> new RuntimeException("Product not found or not available in this shop: " + itemRequest.getProductId()));

            // Soft Stock Check (preliminary validation)
            if (product.getStockQuantity() == null || product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Create Order Item (Do NOT deduct stock here)
            OrderItem orderItem = new OrderItem(order, product, itemRequest.getQuantity(), product.getPrice());
            orderItems.add(orderItem);

            totalAmount += product.getPrice() * itemRequest.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Publish event for async stock reconciliation
        eventPublisher.publishEvent(new StockReconciliationEvent(savedOrder.getId()));

        // Log the action
        auditService.log("CREATE", "ORDER", savedOrder.getId(), user.getId(), "Order placed (PENDING): " + totalAmount);

        return toResponse(savedOrder);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        Long currentShopId = currentUserService.getCurrentShopId();
        boolean canViewOtherUsersOrders = currentUserService.hasAuthority(Permission.DASHBOARD_READ);
        if (!canViewOtherUsersOrders && !currentUserService.getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own orders");
        }
        return orderRepository.findByUserIdAndShopId(userId, currentShopId);
    }

    private OrderResponseDTO toResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setReconciliationStartedAt(order.getReconciliationStartedAt());
        response.setReconciliationCompletedAt(order.getReconciliationCompletedAt());
        response.setReconciliationFailureReason(order.getReconciliationFailureReason());
        return response;
    }
}
