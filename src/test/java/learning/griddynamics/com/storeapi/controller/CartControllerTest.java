package learning.griddynamics.com.storeapi.controller;

import learning.griddynamics.com.storeapi.exception.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;
import learning.griddynamics.com.storeapi.dto.request.CartItemRequest;
import learning.griddynamics.com.storeapi.dto.response.CartItemResponse;
import learning.griddynamics.com.storeapi.dto.response.CartResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.exception.CartItemNotFoundException;
import learning.griddynamics.com.storeapi.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CartController controller = new CartController(cartService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void givenValidRequest_whenAddItem_thenReturnUpdatedCart() throws Exception {
        // given
        CartItemRequest request = new CartItemRequest("2411", 2);
        CartResponse response = new CartResponse(
                List.of(new CartItemResponse(1L, "2411", "Nail gun", 2,
                        new BigDecimal("23.95"), new BigDecimal("47.90"))),
                new BigDecimal("47.90"));

        when(cartService.addItem(any(Cart.class), eq(request))).thenReturn(response);

        // when / then
        mockMvc.perform(post("/api/cart/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(47.90))
                .andExpect(jsonPath("$.items[0].productId").value("2411"));
    }

    @Test
    void givenEmptySession_whenGetCart_thenReturnEmptyCart() throws Exception {
        // given
        when(cartService.getCart(any(Cart.class)))
                .thenReturn(new CartResponse(List.of(), BigDecimal.ZERO));

        // when / then
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.subtotal").value(0));
    }

    @Test
    void givenExistingOrdinal_whenUpdateItem_thenReturnUpdatedCart() throws Exception {
        // given
        CartItemRequest request = new CartItemRequest("2411", 5);
        CartResponse response = new CartResponse(
                List.of(new CartItemResponse(1L, "2411", "Nail gun", 5,
                        new BigDecimal("23.95"), new BigDecimal("119.75"))),
                new BigDecimal("119.75"));

        when(cartService.updateItem(any(Cart.class), eq(1L), eq(request))).thenReturn(response);

        // when / then
        mockMvc.perform(put("/api/cart/items/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    void givenExistingOrdinal_whenRemoveItem_thenReturnUpdatedCart() throws Exception {
        // given
        when(cartService.removeItem(any(Cart.class), eq(1L)))
                .thenReturn(new CartResponse(List.of(), BigDecimal.ZERO));

        // when / then
        mockMvc.perform(delete("/api/cart/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void givenUnknownOrdinal_whenRemoveItem_thenReturn404() throws Exception {
        // given
        when(cartService.removeItem(any(Cart.class), eq(999L)))
                .thenThrow(new CartItemNotFoundException(999L));

        // when / then
        mockMvc.perform(delete("/api/cart/items/999"))
                .andExpect(status().isNotFound());
    }
}