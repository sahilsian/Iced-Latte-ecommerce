package com.zufar.icedlatte.review.repository;

import com.zufar.icedlatte.review.dto.ProductRatingCount;
import com.zufar.icedlatte.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT COUNT(pr) " +
            "FROM ProductReview pr " +
            "WHERE pr.productInfo.productId = :productId")
    Integer getReviewCountProductById(UUID productId);

    @Query("SELECT AVG(pr.productRating) " +
            "FROM ProductReview pr " +
            "WHERE pr.productInfo.productId = :productId")
    Double getAvgRatingByProductId(UUID productId);

    @Query("SELECT new com.zufar.icedlatte.review.dto.ProductRatingCount(pr.productRating, COUNT(pr.productRating)) " +
            "FROM ProductReview pr " +
            "WHERE pr.productInfo.productId = :productId " +
            "GROUP BY pr.productRating")
    List<ProductRatingCount> getRatingsMapByProductId(UUID productId);

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE product_reviews " +
                    "SET likes_count = (" +
                        "SELECT count(product_reviews_likes.id)" +
                        "FROM product_reviews_likes " +
                        "WHERE product_reviews_likes.is_like = true AND product_reviews_likes.review_id = product_reviews.id" +
                    ") " +
                    "WHERE product_reviews.id = :productReviewId")
    void updateLikesCount(final UUID productReviewId);

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE product_reviews " +
                    "SET dislikes_count = (" +
                        "SELECT count(product_reviews_likes.id)" +
                        "FROM product_reviews_likes " +
                        "WHERE product_reviews_likes.is_like = false AND product_reviews_likes.review_id = product_reviews.id" +
                    ") " +
                    "WHERE product_reviews.id = :productReviewId")
    void updateDislikesCount(final UUID productReviewId);
}
