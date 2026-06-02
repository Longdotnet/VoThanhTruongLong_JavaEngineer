package com.seveneleven.controller;

import com.seveneleven.constants.MessageConstants;
import com.seveneleven.constants.ViewConstants;
import com.seveneleven.dto.ProductRequest;
import com.seveneleven.exception.BusinessException;
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
 * Controller for admin product management operations
 * Handles CRUD operations for products
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController extends BaseController {

    private static final String PRODUCT_REQUEST_ATTR = "productRequest";
    private static final String PRODUCTS_ATTR = "products";
    private static final String PRODUCT_ATTR = "product";
    private static final String PRODUCT_ID_ATTR = "productId";
    private static final int PAGE_SIZE = 8;

    private final ProductService productService;

    /**
     * Display list of all products
     */
    @GetMapping
    public String listProducts(@RequestParam(defaultValue = "0") int page, Model model) {
        log.debug("Fetching products page {} for admin view", page);
        Page<com.seveneleven.dto.ProductResponse> productPage = productService.getAllProductsPaged(page, PAGE_SIZE);
        model.addAttribute(PRODUCTS_ATTR, productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        return ViewConstants.ADMIN_PRODUCTS_LIST;
    }

    /**
     * Display product detail page
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        log.debug("Fetching product detail for id: {}", id);
        model.addAttribute(PRODUCT_ATTR, productService.getProductById(id));
        return ViewConstants.ADMIN_PRODUCTS_DETAIL;
    }

    /**
     * Show form for creating new product
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.debug("Displaying product creation form");
        model.addAttribute(PRODUCT_REQUEST_ATTR, new ProductRequest());
        return ViewConstants.ADMIN_PRODUCTS_FORM;
    }

    /**
     * Handle product creation
     */
    @PostMapping
    public String createProduct(@Valid @ModelAttribute ProductRequest productRequest,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors in product creation: {}", result.getAllErrors());
            return ViewConstants.ADMIN_PRODUCTS_FORM;
        }

        log.info("Creating new product with SKU: {}", productRequest.getSku());
        productService.createProduct(productRequest);
        addSuccessMessage(redirectAttributes, MessageConstants.PRODUCT_CREATED_SUCCESS);
        return ViewConstants.REDIRECT_ADMIN_PRODUCTS;
    }

    /**
     * Show form for editing existing product
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Displaying product edit form for id: {}", id);
        var product = productService.getProductById(id);

        ProductRequest request = ProductRequest.builder()
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();

        model.addAttribute(PRODUCT_REQUEST_ATTR, request);
        model.addAttribute(PRODUCT_ID_ATTR, id);
        return ViewConstants.ADMIN_PRODUCTS_FORM;
    }

    /**
     * Handle product update
     */
    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute ProductRequest productRequest,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors in product update for id {}: {}", id, result.getAllErrors());
            model.addAttribute(PRODUCT_ID_ATTR, id);
            return ViewConstants.ADMIN_PRODUCTS_FORM;
        }

        log.info("Updating product id: {} with SKU: {}", id, productRequest.getSku());
        productService.updateProduct(id, productRequest);
        addSuccessMessage(redirectAttributes, MessageConstants.PRODUCT_UPDATED_SUCCESS);
        return ViewConstants.REDIRECT_ADMIN_PRODUCTS;
    }

    /**
     * Handle product deletion (soft delete by deactivating)
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deactivating product id: {}", id);
        productService.deleteProduct(id);
        addSuccessMessage(redirectAttributes, MessageConstants.PRODUCT_DELETED_SUCCESS);
        return ViewConstants.REDIRECT_ADMIN_PRODUCTS;
    }
}

