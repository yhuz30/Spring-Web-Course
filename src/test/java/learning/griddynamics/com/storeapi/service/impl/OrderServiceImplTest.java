package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.response.OrderConfirmationResponse;
import learning.griddynamics.com.storeapi.dto.response.OrderSummaryResponse;
import learning.griddynamics.com.storeapi.entity.*;
import learning.griddynamics.com.storeapi.exception.*;
import learning.griddynamics.com.storeapi.repository.OrderRepository;
import learning.griddynamics.com.storeapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("john@example.com").build();
        product = Product.builder()
                .id("2411")
                .title("Nail gun")
                .available(8)
                .price(new BigDecimal("23.95"))
                .build();
        cart = new Cart();
    }

    @Test
    void givenEmptyCart_whenCheckout_thenThrowEmptyCartException() {
        // given / when / then
        assertThatThrownBy(() -> orderService.checkout(user, cart))
                .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void givenSufficientStock_whenCheckout_thenOrderIsConfirmedAndStockDecreased() {
        // given
        cart.addItem("2411", "Nail gun", 2, new BigDecimal("23.95"));
        when(productRepository.findById("2411")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        // when
        OrderConfirmationResponse response = orderService.checkout(user, cart);

        // then
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.total()).isEqualByComparingTo("47.90");
        assertThat(response.items()).hasSize(1);
        assertThat(cart.isEmpty()).isTrue();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getAvailable()).isEqualTo(6);
    }

    @Test
    void givenInsufficientStock_whenCheckout_thenThrowInsufficientStockException() {
        // given
        cart.addItem("2411", "Nail gun", 50, new BigDecimal("23.95"));
        when(productRepository.findById("2411")).thenReturn(Optional.of(product));

        // when / then
        assertThatThrownBy(() -> orderService.checkout(user, cart))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void givenProductRemovedFromCatalog_whenCheckout_thenThrowProductNotFoundException() {
        // given
        cart.addItem("9999", "Ghost product", 1, new BigDecimal("10.00"));
        when(productRepository.findById("9999")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> orderService.checkout(user, cart))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void givenConfirmedOwnOrder_whenCancel_thenOrderIsCancelledAndStockReturned() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderItem orderItem = OrderItem.builder()
                .productId("2411")
                .productTitle("Nail gun")
                .quantity(2)
                .priceAtPurchase(new BigDecimal("23.95"))
                .build();

        Order order = Order.builder()
                .id(orderId)
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .total(new BigDecimal("47.90"))
                .build();
        order.addItem(orderItem);

        product.setAvailable(6); // was decreased at checkout time

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById("2411")).thenReturn(Optional.of(product));

        // when
        orderService.cancel(user, orderId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getAvailable()).isEqualTo(8);
    }

    @Test
    void givenOrderBelongsToAnotherUser_whenCancel_thenThrowOrderNotFoundException() {
        // given
        UUID orderId = UUID.randomUUID();
        User anotherUser = User.builder().id(UUID.randomUUID()).build();
        Order order = Order.builder().id(orderId).user(anotherUser).status(OrderStatus.CONFIRMED).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when / then
        assertThatThrownBy(() -> orderService.cancel(user, orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void givenAlreadyCancelledOrder_whenCancel_thenThrowOrderNotCancellableException() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).user(user).status(OrderStatus.CANCELLED).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when / then
        assertThatThrownBy(() -> orderService.cancel(user, orderId))
                .isInstanceOf(OrderNotCancellableException.class);
    }

    @Test
    void givenUserHasOrders_whenGetOrders_thenReturnSummaries() {
        // given
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .total(new BigDecimal("47.90"))
                .build();

        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(order));

        // when
        List<OrderSummaryResponse> result = orderService.getOrders(user);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().total()).isEqualByComparingTo("47.90");
    }
}