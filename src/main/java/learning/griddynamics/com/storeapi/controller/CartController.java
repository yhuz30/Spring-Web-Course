package learning.griddynamics.com.storeapi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import learning.griddynamics.com.storeapi.dto.request.CartItemRequest;
import learning.griddynamics.com.storeapi.dto.response.CartResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private static final String CART_SESSION_ATTR = "cart";

    private final CartService cartService;

    @PostMapping("/items")
    public CartResponse addItem(HttpSession session, @Valid @RequestBody CartItemRequest request) {
        Cart cart = getOrCreateCart(session);
        CartResponse response = cartService.addItem(cart, request);
        session.setAttribute(CART_SESSION_ATTR, cart);
        return response;
    }

    @GetMapping
    public CartResponse getCart(HttpSession session) {
        return cartService.getCart(getOrCreateCart(session));
    }

    @PutMapping("/items/{ordinal}")
    public CartResponse updateItem(HttpSession session, @PathVariable long ordinal, @Valid @RequestBody CartItemRequest request) {
        Cart cart = getOrCreateCart(session);
        CartResponse response = cartService.updateItem(cart, ordinal, request);
        session.setAttribute(CART_SESSION_ATTR, cart);
        return response;
    }

    @DeleteMapping("/items/{ordinal}")
    public CartResponse removeItem(HttpSession session, @PathVariable long ordinal) {
        Cart cart = getOrCreateCart(session);
        CartResponse response = cartService.removeItem(cart, ordinal);
        session.setAttribute(CART_SESSION_ATTR, cart);
        return response;
    }

    private Cart getOrCreateCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_SESSION_ATTR);
        if (cart == null) {
            cart = new Cart();
        }
        return cart;
    }
}