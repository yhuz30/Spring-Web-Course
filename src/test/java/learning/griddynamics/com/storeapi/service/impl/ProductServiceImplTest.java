package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.response.ProductResponse;
import learning.griddynamics.com.storeapi.entity.Product;
import learning.griddynamics.com.storeapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void givenProductsExist_whenGetAllProducts_thenReturnMappedResponses() {
        // given
        Product product = Product.builder()
                .id("2411")
                .title("Nail gun")
                .available(8)
                .price(new BigDecimal("23.95"))
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product));

        // when
        List<ProductResponse> result = productService.getAllProducts();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(
                new ProductResponse("2411", "Nail gun", 8, new BigDecimal("23.95")));
    }

    @Test
    void givenNoProducts_whenGetAllProducts_thenReturnEmptyList() {
        // given
        when(productRepository.findAll()).thenReturn(List.of());

        // when
        List<ProductResponse> result = productService.getAllProducts();

        // then
        assertThat(result).isEmpty();
    }
}