package learning.griddynamics.com.storeapi.service;

import learning.griddynamics.com.storeapi.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();
}