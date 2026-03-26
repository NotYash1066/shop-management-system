package com.shopmanagement.dto;

import com.shopmanagement.entity.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reconciliationStartedAt;
    private LocalDateTime reconciliationCompletedAt;
    private String reconciliationFailureReason;
}
