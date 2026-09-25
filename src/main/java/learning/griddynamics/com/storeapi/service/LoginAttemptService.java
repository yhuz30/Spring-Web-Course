package learning.griddynamics.com.storeapi.service;

public interface LoginAttemptService {

    void checkAllowed(String email);

    void onLoginFailed(String email);

    void onLoginSucceeded(String email);
}