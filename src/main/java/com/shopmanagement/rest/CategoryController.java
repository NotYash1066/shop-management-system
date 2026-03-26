package com.shopmanagement.rest;

import com.shopmanagement.entity.Category;
import com.shopmanagement.repository.CategoryRepository;
import com.shopmanagement.services.CurrentUserService;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@PreAuthorize("hasAuthority('category:read')")
@Transactional
public class CategoryController {

	private final CategoryRepository categoryRepository;
    private final com.shopmanagement.repository.ShopRepository shopRepository;
    private final CurrentUserService currentUserService;

    public CategoryController(
            CategoryRepository categoryRepository,
            com.shopmanagement.repository.ShopRepository shopRepository,
            CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
        this.currentUserService = currentUserService;
    }

	@GetMapping
	public List<Category> getAllCategories() {
        Long shopId = currentUserService.getCurrentShopId();
		return categoryRepository.findByShopId(shopId);
	}

	@GetMapping("/{id}")
	public Category getCategoryById(@PathVariable Long id) {
        Long shopId = currentUserService.getCurrentShopId();
			return categoryRepository.findByIdAndShopId(id, shopId);
	}

	@PostMapping
    @PreAuthorize("hasAuthority('category:write')")
	public Category createCategory(@RequestBody Category category) {
        Long shopId = currentUserService.getCurrentShopId();
        com.shopmanagement.entity.Shop shop = shopRepository.getReferenceById(shopId);
        category.setShop(shop);
		return categoryRepository.save(category);
	}

	@PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:write')")
	public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Long shopId = currentUserService.getCurrentShopId();
        Category existing = categoryRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
            throw new RuntimeException("Category not found");
        }
		category.setId(id);
        category.setShop(shopRepository.getReferenceById(shopId));
		return categoryRepository.save(category);
	}

	@DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:write')")
	public String deleteCategory(@PathVariable Long id) {
        Long shopId = currentUserService.getCurrentShopId();
        Category existing = categoryRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
            throw new RuntimeException("Category not found");
        }
			categoryRepository.deleteById(id);
			return "Category deleted";
	}
}
