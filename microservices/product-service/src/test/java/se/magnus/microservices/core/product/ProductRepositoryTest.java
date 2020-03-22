package se.magnus.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import se.magnus.microservices.core.product.persistent.ProductEntity;
import se.magnus.microservices.core.product.persistent.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
@DataMongoTest
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    private void setupDb() {
        repository.deleteAll();
        ProductEntity newProduct = new ProductEntity(1,"product1",1);
        savedEntity = repository.save(newProduct);
        assertEqualsProduct(newProduct, savedEntity);
    }

    @Test
    @DisplayName("it should success create new product")
    public void createProduct() {
        ProductEntity newProduct = new ProductEntity(2, "product2", 2);
        savedEntity = repository.save(newProduct);
        assertEqualsProduct(newProduct, savedEntity);

        ProductEntity foundProduct = repository.findByProductId(savedEntity.getProductId()).get();
        assertEqualsProduct(savedEntity, foundProduct);

        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("it should updating name to product-x")
    public void updateProduct() {
        savedEntity.setName("product-x");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findByProductId(savedEntity.getProductId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("product-x", foundEntity.getName());
    }

    @Test
    @DisplayName("it should success deleting product")
    public void deleteProduct() {
        repository.delete(savedEntity);
        assertEquals(false, repository.existsById(savedEntity.getId()));
    }

    @Test
    @DisplayName("it should able to retrieve a product record")
    public void getProductById() {
        Optional<ProductEntity> productEntity = repository.findByProductId(savedEntity.getProductId());
        assertEquals(true, productEntity.isPresent());
        assertEqualsProduct(savedEntity, productEntity.get());
    }

    @Test
    @DisplayName(("it should throw DuplicateKeyException for repeating productId (unique index)"))
    public void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity newProductEntity = new ProductEntity(savedEntity.getProductId(), "product2", 1);
            repository.save(newProductEntity);
        }, "expected to throw DuplicateKeyException");
    }

    @Test
    public void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1);

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version
        // number, that is, a Optimistic Lock Error
        try {
            entity2.setName("n2");
            repository.save(entity2);
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException ignore) {
            System.out.println(ignore.getMessage());
        }

        // Get the updated entity from the database and verify its new
        // state
        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    @Test
    public void paging() {
        repository.deleteAll();
        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                .mapToObj(id -> new ProductEntity(id,"name"+id, id))
                .collect(Collectors.toList());
        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);

        assertEquals(true, nextPage.next().isUnpaged(), "no more page available");
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds,
                productPage.getContent()
                        .stream()
                        .map(p -> p.getProductId())
                        .collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId(), "PK are equal");
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion(), "version are equal");
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId(), "product id are equal");
        assertEquals(expectedEntity.getName(),           actualEntity.getName(), "name are equal");
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight(), "weight are equal");
    }
}
