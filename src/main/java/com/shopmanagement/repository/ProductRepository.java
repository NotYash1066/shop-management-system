package com.shopmanagement.repository;

import com.shopmanagement.dto.ProductResponseDTO;
import com.shopmanagement.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByCategoryIdAndShopId(Long categoryId, Long shopId);

	List<Product> findBySupplierIdAndShopId(Long supplierId, Long shopId);
	List<Product> findByShopIdAndStockQuantityLessThanOrderByStockQuantityAsc(Long shopId, Integer threshold);

    List<Product> findByShopId(Long shopId);

    Optional<Product> findByIdAndShopId(Long id, Long shopId);

    Optional<Product> findBySkuIgnoreCaseAndShopId(String sku, Long shopId);

    boolean existsBySkuIgnoreCaseAndShopId(String sku, Long shopId);

    boolean existsBySkuIgnoreCaseAndShopIdAndIdNot(String sku, Long shopId, Long id);

    @Query("""
            select new com.shopmanagement.dto.ProductResponseDTO(
                p.id,
                p.name,
                p.price,
                p.stockQuantity,
                p.sku,
                p.lowStockThreshold,
                c.id,
                c.name,
                s.id,
                s.name,
                p.shop.id
            )
            from Product p
            left join p.category c
            left join p.supplier s
            where p.shop.id = :shopId
            order by p.id
            """)
    List<ProductResponseDTO> findProductResponsesByShopId(@Param("shopId") Long shopId);

    @Query("""
            select new com.shopmanagement.dto.ProductResponseDTO(
                p.id,
                p.name,
                p.price,
                p.stockQuantity,
                p.sku,
                p.lowStockThreshold,
                c.id,
                c.name,
                s.id,
                s.name,
                p.shop.id
            )
            from Product p
            left join p.category c
            left join p.supplier s
            where p.id = :id and p.shop.id = :shopId
            """)
    Optional<ProductResponseDTO> findProductResponseByIdAndShopId(@Param("id") Long id, @Param("shopId") Long shopId);

    @Query("""
            select new com.shopmanagement.dto.ProductResponseDTO(
                p.id,
                p.name,
                p.price,
                p.stockQuantity,
                p.sku,
                p.lowStockThreshold,
                c.id,
                c.name,
                s.id,
                s.name,
                p.shop.id
            )
            from Product p
            left join p.category c
            left join p.supplier s
            where lower(p.sku) = lower(:sku) and p.shop.id = :shopId
            """)
    Optional<ProductResponseDTO> findProductResponseBySkuAndShopId(@Param("sku") String sku, @Param("shopId") Long shopId);

    @Query("""
            select new com.shopmanagement.dto.ProductResponseDTO(
                p.id,
                p.name,
                p.price,
                p.stockQuantity,
                p.sku,
                p.lowStockThreshold,
                c.id,
                c.name,
                s.id,
                s.name,
                p.shop.id
            )
            from Product p
            left join p.category c
            left join p.supplier s
            where c.id = :categoryId and p.shop.id = :shopId
            order by p.id
            """)
    List<ProductResponseDTO> findProductResponsesByCategoryIdAndShopId(
            @Param("categoryId") Long categoryId,
            @Param("shopId") Long shopId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.shop.id = :shopId")
    Optional<Product> findByIdAndShopIdForUpdate(@Param("id") Long id, @Param("shopId") Long shopId);
}
