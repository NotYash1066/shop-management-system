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
@PreAuthorize("true")
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
	public Category createCategory(@RequestBody Category category) {
		return categoryRepository.save(category);
	}

	@PutMapping("/{id}")
	public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
		category.setId(id);
		return categoryRepository.save(category);
	}

	@DeleteMapping("/{id}")
	public String deleteCategory(@PathVariable Long id) {
		categoryRepository.deleteById(id);
		return "Category deleted";
	}
}