package com.zufar.icedlatte.review.repository;

import com.zufar.icedlatte.review.entity.ProductReviewRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductReviewRateRepository extends JpaRepository<ProductReviewRate, UUID> {

    Optional<ProductReviewRate> findByUserIdAndReviewId(UUID userId, UUID reviewId);

    List<ProductReviewRate> findAllByUserIdAndReviewProductInfoProductId(UUID userId, UUID productId);
}
