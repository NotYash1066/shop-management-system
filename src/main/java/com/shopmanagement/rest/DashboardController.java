package com.shopmanagement.rest;

import com.shopmanagement.repository.OrderRepository;
import com.shopmanagement.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Double totalRevenue = orderRepository.sumTotalRevenue();
        Long totalOrders = orderRepository.countTotalOrders();

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("totalOrders", totalOrders);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts() {
        // Hardcoded threshold of 10 for now, or use a query param
        return ResponseEntity.ok(productRepository.findByStockQuantityLessThan(10));
    }
}
