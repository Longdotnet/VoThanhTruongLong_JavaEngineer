package com.seveneleven.mapper;

import com.seveneleven.dto.ProductRequest;
import com.seveneleven.dto.ProductResponse;
import com.seveneleven.entity.Product;
import com.seveneleven.entity.ProductStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper for Product entity and DTOs
 * Separates mapping logic from service layer (Single Responsibility)
 */
@Component
public class ProductMapper {

    /**
     * Convert Product entity to ProductResponse DTO
     */
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Convert ProductRequest DTO to Product entity
     */
    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .status(ProductStatus.ACTIVE)
                .build();
    }

    /**
     * Update existing Product entity from ProductRequest DTO
     */
    public void updateEntity(Product product, ProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
    }
}

