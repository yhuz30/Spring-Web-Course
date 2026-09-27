package learning.griddynamics.com.storeapi.controller;

import jakarta.servlet.http.HttpSession;
import learning.griddynamics.com.storeapi.dto.response.OrderConfirmationResponse;
import learning.griddynamics.com.storeapi.dto.response.OrderSummaryResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.security.UserPrincipal;
import learning.griddynamics.com.storeapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final String CART_SESSION_ATTR = "cart";

    private final OrderService orderService;

    @PostMapping("/checkout")
    public OrderConfirmationResponse checkout(HttpSession session, @AuthenticationPrincipal UserPrincipal principal) {
        Cart cart = getOrCreateCart(session);
        OrderConfirmationResponse response = orderService.checkout(principal.user(), cart);
        session.setAttribute(CART_SESSION_ATTR, cart); // cart is now empty, persist that back
        return response;
    }

    @PostMapping("/{orderId}/cancel")
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID orderId) {
        orderService.cancel(principal.user(), orderId);
    }

    @GetMapping
    public List<OrderSummaryResponse> getOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getOrders(principal.user());
    }

    private Cart getOrCreateCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_SESSION_ATTR);
        if (cart == null) {
            cart = new Cart();
        }
        return cart;
    }
}