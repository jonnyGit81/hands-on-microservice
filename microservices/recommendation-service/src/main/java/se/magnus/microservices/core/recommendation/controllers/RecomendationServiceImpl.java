package se.magnus.microservices.core.recommendation.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.recomendation.Recommendation;
import se.magnus.api.core.recomendation.RecommendationService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class RecomendationServiceImpl implements RecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecomendationServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecomendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecomendations(int productId) {
        if(productId < 1) {
            throw new InvalidInputException("invalid product id, " + productId);
        }

        if(productId == 13) {
            LOGGER.debug("No recommendations found for productId: {}", productId);
            return Collections.emptyList();
        }

        List<Recommendation> list = new ArrayList<>();
        list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

        LOGGER.debug("/recomendations response size {}", list.size());

        return list;
    }
}
