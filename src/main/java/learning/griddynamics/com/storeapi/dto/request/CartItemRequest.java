package learning.griddynamics.com.storeapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CartItemRequest(

        @NotBlank
        String id,

        @Min(1)
        int quantity
) {
}