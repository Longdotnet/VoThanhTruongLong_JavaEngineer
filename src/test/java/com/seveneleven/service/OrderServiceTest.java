package com.seveneleven.service;

import com.seveneleven.dto.OrderItemRequest;
import com.seveneleven.dto.OrderRequest;
import com.seveneleven.entity.*;
import com.seveneleven.exception.BusinessException;
import com.seveneleven.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Product activeProduct;
    private OrderRequest validOrderRequest;

    @BeforeEach
    void setUp() {
        activeProduct = Product.builder()
                .id(1L)
                .name("Coca Cola 330ml")
                .sku("CC-330")
                .price(new BigDecimal("15000"))
                .stockQuantity(50)
                .status(ProductStatus.ACTIVE)
                .build();

        validOrderRequest = OrderRequest.builder()
                .customerName("Nguyen Van A")
                .customerPhone("0912345678")
                .items(List.of(
                        OrderItemRequest.builder().productId(1L).quantity(3).build()
                ))
                .build();
    }

    @Test
    void createOrder_success() {
        when(productService.findProductEntityById(1L)).thenReturn(activeProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(validOrderRequest);

        assertThat(order.getCustomerName()).isEqualTo("Nguyen Van A");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_calculatesTotalAmountCorrectly() {
        when(productService.findProductEntityById(1L)).thenReturn(activeProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(validOrderRequest);

        // 15000 * 3 = 45000
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45000"));
    }

    @Test
    void createOrder_insufficientStock_throwsBusinessException() {
        activeProduct.setStockQuantity(2); // only 2 in stock, requesting 3
        when(productService.findProductEntityById(1L)).thenReturn(activeProduct);

        assertThatThrownBy(() -> orderService.createOrder(validOrderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_inactiveProduct_throwsBusinessException() {
        activeProduct.setStatus(ProductStatus.INACTIVE);
        when(productService.findProductEntityById(1L)).thenReturn(activeProduct);

        assertThatThrownBy(() -> orderService.createOrder(validOrderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_decreasesProductStock() {
        when(productService.findProductEntityById(1L)).thenReturn(activeProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(validOrderRequest);

        // 50 - 3 = 47
        assertThat(activeProduct.getStockQuantity()).isEqualTo(47);
    }
}
