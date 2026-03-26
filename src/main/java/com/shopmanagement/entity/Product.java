package com.shopmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_shop", columnList = "shop_id"),
    @Index(name = "idx_product_shop_sku", columnList = "shop_id, sku"),
    @Index(name = "idx_product_shop_category", columnList = "shop_id, category_id"),
    @Index(name = "idx_product_shop_stock", columnList = "shop_id, stockQuantity")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_product_shop_sku", columnNames = {"shop_id", "sku"})
})
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Version
    private Long version;

	private String name;
	private double price;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@ManyToOne
	@JoinColumn(name = "supplier_id")
	private Supplier supplier;

	private Integer stockQuantity;
	private String sku;
	private Integer lowStockThreshold;
}
