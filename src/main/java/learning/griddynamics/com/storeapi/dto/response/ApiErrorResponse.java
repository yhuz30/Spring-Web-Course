package learning.griddynamics.com.storeapi.dto.response;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(Instant.now(), status, error, message, List.of());
    }
}