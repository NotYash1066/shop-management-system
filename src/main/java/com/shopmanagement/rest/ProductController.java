package com.shopmanagement.rest;

import com.shopmanagement.entity.Product;
import com.shopmanagement.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Base permission for this controller
@Transactional
public class ProductController {
	@Autowired
	private ProductRepository productRepository;
    
    @Autowired
    private com.shopmanagement.repository.ShopRepository shopRepository;

	@GetMapping
	public List<Product> getAllProducts() {
        Long shopId = getCurrentShopId();
		return productRepository.findByShopId(shopId);
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
        Long shopId = getCurrentShopId();
		return productRepository.findByIdAndShopId(id, shopId).orElseThrow(() -> new RuntimeException("Product not found"));
	}

	@GetMapping("/category/{categoryId}")
	public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        Long shopId = getCurrentShopId();
		return productRepository.findByCategoryIdAndShopId(categoryId, shopId);
	}

	@PostMapping
    @PreAuthorize("hasRole('ADMIN')")
	public Product createProduct(@RequestBody Product product) {
        Long shopId = getCurrentShopId();
        product.setShop(shopRepository.getReferenceById(shopId));
		return productRepository.save(product);
	}

	@PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Long shopId = getCurrentShopId();
        Product existing = productRepository.findByIdAndShopId(id, shopId).orElseThrow(() -> new RuntimeException("Product not found"));
		product.setId(id);
        product.setShop(shopRepository.getReferenceById(shopId)); // Ensure shop doesn't change or is set
		return productRepository.save(product);
	}

	@DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public String deleteProduct(@PathVariable Long id) {
        Long shopId = getCurrentShopId();
        Product existing = productRepository.findByIdAndShopId(id, shopId).orElseThrow(() -> new RuntimeException("Product not found"));
		productRepository.deleteById(id);
		return "Product deleted";
	}
    
    private Long getCurrentShopId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.shopmanagement.security.services.UserDetailsImpl userDetails = (com.shopmanagement.security.services.UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getShopId();
    }
}