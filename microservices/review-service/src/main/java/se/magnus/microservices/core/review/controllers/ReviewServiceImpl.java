package se.magnus.microservices.core.review.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.microservices.core.review.persistent.ReviewEntity;
import se.magnus.microservices.core.review.persistent.ReviewRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {

        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;

    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> entities = repository.findByProductId(productId);
        List<Review> reviews = mapper.entityListToApiList(entities);
        String serviceAddresses = serviceUtil.getServiceAddress();
        reviews.forEach(review -> review.setServiceAddress(serviceAddresses));
        LOG.debug("/reviews response size: {}", entities.size());

        return reviews;
    }

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity newEntity = mapper.apiToEntity(body);
            repository.save(newEntity);

            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" +
                    body.getReviewId(), dke);
        }
    }

    @Override
    public void deleteReviews(int productId) {
        //. Note that the implementation is idempotent, that is, it will not report any failure if the entity is not found
        // because HTTP DELETE is idempotent, also for UPDATE
        repository.deleteAll(repository.findByProductId(productId));
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    }
}
