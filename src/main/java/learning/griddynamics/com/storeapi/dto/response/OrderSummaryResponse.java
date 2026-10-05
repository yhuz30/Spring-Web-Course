package learning.griddynamics.com.storeapi.dto.response;

import learning.griddynamics.com.storeapi.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID orderId,
        Instant createdAt,
        BigDecimal total,
        OrderStatus status
) {
}