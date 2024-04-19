package com.zufar.icedlatte.product.repository;

import com.zufar.icedlatte.product.entity.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, UUID> {

    @Modifying
    @Query(value = "UPDATE product p SET average_rating = (" +
            "SELECT AVG(pr.rating) " +
            "FROM product_reviews pr " +
            "WHERE pr.product_id = p.id" +
            ") " +
            "WHERE p.id = :productId", nativeQuery = true)
    void updateAverageRating(UUID productId);

    @Modifying
    @Query(value = "UPDATE product p SET reviews_count = (" +
            "SELECT COUNT(pr.id) " +
            "FROM product_reviews pr " +
            "WHERE pr.product_id = p.id" +
            ") " +
            "WHERE p.id = :productId", nativeQuery = true)
    void updateReviewsCount(UUID productId);
}