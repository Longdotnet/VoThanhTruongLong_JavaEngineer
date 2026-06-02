package com.seveneleven.controller;

import com.seveneleven.constants.ViewConstants;
import com.seveneleven.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for admin order management operations
 * Handles order viewing and management for administrators
 */
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController extends BaseController {

    private static final String ORDERS_ATTR = "orders";
    private static final String ORDER_ATTR = "order";
    private static final int PAGE_SIZE = 9;

    private final OrderService orderService;

    /**
     * Display list of all orders
     */
    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page, Model model) {
        log.debug("Fetching orders page {} for admin view", page);
        Page<com.seveneleven.entity.Order> orderPage = orderService.getAllOrdersPaged(page, PAGE_SIZE);
        model.addAttribute(ORDERS_ATTR, orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        return ViewConstants.ADMIN_ORDERS_LIST;
    }

    /**
     * Display order detail page
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        log.debug("Fetching order detail for id: {}", id);
        model.addAttribute(ORDER_ATTR, orderService.getOrderById(id));
        return ViewConstants.ADMIN_ORDERS_DETAIL;
    }
}
