package se.magnus.microservices.core.recommendation.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.recomendation.Recommendation;
import se.magnus.api.core.recomendation.RecommendationService;
import se.magnus.microservices.core.recommendation.persistent.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistent.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper) {

        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;

    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if(productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        String serviceAddress = serviceUtil.getServiceAddress();
        List<Recommendation> recommendationList = mapper.entityListToApiList(entityList);
        recommendationList.forEach(recommendation -> recommendation.setServiceAddress(serviceAddress));

        return recommendationList;

    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        RecommendationEntity entity = mapper.apiToEntity(body);

        try {

            repository.save(entity);
            LOGGER.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(entity);

        } catch (DuplicateKeyException e) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
        }

    }

    @Override
    public void deleteRecommendations(int productId) {

        LOGGER.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));

    }
}
