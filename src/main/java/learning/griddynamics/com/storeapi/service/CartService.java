package learning.griddynamics.com.storeapi.service;

import learning.griddynamics.com.storeapi.dto.request.CartItemRequest;
import learning.griddynamics.com.storeapi.dto.response.CartResponse;
import learning.griddynamics.com.storeapi.entity.Cart;

public interface CartService {

    CartResponse addItem(Cart cart, CartItemRequest request);

    CartResponse getCart(Cart cart);

    CartResponse removeItem(Cart cart, long ordinal);

    CartResponse updateItem(Cart cart, long ordinal, CartItemRequest request);
}