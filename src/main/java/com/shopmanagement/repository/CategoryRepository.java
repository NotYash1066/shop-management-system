package com.shopmanagement.repository;

import com.shopmanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Category findByName(String name);

	Category findByIdAndName(Long id, String name);

	Category findByIdAndNameAndProductsIsNotNull(Long id, String name);
}