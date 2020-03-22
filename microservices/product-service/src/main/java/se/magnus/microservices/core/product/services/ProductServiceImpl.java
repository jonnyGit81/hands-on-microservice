package se.magnus.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistent.ProductEntity;
import se.magnus.microservices.core.product.persistent.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository productRepository, ProductMapper productMapper) {
        this.serviceUtil = serviceUtil;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity productEntity = productRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        /*if (productId == 13) {
            throw new NotFoundException("No product found for productId: " + productId);
        }*/

        LOG.debug("/product return the found product for productId={}", productId);
        Product response = productMapper.entityToApi(productEntity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("/product return the found product for productId={}", response.toString());

        return response;

    }

    @Override
    public Product createProduct(Product body) {
        ProductEntity productEntity = productMapper.apiToEntity(body);

        try {

            productRepository.save(productEntity);
            LOG.debug("createProduct: entity created for productId: {}", body.getProductId());

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate Key, productId " + body.getProductId());
        }

        return productMapper.entityToApi(productEntity);
    }

    @Override
    public void deleteProduct(int productId) {

        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        productRepository.findByProductId(productId).ifPresent(productEntity -> productRepository.delete(productEntity));

    }

}
