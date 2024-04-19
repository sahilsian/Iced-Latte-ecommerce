package com.zufar.icedlatte.product.repository;

import com.zufar.icedlatte.product.entity.ProductInfo;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.UUID;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, UUID> {

    @Query("SELECT p FROM ProductInfo p " +
            "WHERE (:minPrice IS NULL OR :maxPrice IS NULL OR p.price BETWEEN :minPrice AND :maxPrice) " +
            "AND (:minRating IS NULL OR :maxRating IS NULL OR p.averageRating BETWEEN :minRating AND :maxRating) ")
    Page<ProductInfo> findAllProducts(
            @Param(value = "minPrice") Integer minPrice,
            @Param(value = "maxPrice") Integer maxPrice,
            @Param(value = "minRating") Integer minRating,
            Pageable pageable);

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE product p " +
                    "SET average_rating = (" +
                        "SELECT AVG(pr.rating) " +
                        "FROM product_reviews pr " +
                        "WHERE pr.product_id = p.id" +
                    ") " +
                    "WHERE p.id = :productId")
    void updateAverageRating(final UUID productId);

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE product p " +
                    "SET reviews_count = (" +
                        "SELECT COUNT(pr.id) " +
                        "FROM product_reviews pr " +
                        "WHERE pr.product_id = p.id" +
                    ") " +
                    "WHERE p.id = :productId")
    void updateReviewsCount(final UUID productId);
}