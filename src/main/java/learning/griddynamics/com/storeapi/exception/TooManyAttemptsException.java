package learning.griddynamics.com.storeapi.exception;

public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException() {
        super("Too many login attempts, try again later");
    }
}