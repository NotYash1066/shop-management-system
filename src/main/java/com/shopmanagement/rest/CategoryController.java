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

	@GetMapping
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@GetMapping("/{id}")
	public Category getCategoryById(@PathVariable Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Category not found"));
	}

	@PostMapping
    @PreAuthorize("hasRole('ADMIN')")
	public Category createCategory(@RequestBody Category category) {
		return categoryRepository.save(category);
	}

	@PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
		category.setId(id);
		return categoryRepository.save(category);
	}

	@DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public String deleteCategory(@PathVariable Long id) {
		categoryRepository.deleteById(id);
		return "Category deleted";
	}
}