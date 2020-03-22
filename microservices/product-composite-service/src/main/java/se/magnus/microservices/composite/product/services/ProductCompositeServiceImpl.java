package se.magnus.microservices.composite.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.composite.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recomendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ProductCompositeIntegration integration;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration productCompositeIntegration, ServiceUtil serviceUtil) {
        this.integration = productCompositeIntegration;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendationList = integration.getRecommendations(productId);

        List<Review> reviewList = integration.getReviews(productId);

        return createProductAggregate(product, recommendationList, reviewList);
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {
        try {

            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(),
                            r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(),
                            r.getContent(), null);
                    integration.createReview(review);
                });
            }

            LOG.debug("createCompositeProduct: composite entites created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public void deleteCompositeProduct(int productId) {
        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);

        integration.deleteRecommendations(productId);

        integration.deleteReviews(productId);

        LOG.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
                                                    List<Review> reviews) {

        List<RecommendationSummary> recommendationSummaries = null;
        String recommendationServiceAddress = "";

        if (!CollectionUtils.isEmpty(recommendations)) {
            recommendationSummaries = recommendations.stream()
                    .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                    .collect(Collectors.toList());

            recommendationServiceAddress = recommendations.get(0).getServiceAddress();
        }

        List<ReviewSummary> reviewSummaries = null;
        String reviewServiceAddress = "";

        if (!CollectionUtils.isEmpty(reviews)) {
            reviewSummaries = reviews.stream()
                    .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                    .collect(Collectors.toList());

            reviewServiceAddress = reviews.get(0).getServiceAddress();
        }

        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceUtil.getServiceAddress(),
                product.getServiceAddress(), reviewServiceAddress, recommendationServiceAddress);

        return new ProductAggregate(product.getProductId(), product.getName(), product.getWeight(),
                recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}