package com.seveneleven.constants;

/**
 * Constants for view names and paths
 * Centralized view paths to avoid magic strings
 */
public final class ViewConstants {

    private ViewConstants() {
        // Prevent instantiation
    }

    // Admin Product Views
    public static final String ADMIN_PRODUCTS_LIST = "admin/products/list";
    public static final String ADMIN_PRODUCTS_DETAIL = "admin/products/detail";
    public static final String ADMIN_PRODUCTS_FORM = "admin/products/form";
    public static final String REDIRECT_ADMIN_PRODUCTS = "redirect:/admin/products";

    // Admin Order Views
    public static final String ADMIN_ORDERS_LIST = "admin/orders/list";
    public static final String ADMIN_ORDERS_DETAIL = "admin/orders/detail";

    // User Views
    public static final String USER_PRODUCTS = "user/products";
    public static final String REDIRECT_PRODUCTS = "redirect:/products";
    public static final String REDIRECT_HOME = "redirect:/";
}

