package com.shopmanagement.services.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.entity.Order;
import com.shopmanagement.entity.OrderItem;
import com.shopmanagement.entity.OrderStatus;
import com.shopmanagement.entity.Product;
import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.services.AuditService;
import com.shopmanagement.services.InventoryCacheService;

@Service
public class StockReconciliationService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryCacheService inventoryCacheService;
    private final AuditService auditService;

    public StockReconciliationService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            InventoryCacheService inventoryCacheService,
            AuditService auditService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryCacheService = inventoryCacheService;
        this.auditService = auditService;
    }

    @Transactional
    public void reconcileOrder(Long orderId) {
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        order.setReconciliationStartedAt(startedAt);
        order.setReconciliationCompletedAt(null);
        order.setReconciliationFailureReason(null);

        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findByIdAndShopIdForUpdate(
                            item.getProduct().getId(),
                            order.getShop().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            int requestedQuantity = item.getQuantity();

            if (product.getStockQuantity() == null || product.getStockQuantity() < requestedQuantity) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            productRepository.save(product);
            inventoryCacheService.evictProduct(order.getShop().getId(), product.getId(), product.getSku());
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setReconciliationCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
        auditService.log(
                "RECONCILE",
                "ORDER",
                order.getId(),
                order.getUser().getId(),
                "Stock reconciliation completed successfully");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOrderFailed(Long orderId, String reason) {
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.FAILED);
        if (order.getReconciliationStartedAt() == null) {
            order.setReconciliationStartedAt(LocalDateTime.now());
        }
        order.setReconciliationCompletedAt(LocalDateTime.now());
        order.setReconciliationFailureReason(reason);
        orderRepository.save(order);
        auditService.log(
                "RECONCILE",
                "ORDER",
                order.getId(),
                order.getUser().getId(),
                "Stock reconciliation failed: " + reason);
    }
}
