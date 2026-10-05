package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.request.CartItemRequest;
import learning.griddynamics.com.storeapi.dto.response.CartResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.entity.Product;
import learning.griddynamics.com.storeapi.exception.CartItemNotFoundException;
import learning.griddynamics.com.storeapi.exception.InsufficientStockException;
import learning.griddynamics.com.storeapi.exception.ProductNotFoundException;
import learning.griddynamics.com.storeapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        product = Product.builder()
                .id("2411")
                .title("Nail gun")
                .available(8)
                .price(new BigDecimal("23.95"))
                .build();
    }

    @Test
    void givenAvailableStock_whenAddItem_thenItemIsAddedToCart() {
        // given
        CartItemRequest request = new CartItemRequest("2411", 2);
        when(productRepository.findById("2411")).thenReturn(Optional.of(product));

        // when
        CartResponse response = cartService.addItem(cart, request);

        // then
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.subtotal()).isEqualByComparingTo("47.90");
    }

    @Test
    void givenUnknownProduct_whenAddItem_thenThrowProductNotFoundException() {
        // given
        CartItemRequest request = new CartItemRequest("9999", 1);
        when(productRepository.findById("9999")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> cartService.addItem(cart, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void givenQuantityExceedsStock_whenAddItem_thenThrowInsufficientStockException() {
        // given
        CartItemRequest request = new CartItemRequest("2411", 50);
        when(productRepository.findById("2411")).thenReturn(Optional.of(product));

        // when / then
        assertThatThrownBy(() -> cartService.addItem(cart, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void givenItemsInCart_whenGetCart_thenReturnCurrentState() {
        // given
        cart.addItem("2411", "Nail gun", 2, new BigDecimal("23.95"));

        // when
        CartResponse response = cartService.getCart(cart);

        // then
        assertThat(response.items()).hasSize(1);
        assertThat(response.subtotal()).isEqualByComparingTo("47.90");
    }

    @Test
    void givenExistingOrdinal_whenRemoveItem_thenItemIsRemoved() {
        // given
        long ordinal = cart.addItem("2411", "Nail gun", 2, new BigDecimal("23.95"));

        // when
        CartResponse response = cartService.removeItem(cart, ordinal);

        // then
        assertThat(response.items()).isEmpty();
    }

    @Test
    void givenUnknownOrdinal_whenRemoveItem_thenThrowCartItemNotFoundException() {
        // given / when / then
        assertThatThrownBy(() -> cartService.removeItem(cart, 999L))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void givenExistingItemAndEnoughStock_whenUpdateItem_thenQuantityIsUpdated() {
        // given
        long ordinal = cart.addItem("2411", "Nail gun", 2, new BigDecimal("23.95"));
        CartItemRequest request = new CartItemRequest("2411", 5);
        when(productRepository.findById("2411")).thenReturn(Optional.of(product));

        // when
        CartResponse response = cartService.updateItem(cart, ordinal, request);

        // then
        assertThat(response.items().getFirst().quantity()).isEqualTo(5);
    }

    @Test
    void givenUnknownOrdinal_whenUpdateItem_thenThrowCartItemNotFoundException() {
        // given
        CartItemRequest request = new CartItemRequest("2411", 5);

        // when / then
        assertThatThrownBy(() -> cartService.updateItem(cart, 999L, request))
                .isInstanceOf(CartItemNotFoundException.class);
    }
}