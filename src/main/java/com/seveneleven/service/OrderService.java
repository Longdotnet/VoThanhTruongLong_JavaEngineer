package com.seveneleven.service;

import com.seveneleven.dto.OrderItemRequest;
import com.seveneleven.dto.OrderRequest;
import com.seveneleven.entity.*;
import com.seveneleven.exception.BusinessException;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Order business logic
 * Handles order operations with proper transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    /**
     * Retrieve all orders
     */
    public List<Order> getAllOrders() {
        log.debug("Fetching all orders from database");
        return orderRepository.findAll();
    }


    public Order getOrderById(Long id) {
        log.debug("Fetching order with id: {} using optimized query", id);

        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });
    }

    /**
     * Create new order with validation and stock management
     */
    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerName());

        validateOrderRequest(request);

        Order order = initializeOrder(request);
        BigDecimal totalAmount = processOrderItems(order, request.getItems());

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with id: {} and total amount: {}",
                savedOrder.getId(), totalAmount);
        return savedOrder;
    }

    /**
     * Retrieve all orders with pagination (admin)
     */
    public Page<Order> getAllOrdersPaged(int page, int size) {
        log.debug("Fetching orders page {} size {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findAll(pageable);
    }

    /**
     * Validate order request has items
     */
    private void validateOrderRequest(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("Order request has no items");
            throw new BusinessException("Order must contain at least one item");
        }
    }

    /**
     * Initialize order entity
     */
    private Order initializeOrder(OrderRequest request) {
        return Order.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    /**
     * Process order items and calculate total
     */
    private BigDecimal processOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productService.findProductEntityById(itemRequest.getProductId());

            validateProductAvailability(product, itemRequest.getQuantity());

            OrderItem orderItem = createOrderItem(order, product, itemRequest.getQuantity());
            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(orderItem.getSubtotal());
            updateProductStock(product, itemRequest.getQuantity());
        }

        return totalAmount;
    }

    /**
     * Validate product availability and stock
     */
    private void validateProductAvailability(Product product, Integer quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            log.warn("Product is not available: {}", product.getName());
            throw new BusinessException("Product is not available: " + product.getName());
        }

        if (quantity > product.getStockQuantity()) {
            log.warn("Insufficient stock for product: {}. Available: {}, Requested: {}",
                    product.getName(), product.getStockQuantity(), quantity);
            throw new BusinessException(
                String.format("Insufficient stock for product: %s. Available: %d",
                        product.getName(), product.getStockQuantity())
            );
        }
    }

    /**
     * Create order item with calculated subtotal
     */
    private OrderItem createOrderItem(Order order, Product product, Integer quantity) {
        BigDecimal unitPrice = product.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
    }

    /**
     * Update product stock after order
     */
    private void updateProductStock(Product product, Integer quantity) {
        int newStock = product.getStockQuantity() - quantity;
        product.setStockQuantity(newStock);
        log.debug("Updated stock for product {}: {} -> {}",
                product.getId(), product.getStockQuantity() + quantity, newStock);
    }
}
