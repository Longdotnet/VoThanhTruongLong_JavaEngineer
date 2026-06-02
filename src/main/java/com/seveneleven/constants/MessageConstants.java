package com.seveneleven.constants;

/**
 * Constants for user-facing messages
 * Centralized messages for easy maintenance and internationalization
 */
public final class MessageConstants {

    private MessageConstants() {
        // Prevent instantiation
    }

    // Product messages
    public static final String PRODUCT_CREATED_SUCCESS = "Product created successfully!";
    public static final String PRODUCT_UPDATED_SUCCESS = "Product updated successfully!";
    public static final String PRODUCT_DELETED_SUCCESS = "Product deactivated successfully!";

    // Order messages
    public static final String ORDER_CREATED_SUCCESS_TEMPLATE = "Order #%d placed successfully! Total: %s VND";
    public static final String ORDER_VALIDATION_ERROR = "Please fix the errors below.";

    // Generic messages
    public static final String OPERATION_SUCCESS = "Operation completed successfully!";
    public static final String OPERATION_FAILED = "Operation failed. Please try again.";
}

