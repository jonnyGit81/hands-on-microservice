package se.magnus.api.core.recomendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface RecommendationService {

    @GetMapping(value = "/recommendation", produces = APPLICATION_JSON_VALUE)
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"recommendationId":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value    = "/recommendation",
            consumes = "application/json",
            produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation body);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     */
    @DeleteMapping(value = "/recommendation")
    void deleteRecommendations(@RequestParam(value = "productId", required = true)  int productId);
}
