package com.shopmanagement.repository;

import com.shopmanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
	Supplier findByName(String name);

	Supplier findByIdAndName(Long id, String name);

	Supplier findByIdAndNameAndProductsIsNotNull(Long id, String name);
}