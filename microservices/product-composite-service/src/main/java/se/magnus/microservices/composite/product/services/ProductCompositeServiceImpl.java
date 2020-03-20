package se.magnus.microservices.composite.product.services;

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

    private final ProductCompositeIntegration productCompositeIntegration;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration productCompositeIntegration, ServiceUtil serviceUtil) {
        this.productCompositeIntegration = productCompositeIntegration;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = productCompositeIntegration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendationList = productCompositeIntegration.getRecomendations(productId);

        List<Review> reviewList = productCompositeIntegration.getReviews(productId);

        return createProductAggregate(product, recommendationList, reviewList);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
                                                    List<Review> reviews) {

        List<RecommendationSummary> recommendationSummaries = null;
        String recommendationServiceAddress = "";

        if (!CollectionUtils.isEmpty(recommendations)) {
            recommendationSummaries = recommendations.stream()
                    .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                    .collect(Collectors.toList());

            recommendationServiceAddress = recommendations.get(0).getServiceAddress();
        }

        List<ReviewSummary> reviewSummaries = null;
        String reviewServiceAddress = "";

        if (!CollectionUtils.isEmpty(reviews)) {
            reviewSummaries = reviews.stream()
                    .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                    .collect(Collectors.toList());

            reviewServiceAddress = reviews.get(0).getServiceAddress();
        }

        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceUtil.getServiceAddress(),
                product.getServiceAddress(), reviewServiceAddress, recommendationServiceAddress);

        return new ProductAggregate(product.getProductId(), product.getName(), product.getWeight(),
                recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}