package learning.griddynamics.com.storeapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private Long ordinal;
    private String productId;
    private String productTitle;
    private int quantity;
    private BigDecimal priceSnapshot;

    public BigDecimal getLineTotal() {
        return priceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }
}