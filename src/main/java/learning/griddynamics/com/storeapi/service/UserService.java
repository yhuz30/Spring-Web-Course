package learning.griddynamics.com.storeapi.service;

import learning.griddynamics.com.storeapi.dto.request.RegisterRequest;
import learning.griddynamics.com.storeapi.entity.User;

public interface UserService {

    void register(RegisterRequest request);

    User authenticate(String email, String rawPassword);

    void resetPassword(String email);
}