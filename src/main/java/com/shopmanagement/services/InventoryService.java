package com.shopmanagement.services;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.config.CacheConfig;
import com.shopmanagement.dto.ProductResponseDTO;
import com.shopmanagement.entity.Category;
import com.shopmanagement.entity.Product;
import com.shopmanagement.entity.Supplier;
import com.shopmanagement.repository.CategoryRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.repository.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final CurrentUserService currentUserService;
    private final InventoryCacheService inventoryCacheService;

    public InventoryService(
            ProductRepository productRepository,
            ShopRepository shopRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            CurrentUserService currentUserService,
            InventoryCacheService inventoryCacheService) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.currentUserService = currentUserService;
        this.inventoryCacheService = inventoryCacheService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findProductResponsesByShopId(currentUserService.getCurrentShopId());
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.INVENTORY_PRODUCT_BY_ID_CACHE,
            key = "#root.target.cacheKeyById(#id)",
            sync = true)
    public ProductResponseDTO getProductById(Long id) {
        return productRepository.findProductResponseByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.INVENTORY_PRODUCT_BY_SKU_CACHE,
            key = "#root.target.cacheKeyBySku(#sku)",
            sync = true)
    public ProductResponseDTO getProductBySku(String sku) {
        return productRepository.findProductResponseBySkuAndShopId(sku, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findProductResponsesByCategoryIdAndShopId(categoryId, currentUserService.getCurrentShopId());
    }

    @Transactional
    public ProductResponseDTO createProduct(Product product) {
        Long shopId = currentUserService.getCurrentShopId();
        validateUniqueSku(product.getSku(), shopId, null);
        product.setShop(shopRepository.getReferenceById(shopId));
        product.setCategory(resolveCategoryForShop(product.getCategory(), shopId));
        product.setSupplier(resolveSupplierForShop(product.getSupplier(), shopId));
        Product savedProduct = productRepository.save(product);
        return getProductById(savedProduct.getId());
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, Product product) {
        Long shopId = currentUserService.getCurrentShopId();
        Product existingProduct = productRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        validateUniqueSku(product.getSku(), shopId, id);
        product.setId(id);
        product.setShop(shopRepository.getReferenceById(shopId));
        product.setCategory(resolveCategoryForShop(product.getCategory(), shopId));
        product.setSupplier(resolveSupplierForShop(product.getSupplier(), shopId));
        productRepository.save(product);
        inventoryCacheService.evictProduct(shopId, id, existingProduct.getSku());
        inventoryCacheService.evictProduct(shopId, id, product.getSku());
        return getProductById(id);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Long shopId = currentUserService.getCurrentShopId();
        Product existingProduct = productRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        productRepository.deleteById(id);
        inventoryCacheService.evictProduct(shopId, id, existingProduct.getSku());
    }

    public String cacheKeyById(Long id) {
        return currentUserService.getCurrentShopId() + ":id:" + id;
    }

    public String cacheKeyBySku(String sku) {
        return currentUserService.getCurrentShopId() + ":sku:" + sku.toLowerCase();
    }

    private void validateUniqueSku(String sku, Long shopId, Long currentProductId) {
        if (sku == null || sku.isBlank()) {
            return;
        }

        boolean skuExists = currentProductId == null
                ? productRepository.existsBySkuIgnoreCaseAndShopId(sku, shopId)
                : productRepository.existsBySkuIgnoreCaseAndShopIdAndIdNot(sku, shopId, currentProductId);
        if (skuExists) {
            throw new IllegalArgumentException("SKU must be unique within a shop");
        }
    }

    private Category resolveCategoryForShop(Category category, Long shopId) {
        if (category == null || category.getId() == null) {
            return null;
        }

        Category resolvedCategory = categoryRepository.findByIdAndShopId(category.getId(), shopId);
        if (resolvedCategory == null) {
            throw new EntityNotFoundException("Category not found");
        }
        return resolvedCategory;
    }

    private Supplier resolveSupplierForShop(Supplier supplier, Long shopId) {
        if (supplier == null || supplier.getId() == null) {
            return null;
        }

        Supplier resolvedSupplier = supplierRepository.findByIdAndShopId(supplier.getId(), shopId);
        if (resolvedSupplier == null) {
            throw new EntityNotFoundException("Supplier not found");
        }
        return resolvedSupplier;
    }
}
