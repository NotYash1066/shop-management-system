package com.shopmanagement.repository;

import com.shopmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double sumTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    Long countTotalOrders();
}
