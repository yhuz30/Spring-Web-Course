package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.response.OrderConfirmationResponse;
import learning.griddynamics.com.storeapi.dto.response.OrderItemResponse;
import learning.griddynamics.com.storeapi.dto.response.OrderSummaryResponse;
import learning.griddynamics.com.storeapi.entity.*;
import learning.griddynamics.com.storeapi.exception.*;
import learning.griddynamics.com.storeapi.repository.OrderRepository;
import learning.griddynamics.com.storeapi.repository.ProductRepository;
import learning.griddynamics.com.storeapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderConfirmationResponse checkout(User user, Cart cart) {
        if (cart.isEmpty()) {
            throw new EmptyCartException();
        }

        Order order = Order.builder()
                .user(user)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // re-check prices & stock against DB, not trusting the cart snapshot blindly
        for (CartItem cartItem : cart.getItems().values()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(cartItem.getProductId()));

            if (product.getAvailable() < cartItem.getQuantity()) {
                throw new InsufficientStockException(product.getId(), cartItem.getQuantity(), product.getAvailable());
            }

            product.setAvailable(product.getAvailable() - cartItem.getQuantity());
            productRepository.save(product); // triggers @Version check

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productTitle(product.getTitle())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            order.addItem(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotal(total);
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);

        cart.getItems().clear(); // empty the cart after successful checkout

        return toConfirmationResponse(saved);
    }

    @Override
    @Transactional
    public void cancel(User user, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderNotCancellableException(orderId);
        }

        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setAvailable(product.getAvailable() + item.getQuantity());
                productRepository.save(product);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getCreatedAt(), o.getTotal(), o.getStatus()))
                .toList();
    }

    private OrderConfirmationResponse toConfirmationResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getProductTitle(), i.getQuantity(), i.getPriceAtPurchase()))
                .toList();

        return new OrderConfirmationResponse(order.getId(), order.getCreatedAt(), order.getStatus(), order.getTotal(), items);
    }
}