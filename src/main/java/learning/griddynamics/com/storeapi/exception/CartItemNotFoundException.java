package learning.griddynamics.com.storeapi.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(long ordinal) {
        super("Cart item with ordinal '%d' not found".formatted(ordinal));
    }
}