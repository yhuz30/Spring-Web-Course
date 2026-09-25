package learning.griddynamics.com.storeapi.service;

import learning.griddynamics.com.storeapi.dto.response.OrderConfirmationResponse;
import learning.griddynamics.com.storeapi.dto.response.OrderSummaryResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.entity.User;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderConfirmationResponse checkout(User user, Cart cart);

    void cancel(User user, UUID orderId);

    List<OrderSummaryResponse> getOrders(User user);
}