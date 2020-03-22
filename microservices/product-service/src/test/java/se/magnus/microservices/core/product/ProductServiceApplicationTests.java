package se.magnus.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.product.Product;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.product.persistent.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient(timeout = "60000")
//10 seconds, prevent error timeout 5000ms on nonblocking call exception
class ProductServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @BeforeEach
    public void setupDb() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll().block();
    }

    @Test
    void getProductById() {
        int productId = 1;

        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, (long) repository.count().block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, (long) repository.count().block());

        getAndVerifyProduct("/" + productId, OK)
                .jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    void duplicateError() {
        int productId = 1;

        assertNull(repository.findByProductId(productId).block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());

        try {
            sendCreateProductEvent(productId);
            fail("Expected a MessagingException here!");
        } catch (MessagingException me) {
            if (me.getCause() instanceof InvalidInputException) {
                InvalidInputException iie = (InvalidInputException) me.getCause();
                assertEquals("Duplicate key, Product Id: " + productId, iie.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }
    }

    @Test
    void deleteProduct() {

        int productId = 1;

        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
    }

    //Negative test case
    @Test
    void getProductInvalidParameterString() {

        getAndVerifyProduct("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {

        int productIdNotFound = 13;
        String productIdPath = "/" + productIdNotFound;
        getAndVerifyProduct(productIdPath, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {

        int productIdInvalid = -1;
        String productIdPath = "/" + productIdInvalid;
        getAndVerifyProduct(productIdPath, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        return client.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }

    private void sendCreateProductEvent(int productId) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        Event<Integer, Product> event = new Event(CREATE, productId, product);
        input.send(new GenericMessage<>(event));
    }

    private void sendDeleteProductEvent(int productId) {
        Event<Integer, Product> event = new Event(DELETE, productId, null);
        input.send(new GenericMessage<>(event));
    }
}
