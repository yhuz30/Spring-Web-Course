package learning.griddynamics.com.storeapi.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productId, int requested, int available) {
        super("Not enough stock for product '%s': requested %d, available %d"
                .formatted(productId, requested, available));
    }
}