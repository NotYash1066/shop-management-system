package com.shopmanagement.rest;

import com.shopmanagement.entity.Category;
import com.shopmanagement.repository.CategoryRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@Transactional
public class CategoryController {

	@Autowired
	private CategoryRepository categoryRepository;
    
    @Autowired
    private com.shopmanagement.repository.ShopRepository shopRepository;

	@GetMapping
	public List<Category> getAllCategories() {
        Long shopId = getCurrentShopId();
		return categoryRepository.findByShopId(shopId);
	}

	@GetMapping("/{id}")
	public Category getCategoryById(@PathVariable Long id) {
        Long shopId = getCurrentShopId();
		return categoryRepository.findByIdAndShopId(id, shopId);
        // .orElseThrow(() -> new RuntimeException("Category not found")); // findByIdAndShopId returns Category directly or null? 
        // My repo definition was: Category findByIdAndShopId(Long id, Long shopId); which returns null if not found.
        // Better: Optional<Category> findByIdAndShopId... but I defined it as Category in my previous replacement.
        // Let's assume it returns null and handle it, or better, re-read repo. 
        // I defined: Category findByIdAndShopId(Long id, Long shopId);
	}

	@PostMapping
    @PreAuthorize("hasRole('ADMIN')")
	public Category createCategory(@RequestBody Category category) {
        Long shopId = getCurrentShopId();
        com.shopmanagement.entity.Shop shop = shopRepository.getReferenceById(shopId);
        category.setShop(shop);
		return categoryRepository.save(category);
	}

	@PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Long shopId = getCurrentShopId();
        Category existing = categoryRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
            throw new RuntimeException("Category not found");
        }
		category.setId(id);
        category.setShop(shopRepository.getReferenceById(shopId));
		return categoryRepository.save(category);
	}

	@DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public String deleteCategory(@PathVariable Long id) {
        Long shopId = getCurrentShopId();
        Category existing = categoryRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
            throw new RuntimeException("Category not found");
        }
		categoryRepository.deleteById(id);
		return "Category deleted";
	}
    
    private Long getCurrentShopId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.shopmanagement.security.services.UserDetailsImpl userDetails = (com.shopmanagement.security.services.UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getShopId();
    }
}