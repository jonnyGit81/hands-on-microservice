package se.magnus.api.composite;

public class ServiceAddresses {
    private final String cmpositeServiceAddress;
    private final String productServiceAddress;
    private final String reviewServiceAddress;
    private final String recomendationServiceAddress;

    public ServiceAddresses() {
        this.cmpositeServiceAddress = null;
        this.productServiceAddress = null;
        this.reviewServiceAddress = null;
        this.recomendationServiceAddress = null;
    }

    public ServiceAddresses(String cmpositeServiceAddress, String productServiceAddress, String reviewServiceAddress, String recomendationServiceAddress) {
        this.cmpositeServiceAddress = cmpositeServiceAddress;
        this.productServiceAddress = productServiceAddress;
        this.reviewServiceAddress = reviewServiceAddress;
        this.recomendationServiceAddress = recomendationServiceAddress;
    }

    public String getCmpositeServiceAddress() {
        return cmpositeServiceAddress;
    }

    public String getProductServiceAddress() {
        return productServiceAddress;
    }

    public String getReviewServiceAddress() {
        return reviewServiceAddress;
    }

    public String getRecomendationServiceAddress() {
        return recomendationServiceAddress;
    }
}
