package com.shopmanagement.dto;

public record ProductResponseDTO(
        Long id,
        String name,
        double price,
        Integer stockQuantity,
        String sku,
        Integer lowStockThreshold,
        Long categoryId,
        String categoryName,
        Long supplierId,
        String supplierName,
        Long shopId) {
}
