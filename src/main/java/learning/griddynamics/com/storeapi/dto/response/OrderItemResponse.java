package learning.griddynamics.com.storeapi.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String productTitle,
        int quantity,
        BigDecimal priceAtPurchase
) {
}