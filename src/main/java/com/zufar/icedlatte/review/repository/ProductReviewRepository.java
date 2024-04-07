package com.zufar.icedlatte.review.repository;

import com.zufar.icedlatte.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    Optional<ProductReview> findByUserIdAndProductInfoProductId(UUID userId, UUID productId);

    Page<ProductReview> findByProductInfoProductId(@Param("productId") UUID productId, Pageable pageable);

    Page<ProductReview> findByProductInfoProductIdAndUserIdNot(UUID productId, UUID userId, Pageable pageable);

    @Query("SELECT COUNT(pr) FROM ProductReview pr WHERE pr.productInfo.productId = :productId")
    Integer getReviewCountProductById(UUID productId);

    @Query("SELECT AVG(pr.productRating) FROM ProductReview pr WHERE pr.productInfo.productId = :productId")
    Double getAvgRatingByProductId(UUID productId);

    @Query("SELECT pr.productRating, COUNT(pr.productRating) FROM ProductReview pr WHERE pr.productInfo.productId = :productId GROUP BY pr.productRating")
    List<Object[]> getRatingsMapByProductId(UUID productId);
}
