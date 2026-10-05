package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.response.ProductResponse;
import learning.griddynamics.com.storeapi.repository.ProductRepository;
import learning.griddynamics.com.storeapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductResponse(p.getId(), p.getTitle(), p.getAvailable(), p.getPrice()))
                .toList();
    }
}