package com.shopmanagement.repository;

import com.shopmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndShopId(Long userId, Long shopId);
    
    List<Order> findByShopId(Long shopId);
    
    Optional<Order> findByIdAndShopId(Long id, Long shopId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.shop.id = :shopId")
    Double sumTotalRevenueByShopId(Long shopId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId")
    Long countTotalOrdersByShopId(Long shopId);
}
