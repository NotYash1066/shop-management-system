package com.shopmanagement.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopmanagement.entity.Order;
import com.shopmanagement.entity.OrderItem;
import com.shopmanagement.entity.OrderStatus;
import com.shopmanagement.entity.Product;
import com.shopmanagement.entity.Shop;
import com.shopmanagement.entity.User;
import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.services.AuditService;
import com.shopmanagement.services.InventoryCacheService;

@ExtendWith(MockitoExtension.class)
class StockReconciliationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryCacheService inventoryCacheService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private StockReconciliationService stockReconciliationService;

    @Test
    void shouldCompleteOrderAndEvictInventoryCacheWhenInventoryIsAvailable() {
        Shop shop = new Shop("Main", "owner@example.com");
        shop.setId(1L);

        User user = new User("admin", "encoded", "ADMIN");
        user.setId(10L);
        user.setEmail("admin@example.com");
        user.setShop(shop);

        Product product = new Product();
        product.setId(99L);
        product.setName("Laptop");
        product.setSku("LAP-001");
        product.setStockQuantity(10);
        product.setShop(shop);

        Order order = new Order();
        order.setId(50L);
        order.setUser(user);
        order.setShop(shop);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(List.of(new OrderItem(order, product, 3, 1000.0)));

        when(orderRepository.findDetailedById(50L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdAndShopIdForUpdate(99L, 1L)).thenReturn(Optional.of(product));

        stockReconciliationService.reconcileOrder(50L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getReconciliationStartedAt()).isNotNull();
        assertThat(order.getReconciliationCompletedAt()).isNotNull();
        assertThat(order.getReconciliationFailureReason()).isNull();
        assertThat(product.getStockQuantity()).isEqualTo(7);
        verify(productRepository).save(product);
        verify(inventoryCacheService).evictProduct(1L, 99L, "LAP-001");
        verify(orderRepository).save(order);
        verify(auditService).log("RECONCILE", "ORDER", 50L, 10L, "Stock reconciliation completed successfully");
    }

    @Test
    void shouldFailWithoutPersistingStockChangesWhenInventoryIsInsufficient() {
        Shop shop = new Shop("Main", "owner@example.com");
        shop.setId(1L);

        User user = new User("admin", "encoded", "ADMIN");
        user.setId(10L);
        user.setEmail("admin@example.com");
        user.setShop(shop);

        Product product = new Product();
        product.setId(100L);
        product.setName("Monitor");
        product.setSku("MON-001");
        product.setStockQuantity(1);
        product.setShop(shop);

        Order order = new Order();
        order.setId(51L);
        order.setUser(user);
        order.setShop(shop);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(List.of(new OrderItem(order, product, 2, 500.0)));

        when(orderRepository.findDetailedById(51L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdAndShopIdForUpdate(100L, 1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockReconciliationService.reconcileOrder(51L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient stock for product: Monitor");

        verify(productRepository, never()).save(product);
        verify(inventoryCacheService, never()).evictProduct(1L, 100L, "MON-001");
    }

    @Test
    void shouldMarkOrderFailedWithFailureMetadata() {
        Shop shop = new Shop("Main", "owner@example.com");
        shop.setId(1L);

        User user = new User("admin", "encoded", "ADMIN");
        user.setId(10L);
        user.setEmail("admin@example.com");
        user.setShop(shop);

        Order order = new Order();
        order.setId(52L);
        order.setUser(user);
        order.setShop(shop);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findDetailedById(52L)).thenReturn(Optional.of(order));

        stockReconciliationService.markOrderFailed(52L, "Deadlock retry limit exceeded");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(order.getReconciliationStartedAt()).isNotNull();
        assertThat(order.getReconciliationCompletedAt()).isNotNull();
        assertThat(order.getReconciliationFailureReason()).isEqualTo("Deadlock retry limit exceeded");
        verify(orderRepository).save(order);
        verify(auditService).log(
                "RECONCILE",
                "ORDER",
                52L,
                10L,
                "Stock reconciliation failed: Deadlock retry limit exceeded");
    }
}
