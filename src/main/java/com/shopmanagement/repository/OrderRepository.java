package com.shopmanagement.repository;

import com.shopmanagement.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndShopId(Long userId, Long shopId);

    List<Order> findByShopId(Long shopId);

    Optional<Order> findByIdAndShopId(Long id, Long shopId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "shop", "user"})
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findDetailedById(@Param("orderId") Long orderId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.shop.id = :shopId")
    Double sumTotalRevenueByShopId(Long shopId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId")
    Long countTotalOrdersByShopId(Long shopId);
}
