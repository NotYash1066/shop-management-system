package com.shopmanagement.rest;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.dto.ProductResponseDTO;
import com.shopmanagement.entity.Product;
import com.shopmanagement.services.InventoryService;

@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasAuthority('inventory:read')")
public class ProductController {

    private final InventoryService inventoryService;

    public ProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<ProductResponseDTO> getAllProducts() {
        return inventoryService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable Long id) {
        return inventoryService.getProductById(id);
    }

    @GetMapping("/sku/{sku}")
    public ProductResponseDTO getProductBySku(@PathVariable String sku) {
        return inventoryService.getProductBySku(sku);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDTO> getProductsByCategory(@PathVariable Long categoryId) {
        return inventoryService.getProductsByCategory(categoryId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:write')")
    public ProductResponseDTO createProduct(@RequestBody Product product) {
        return inventoryService.createProduct(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:write')")
    public ProductResponseDTO updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return inventoryService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:write')")
    public String deleteProduct(@PathVariable Long id) {
        inventoryService.deleteProduct(id);
        return "Product deleted";
    }
}
