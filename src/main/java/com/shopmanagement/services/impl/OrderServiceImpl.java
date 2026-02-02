package com.shopmanagement.services.impl;

import com.shopmanagement.dto.OrderItemRequestDTO;
import com.shopmanagement.dto.OrderRequestDTO;
import com.shopmanagement.dto.OrderResponseDTO;
import com.shopmanagement.entity.*;
import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.services.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    private com.shopmanagement.repository.ShopRepository shopRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Ensure the operation is for the correct shop
        Long currentShopId = getCurrentShopId();
        if (!user.getShop().getId().equals(currentShopId)) {
             throw new RuntimeException("Operation not allowed for this shop");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShop(user.getShop()); // Link order to user's shop
        order.setTotalAmount(0.0);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequestDTO itemRequest : orderRequest.getItems()) {
            // Find product by ID AND Shop ID to prevent buying another shop's product
            Product product = productRepository.findByIdAndShopId(itemRequest.getProductId(), currentShopId)
                    .orElseThrow(() -> new RuntimeException("Product not found or not available in this shop: " + itemRequest.getProductId()));

            // Stock Check
            if (product.getStockQuantity() == null || product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Deduct Stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            // Create Order Item
            OrderItem orderItem = new OrderItem(order, product, itemRequest.getQuantity(), product.getPrice());
            orderItems.add(orderItem);

            totalAmount += product.getPrice() * itemRequest.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        
        // Log the action
        auditService.log("CREATE", "ORDER", savedOrder.getId(), user.getId(), "Order placed with total amount: " + totalAmount);

        // Map to DTO (Manual mapping for now)
        OrderResponseDTO response = new OrderResponseDTO();

        response.setId(savedOrder.getId());
        response.setUserId(savedOrder.getUser().getId());
        response.setTotalAmount(savedOrder.getTotalAmount());
        response.setStatus(savedOrder.getStatus());
        response.setCreatedAt(savedOrder.getCreatedAt());

        return response;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        Long currentShopId = getCurrentShopId();
        return orderRepository.findByUserIdAndShopId(userId, currentShopId);
    }
    
    private Long getCurrentShopId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.shopmanagement.security.services.UserDetailsImpl userDetails = (com.shopmanagement.security.services.UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getShopId();
    }
}
