package com.seveneleven.controller;

import com.seveneleven.constants.MessageConstants;
import com.seveneleven.constants.ViewConstants;
import com.seveneleven.dto.OrderRequest;
import com.seveneleven.exception.BusinessException;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.service.OrderService;
import com.seveneleven.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for user-facing operations
 * Handles product browsing and order placement
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController extends BaseController {

    private static final String PRODUCTS_ATTR = "products";
    private static final String ORDER_REQUEST_ATTR = "orderRequest";
    private static final int PAGE_SIZE = 6;

    private final ProductService productService;
    private final OrderService orderService;

    /**
     * Redirect home to products page
     */
    @GetMapping("/")
    public String home() {
        return ViewConstants.REDIRECT_PRODUCTS;
    }

    /**
     * Display products listing page for customers
     */
    @GetMapping("/products")
    public String listProducts(@RequestParam(defaultValue = "0") int page, Model model) {
        log.debug("Fetching active products page {} for user view", page);
        Page<com.seveneleven.dto.ProductResponse> productPage = productService.getActiveProductsPaged(page, PAGE_SIZE);
        model.addAttribute(PRODUCTS_ATTR, productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute(ORDER_REQUEST_ATTR, new OrderRequest());
        return ViewConstants.USER_PRODUCTS;
    }

    /**
     * Handle order creation from customer
     */
    @PostMapping("/orders")
    public String createOrder(@Valid @ModelAttribute OrderRequest orderRequest,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        // Bean Validation errors (blank name, phone, etc.)
        if (result.hasErrors()) {
            log.warn("Validation errors in order creation: {}", result.getAllErrors());
            model.addAttribute(PRODUCTS_ATTR, productService.getActiveProducts());
            model.addAttribute(ERROR_MESSAGE, MessageConstants.ORDER_VALIDATION_ERROR);
            return ViewConstants.USER_PRODUCTS;
        }

        try {
            log.info("Creating order for customer: {}", orderRequest.getCustomerName());
            var order = orderService.createOrder(orderRequest);
            String successMessage = String.format(
                    MessageConstants.ORDER_CREATED_SUCCESS_TEMPLATE,
                    order.getId(),
                    String.format("%,.0f", order.getTotalAmount())
            );
            addSuccessMessage(redirectAttributes, successMessage);
            return ViewConstants.REDIRECT_PRODUCTS;

        } catch (BusinessException | ResourceNotFoundException e) {
            // Business errors: out of stock, inactive product, etc.
            // Stay on same page to keep the form + show error
            log.warn("Order creation failed for customer {}: {}", orderRequest.getCustomerName(), e.getMessage());
            model.addAttribute(PRODUCTS_ATTR, productService.getActiveProducts());
            model.addAttribute(ERROR_MESSAGE, e.getMessage());
            return ViewConstants.USER_PRODUCTS;
        }
    }
}
