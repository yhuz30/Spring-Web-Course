package learning.griddynamics.com.storeapi.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        long ordinal,
        String productId,
        String productTitle,
        int quantity,
        BigDecimal price,
        BigDecimal lineTotal
) {
}