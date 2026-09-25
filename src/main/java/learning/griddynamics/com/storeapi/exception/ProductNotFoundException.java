package learning.griddynamics.com.storeapi.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product '%s' not found".formatted(productId));
    }
}