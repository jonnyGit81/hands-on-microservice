package se.magnus.api.composite;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeService {

    @GetMapping("/product-composite/{productId}")
    ProductAggregate getProduct(@PathVariable int productId);
}
