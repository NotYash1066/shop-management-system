package com.shopmanagement.rest;

import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.services.CurrentUserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public DashboardController(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Long shopId = currentUserService.getCurrentShopId();
        Map<String, Object> stats = new HashMap<>();

        Double totalRevenue = orderRepository.sumTotalRevenueByShopId(shopId);
        Long totalOrders = orderRepository.countTotalOrdersByShopId(shopId);

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("totalOrders", totalOrders);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<?> getLowStockProducts() {
        Long shopId = currentUserService.getCurrentShopId();
        return ResponseEntity.ok(productRepository.findByShopIdAndStockQuantityLessThanOrderByStockQuantityAsc(shopId, 10));
    }
}
