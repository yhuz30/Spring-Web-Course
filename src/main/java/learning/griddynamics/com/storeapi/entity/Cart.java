package learning.griddynamics.com.storeapi.entity;

import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Getter
public class Cart implements Serializable {

    // ordinal -> item, LinkedHashMap keeps insertion order for display
    private final Map<Long, CartItem> items = new LinkedHashMap<>();
    private long nextOrdinal = 1;

    public long addItem(String productId, String productTitle, int quantity, BigDecimal price) {
        long ordinal = nextOrdinal++;
        items.put(ordinal, CartItem.builder()
                .ordinal(ordinal)
                .productId(productId)
                .productTitle(productTitle)
                .quantity(quantity)
                .priceSnapshot(price)
                .build());
        return ordinal;
    }

    public boolean removeItem(long ordinal) {
        return nonNull(items.remove(ordinal));
    }

    public CartItem updateQuantity(long ordinal, int quantity) {
        CartItem item = items.get(ordinal);
        if (nonNull(item)) {
            item.setQuantity(quantity);
        }
        return item;
    }

    public BigDecimal getSubtotal() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}