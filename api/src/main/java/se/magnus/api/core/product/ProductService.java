package se.magnus.api.core.product;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

public interface ProductService {

    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Product getProduct(@PathVariable int productId);

    @PostMapping(value    = "/product", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Product createProduct(@RequestBody Product body);

    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);
}
