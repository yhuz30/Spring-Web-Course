package learning.griddynamics.com.storeapi.controller;

import learning.griddynamics.com.storeapi.dto.response.OrderConfirmationResponse;
import learning.griddynamics.com.storeapi.dto.response.OrderSummaryResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.entity.OrderStatus;
import learning.griddynamics.com.storeapi.entity.User;
import learning.griddynamics.com.storeapi.exception.EmptyCartException;
import learning.griddynamics.com.storeapi.exception.GlobalExceptionHandler;
import learning.griddynamics.com.storeapi.security.UserPrincipal;
import learning.griddynamics.com.storeapi.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;
    private User user;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilter(new SecurityContextHolderFilter(new HttpSessionSecurityContextRepository()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        user = User.builder().id(UUID.randomUUID()).email("john@example.com").build();
        principal = new UserPrincipal(user);
    }

    @Test
    void givenNonEmptyCart_whenCheckout_thenReturnConfirmation() throws Exception {
        // given
        OrderConfirmationResponse response = new OrderConfirmationResponse(
                UUID.randomUUID(), Instant.now(), OrderStatus.CONFIRMED,
                new BigDecimal("47.90"), List.of());

        when(orderService.checkout(eq(user), any(Cart.class))).thenReturn(response);

        // when / then
        mockMvc.perform(post("/api/orders/checkout")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.total").value(47.90));
    }

    @Test
    void givenEmptyCart_whenCheckout_thenReturn400() throws Exception {
        // given
        when(orderService.checkout(eq(user), any(Cart.class))).thenThrow(new EmptyCartException());

        // when / then
        mockMvc.perform(post("/api/orders/checkout")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenOwnConfirmedOrder_whenCancel_thenReturn200() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        // when / then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk());
    }

    @Test
    void givenUserHasOrders_whenGetOrders_thenReturnSummaryList() throws Exception {
        // given
        OrderSummaryResponse summary = new OrderSummaryResponse(
                UUID.randomUUID(), Instant.now(), new BigDecimal("47.90"), OrderStatus.CONFIRMED);

        when(orderService.getOrders(user)).thenReturn(List.of(summary));

        // when / then
        mockMvc.perform(get("/api/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].total").value(47.90));
    }
}