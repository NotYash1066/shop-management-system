package com.shopmanagement.services;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.shopmanagement.config.CacheConfig;

@Service
public class InventoryCacheService {

    private final CacheManager cacheManager;

    public InventoryCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictProduct(Long shopId, Long productId, String sku) {
        evict(CacheConfig.INVENTORY_PRODUCT_BY_ID_CACHE, shopId + ":id:" + productId);
        if (sku != null && !sku.isBlank()) {
            evict(CacheConfig.INVENTORY_PRODUCT_BY_SKU_CACHE, shopId + ":sku:" + sku.toLowerCase());
        }
    }

    private void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
