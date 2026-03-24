package com.shopmanagement.services.impl;

import com.shopmanagement.entity.Order;
import com.shopmanagement.entity.OrderItem;
import com.shopmanagement.entity.OrderStatus;
import com.shopmanagement.entity.Product;
import com.shopmanagement.event.StockReconciliationEvent;
import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockEventListener {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Async
    @EventListener
    @Transactional
    public void handleStockReconciliation(StockReconciliationEvent event) {
        log.info("Processing stock reconciliation for order ID: {}", event.getOrderId());
        
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} is not in PENDING status. Skipping reconciliation.", order.getId());
            return;
        }

        try {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                int requestedQuantity = item.getQuantity();

                // Double Check Stock (Strict deduction)
                if (product.getStockQuantity() == null || product.getStockQuantity() < requestedQuantity) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                // Deduct Stock
                product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
                productRepository.save(product);
            }

            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("Stock reconciliation successful for order ID: {}", order.getId());

        } catch (Exception e) {
            log.error("Stock reconciliation failed for order ID: {}. Error: {}", order.getId(), e.getMessage());
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }
}
