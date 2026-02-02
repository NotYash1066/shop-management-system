package com.shopmanagement.repository;

import com.shopmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByCategoryIdAndShopId(Long categoryId, Long shopId);

	List<Product> findBySupplierIdAndShopId(Long supplierId, Long shopId);
	List<Product> findByShopIdAndStockQuantityLessThan(Long shopId, Integer threshold);
    
    List<Product> findByShopId(Long shopId);
    
    Optional<Product> findByIdAndShopId(Long id, Long shopId);
}