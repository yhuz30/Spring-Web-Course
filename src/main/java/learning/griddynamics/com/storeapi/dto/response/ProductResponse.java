package learning.griddynamics.com.storeapi.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String title,
        int available,
        BigDecimal price
) {
}