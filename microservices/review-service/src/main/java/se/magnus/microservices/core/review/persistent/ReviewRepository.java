package se.magnus.microservices.core.review.persistent;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    List<ReviewEntity> findByProductId(Integer productId);
}
