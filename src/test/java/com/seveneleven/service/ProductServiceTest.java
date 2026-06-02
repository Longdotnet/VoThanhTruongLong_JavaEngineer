package com.seveneleven.service;

import com.seveneleven.dto.ProductRequest;
import com.seveneleven.dto.ProductResponse;
import com.seveneleven.entity.Product;
import com.seveneleven.entity.ProductStatus;
import com.seveneleven.exception.BusinessException;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validRequest;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        validRequest = ProductRequest.builder()
                .name("Coca Cola 330ml")
                .sku("CC-330")
                .price(new BigDecimal("15000"))
                .stockQuantity(100)
                .build();

        existingProduct = Product.builder()
                .id(1L)
                .name("Coca Cola 330ml")
                .sku("CC-330")
                .price(new BigDecimal("15000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void createProduct_success() {
        when(productRepository.existsBySku("CC-330")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponse response = productService.createProduct(validRequest);

        assertThat(response.getName()).isEqualTo("Coca Cola 330ml");
        assertThat(response.getSku()).isEqualTo("CC-330");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_duplicateSku_throwsBusinessException() {
        when(productRepository.existsBySku("CC-330")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndIdNot("CC-330", 1L)).thenReturn(false);

        ProductRequest updateRequest = ProductRequest.builder()
                .name("Coca Cola 500ml")
                .sku("CC-330")
                .price(new BigDecimal("20000"))
                .stockQuantity(80)
                .build();

        Product updatedProduct = Product.builder()
                .id(1L).name("Coca Cola 500ml").sku("CC-330")
                .price(new BigDecimal("20000")).stockQuantity(80)
                .status(ProductStatus.ACTIVE).build();
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response.getName()).isEqualTo("Coca Cola 500ml");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    void updateProduct_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void deleteProduct_softDelete_setsStatusInactive() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        productService.deleteProduct(1L);

        assertThat(existingProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        verify(productRepository).save(existingProduct);
    }
}

