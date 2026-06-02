package com.seveneleven.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for all controllers
 * Centralized error handling following Single Responsibility Principle
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String GENERIC_ERROR_VIEW = "error";

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        log.error("Resource not found: {}", ex.getMessage(), ex);
        model.addAttribute(ERROR_MESSAGE, ex.getMessage());
        return GENERIC_ERROR_VIEW;
    }

    /**
     * BusinessException: redirect về trang trước với error flash message
     * Dùng Referer header để redirect đúng trang (form, list, v.v.)
     * Fallback về home nếu không có Referer (tuân thủ Open/Closed Principle)
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        log.warn("Business exception: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ex.getMessage());

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            log.debug("Redirecting back to referer: {}", referer);
            return "redirect:" + referer;
        }

        log.debug("No referer found, redirecting to home");
        return "redirect:/";
    }

    /**
     * Bỏ qua các static resource không tìm thấy (favicon.ico, devtools, v.v.)
     * Không cần log ERROR vì đây là browser requests bình thường
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex) {
        log.debug("Static resource not found (ignored): {}", ex.getResourcePath());
        return GENERIC_ERROR_VIEW;
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error occurred", ex);
        model.addAttribute(ERROR_MESSAGE, "An unexpected error occurred. Please try again later.");
        return GENERIC_ERROR_VIEW;
    }
}
