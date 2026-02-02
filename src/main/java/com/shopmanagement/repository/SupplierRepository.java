package com.shopmanagement.repository;

import com.shopmanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
	Supplier findByNameAndShopId(String name, Long shopId);

	Supplier findByIdAndShopId(Long id, Long shopId);
    
    List<Supplier> findByShopId(Long shopId);
}