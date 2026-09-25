package learning.griddynamics.com.storeapi.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(java.util.UUID orderId) {
        super("Order '%s' not found".formatted(orderId));
    }
}