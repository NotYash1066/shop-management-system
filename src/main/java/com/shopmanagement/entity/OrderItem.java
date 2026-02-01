package com.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;
    private Double priceAtTimeOfSale;

    public OrderItem(Order order, Product product, Integer quantity, Double priceAtTimeOfSale) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtTimeOfSale = priceAtTimeOfSale;
    }
}
