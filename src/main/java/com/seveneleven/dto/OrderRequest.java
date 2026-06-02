package com.seveneleven.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 50, message = "Customer name must not exceed 50 characters")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 digits")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must contain digits only")
    private String customerPhone;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
