package com.shopmanagement.dto;

import com.shopmanagement.entity.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
//    private List<OrderItemResponseDTO> items; // Can add this later for detailed response
}
