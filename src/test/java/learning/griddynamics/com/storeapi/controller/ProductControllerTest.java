package learning.griddynamics.com.storeapi.controller;

import learning.griddynamics.com.storeapi.dto.response.ProductResponse;
import learning.griddynamics.com.storeapi.exception.GlobalExceptionHandler;
import learning.griddynamics.com.storeapi.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void givenProductsExist_whenGetAllProducts_thenReturnList() throws Exception {
        // given
        when(productService.getAllProducts()).thenReturn(List.of(
                new ProductResponse("2411", "Nail gun", 8, new BigDecimal("23.95"))));

        // when / then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2411"))
                .andExpect(jsonPath("$[0].title").value("Nail gun"))
                .andExpect(jsonPath("$[0].available").value(8));
    }

    @Test
    void givenNoProducts_whenGetAllProducts_thenReturnEmptyList() throws Exception {
        // given
        when(productService.getAllProducts()).thenReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}