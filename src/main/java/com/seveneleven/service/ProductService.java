package com.seveneleven.service;

import com.seveneleven.dto.ProductRequest;
import com.seveneleven.dto.ProductResponse;
import com.seveneleven.entity.Product;
import com.seveneleven.entity.ProductStatus;
import com.seveneleven.exception.BusinessException;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.mapper.ProductMapper;
import com.seveneleven.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Product business logic
 * Handles product operations with proper transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Retrieve all products
     */
    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all products from database");
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve only active products
     */
    public List<ProductResponse> getActiveProducts() {
        log.debug("Fetching active products from database");
        return productRepository.findByStatus(ProductStatus.ACTIVE).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve product by ID
     */
    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = findProductEntityById(id);
        return productMapper.toResponse(product);
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        validateSkuUniqueness(request.getSku(), null);

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Update existing product
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = findProductEntityById(id);
        validateSkuUniqueness(request.getSku(), id);

        productMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with id: {}", id);
        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Soft delete product by deactivating
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deactivating product with id: {}", id);

        Product product = findProductEntityById(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);

        log.info("Product deactivated successfully with id: {}", id);
    }

    /**
     * Retrieve all products with pagination (admin)
     */
    public Page<ProductResponse> getAllProductsPaged(int page, int size) {
        log.debug("Fetching products page {} size {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    /**
     * Retrieve active products with pagination (user)
     */
    @Transactional
    public void hardDeleteProduct(Long id) {
        log.warn("Attempting HARD DELETE for product with id: {}", id);

        Product product = findProductEntityById(id);

        // Check if product is referenced by any orders
        if (productRepository.isReferencedByOrders(id)) {
            log.error("Cannot hard delete product {}: Referenced by existing orders", id);
            throw new BusinessException(
                String.format("Cannot permanently delete product '%s': It is referenced by existing orders. " +
                             "You can use 'Soft Delete' instead to deactivate it while preserving order history.",
                             product.getName())
            );
        }

        // Safe to delete - no orders reference this product
        productRepository.deleteById(id);
        log.warn("Product PERMANENTLY DELETED with id: {} (SKU: {})", id, product.getSku());
    }

    public Page<ProductResponse> getActiveProductsPaged(int page, int size) {
        log.debug("Fetching active products page {} size {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable).map(productMapper::toResponse);
    }

    /**
     * Find product entity by ID (for internal use)
     */
    public Product findProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
    }

    /**
     * Validate SKU uniqueness
     */
    private void validateSkuUniqueness(String sku, Long excludeId) {
        boolean skuExists = excludeId == null
                ? productRepository.existsBySku(sku)
                : productRepository.existsBySkuAndIdNot(sku, excludeId);

        if (skuExists) {
            log.warn("SKU already exists: {}", sku);
            throw new BusinessException("SKU already exists: " + sku);
        }
    }
}
