package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.request.CartItemRequest;
import learning.griddynamics.com.storeapi.dto.response.CartItemResponse;
import learning.griddynamics.com.storeapi.dto.response.CartResponse;
import learning.griddynamics.com.storeapi.entity.Cart;
import learning.griddynamics.com.storeapi.entity.CartItem;
import learning.griddynamics.com.storeapi.entity.Product;
import learning.griddynamics.com.storeapi.exception.CartItemNotFoundException;
import learning.griddynamics.com.storeapi.exception.InsufficientStockException;
import learning.griddynamics.com.storeapi.exception.ProductNotFoundException;
import learning.griddynamics.com.storeapi.repository.ProductRepository;
import learning.griddynamics.com.storeapi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse addItem(Cart cart, CartItemRequest request) {
        Product product = getProductOrThrow(request.id());
        validateStock(product, request.quantity());

        cart.addItem(product.getId(), product.getTitle(), request.quantity(), product.getPrice());
        return toResponse(cart);
    }

    @Override
    public CartResponse getCart(Cart cart) {
        return toResponse(cart);
    }

    @Override
    public CartResponse removeItem(Cart cart, long ordinal) {
        if (!cart.removeItem(ordinal)) {
            throw new CartItemNotFoundException(ordinal);
        }
        return toResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse updateItem(Cart cart, long ordinal, CartItemRequest request) {
        CartItem existing = cart.getItems().get(ordinal);
        if (existing == null) {
            throw new CartItemNotFoundException(ordinal);
        }

        Product product = getProductOrThrow(existing.getProductId());
        validateStock(product, request.quantity());

        cart.updateQuantity(ordinal, request.quantity());
        return toResponse(cart);
    }

    private Product getProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getAvailable() < requestedQuantity) {
            throw new InsufficientStockException(product.getId(), requestedQuantity, product.getAvailable());
        }
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().values().stream()
                .map(i -> new CartItemResponse(
                        i.getOrdinal(),
                        i.getProductId(),
                        i.getProductTitle(),
                        i.getQuantity(),
                        i.getPriceSnapshot(),
                        i.getLineTotal()))
                .toList();

        return new CartResponse(items, cart.getSubtotal());
    }
}