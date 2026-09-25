package learning.griddynamics.com.storeapi.exception;

public class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(java.util.UUID orderId) {
        super("Order '%s' cannot be cancelled".formatted(orderId));
    }
}