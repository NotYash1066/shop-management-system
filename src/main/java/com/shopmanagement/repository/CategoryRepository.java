package com.shopmanagement.repository;

import com.shopmanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Category findByNameAndShopId(String name, Long shopId);

	Category findByIdAndShopId(Long id, Long shopId);
    
    List<Category> findByShopId(Long shopId);
}