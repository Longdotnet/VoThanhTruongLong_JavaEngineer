package com.seveneleven.repository;

import com.seveneleven.entity.Product;
import com.seveneleven.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    /**
     * Check if product is referenced by any orders
     * Used before hard delete to prevent orphaned order items
     */
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.product.id = :productId")
    boolean isReferencedByOrders(@Param("productId") Long productId);
}
