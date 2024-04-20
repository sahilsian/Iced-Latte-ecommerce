package com.zufar.icedlatte.review.api;

import com.zufar.icedlatte.openapi.dto.ProductReviewRatingStats;
import com.zufar.icedlatte.product.exception.ProductNotFoundException;
import com.zufar.icedlatte.review.converter.ProductReviewDtoConverter;
import com.zufar.icedlatte.review.dto.ProductRatingCount;
import com.zufar.icedlatte.review.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewsStatisticsProvider {

    private final ProductReviewRepository reviewRepository;
    private final ProductReviewDtoConverter productReviewDtoConverter;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Double getAvgRatingByProductId(final UUID productId) {
        return reviewRepository.getAvgRatingByProductId(productId);
    }


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public ProductReviewRatingStats getRatingAndReviewStat(final UUID productId) {
        List<ProductRatingCount> productRatingCountPairs = reviewRepository.getRatingsMapByProductId(productId);
        Double avgRating = reviewRepository.getAvgRatingByProductId(productId);
        if (productRatingCountPairs == null || avgRating == null) {
            log.error("The product with productId = {} was not found.", productId);
            throw new ProductNotFoundException(productId);
        }
        String formattedAvgRating = String.format("%.1f", avgRating);
        Integer reviewsCount = reviewRepository.getReviewCountProductById(productId);
        return new ProductReviewRatingStats(productId, formattedAvgRating, reviewsCount,
                productReviewDtoConverter.convertToProductRatingMap(productRatingCountPairs));
    }
}
