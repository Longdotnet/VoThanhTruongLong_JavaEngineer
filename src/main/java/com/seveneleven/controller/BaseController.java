package com.seveneleven.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Base controller providing common functionality for all controllers
 * Following DRY principle to avoid code duplication
 */
public abstract class BaseController {

    // Constants for attribute names
    protected static final String SUCCESS_MESSAGE = "successMessage";
    protected static final String ERROR_MESSAGE = "errorMessage";

    // Constants for view paths
    protected static final String REDIRECT_PREFIX = "redirect:";

    /**
     * Add success message to redirect attributes
     */
    protected void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE, message);
    }

    /**
     * Add error message to redirect attributes
     */
    protected void addErrorMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, message);
    }
}

