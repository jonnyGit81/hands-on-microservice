package se.magnus.api.core.recomendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecommendationService {

    @GetMapping("/recommendation")
    List<Recommendation> getRecomendations(@RequestParam(value = "productId", required = true) int productId);
}
