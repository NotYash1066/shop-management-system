package com.shopmanagement.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopmanagement.dto.ProductResponseDTO;
import com.shopmanagement.entity.Category;
import com.shopmanagement.entity.Product;
import com.shopmanagement.entity.Shop;
import com.shopmanagement.entity.Supplier;
import com.shopmanagement.repository.CategoryRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.repository.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private InventoryCacheService inventoryCacheService;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void updateProductShouldUseTargetedCacheEvictionAndProjectionLookup() {
        Shop shop = new Shop("Main", "owner@example.com");
        shop.setId(1L);

        Product existingProduct = new Product();
        existingProduct.setId(20L);
        existingProduct.setSku("SKU-OLD");
        existingProduct.setShop(shop);

        Product updatedProduct = new Product();
        updatedProduct.setName("Laptop Pro");
        updatedProduct.setSku("SKU-NEW");
        updatedProduct.setPrice(1499.0);

        ProductResponseDTO response = new ProductResponseDTO(
                20L,
                "Laptop Pro",
                1499.0,
                12,
                "SKU-NEW",
                3,
                null,
                null,
                null,
                null,
                1L);

        when(currentUserService.getCurrentShopId()).thenReturn(1L);
        when(productRepository.findByIdAndShopId(20L, 1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuIgnoreCaseAndShopIdAndIdNot("SKU-NEW", 1L, 20L)).thenReturn(false);
        when(shopRepository.getReferenceById(1L)).thenReturn(shop);
        when(productRepository.findProductResponseByIdAndShopId(20L, 1L)).thenReturn(Optional.of(response));

        ProductResponseDTO updatedResponse = inventoryService.updateProduct(20L, updatedProduct);

        assertThat(updatedResponse).isEqualTo(response);
        verify(productRepository).save(updatedProduct);
        verify(inventoryCacheService).evictProduct(1L, 20L, "SKU-OLD");
        verify(inventoryCacheService).evictProduct(1L, 20L, "SKU-NEW");
    }

    @Test
    void createProductShouldRejectDuplicateSkuWithinShop() {
        Product product = new Product();
        product.setSku("SKU-EXISTS");

        when(currentUserService.getCurrentShopId()).thenReturn(1L);
        when(productRepository.existsBySkuIgnoreCaseAndShopId("SKU-EXISTS", 1L)).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createProduct(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SKU must be unique within a shop");
    }

    @Test
    void createProductShouldRejectCategoryFromAnotherShop() {
        Category category = new Category();
        category.setId(99L);

        Product product = new Product();
        product.setSku("SKU-1");
        product.setCategory(category);

        when(currentUserService.getCurrentShopId()).thenReturn(1L);
        when(productRepository.existsBySkuIgnoreCaseAndShopId("SKU-1", 1L)).thenReturn(false);
        when(shopRepository.getReferenceById(1L)).thenReturn(new Shop("Main", "owner@example.com"));
        when(categoryRepository.findByIdAndShopId(99L, 1L)).thenReturn(null);

        assertThatThrownBy(() -> inventoryService.createProduct(product))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    void createProductShouldRejectSupplierFromAnotherShop() {
        Supplier supplier = new Supplier();
        supplier.setId(88L);

        Product product = new Product();
        product.setSku("SKU-1");
        product.setSupplier(supplier);

        when(currentUserService.getCurrentShopId()).thenReturn(1L);
        when(productRepository.existsBySkuIgnoreCaseAndShopId("SKU-1", 1L)).thenReturn(false);
        when(shopRepository.getReferenceById(1L)).thenReturn(new Shop("Main", "owner@example.com"));
        when(supplierRepository.findByIdAndShopId(88L, 1L)).thenReturn(null);

        assertThatThrownBy(() -> inventoryService.createProduct(product))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Supplier not found");
    }

    @Test
    void createProductShouldResolveCategoryAndSupplierWithinCurrentShop() {
        Shop shop = new Shop("Main", "owner@example.com");
        shop.setId(1L);

        Category requestedCategory = new Category();
        requestedCategory.setId(99L);
        Category resolvedCategory = new Category();
        resolvedCategory.setId(99L);
        resolvedCategory.setShop(shop);

        Supplier requestedSupplier = new Supplier();
        requestedSupplier.setId(88L);
        Supplier resolvedSupplier = new Supplier();
        resolvedSupplier.setId(88L);
        resolvedSupplier.setShop(shop);

        Product product = new Product();
        product.setSku("SKU-1");
        product.setCategory(requestedCategory);
        product.setSupplier(requestedSupplier);

        ProductResponseDTO response = new ProductResponseDTO(
                30L,
                "Laptop",
                999.0,
                10,
                "SKU-1",
                3,
                99L,
                "Electronics",
                88L,
                "Supplier",
                1L);

        when(currentUserService.getCurrentShopId()).thenReturn(1L);
        when(productRepository.existsBySkuIgnoreCaseAndShopId("SKU-1", 1L)).thenReturn(false);
        when(shopRepository.getReferenceById(1L)).thenReturn(shop);
        when(categoryRepository.findByIdAndShopId(99L, 1L)).thenReturn(resolvedCategory);
        when(supplierRepository.findByIdAndShopId(88L, 1L)).thenReturn(resolvedSupplier);
        when(productRepository.save(product)).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(30L);
            return savedProduct;
        });
        when(productRepository.findProductResponseByIdAndShopId(30L, 1L)).thenReturn(Optional.of(response));

        ProductResponseDTO actualResponse = inventoryService.createProduct(product);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(product.getShop()).isEqualTo(shop);
        assertThat(product.getCategory()).isEqualTo(resolvedCategory);
        assertThat(product.getSupplier()).isEqualTo(resolvedSupplier);
    }
}
