package learning.griddynamics.com.storeapi.dto.response;

import learning.griddynamics.com.storeapi.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderConfirmationResponse(
        UUID orderId,
        Instant createdAt,
        OrderStatus status,
        BigDecimal total,
        List<OrderItemResponse> items
) {
}