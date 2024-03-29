package com.zufar.icedlatte.review.endpoint;

import com.zufar.icedlatte.openapi.dto.ProductRatingDto;
import com.zufar.icedlatte.openapi.dto.ProductReviewRatingStats;
import com.zufar.icedlatte.openapi.product.rating.api.ProductRatingApi;
import com.zufar.icedlatte.review.api.ProductRatingProvider;
import com.zufar.icedlatte.review.api.ProductRatingUpdater;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = ProductRatingEndpoint.RATING_URL)
public class ProductRatingEndpoint implements ProductRatingApi {

    public static final String RATING_URL = "/api/v1/products/";

    private final ProductRatingUpdater productRatingUpdater;
    private final ProductRatingProvider productRatingProvider;
    private final SecurityPrincipalProvider securityPrincipalProvider;

    @Override
    @PostMapping("/{productId}/ratings/{productRating}")
    public ResponseEntity<ProductRatingDto> addNewProductRating(@PathVariable final UUID productId,
                                                                @PathVariable @Max(5) @Min(1) final Integer productRating) {
        log.info("Received the request to add new rating = '{}' to the product with the id = '{}'", productRating, productId);
        final UUID userId = securityPrincipalProvider.getUserId();
        final ProductRatingDto productRatingDto = productRatingUpdater.addNewRating(userId, productId, productRating);
        log.info("Rating  = '{}' was added to the product with the id = '{}'", productRating, productId);
        return ResponseEntity.ok().body(productRatingDto);
    }

    @Override
    @GetMapping("/{productId}/ratings/statistics")
    public ResponseEntity<ProductReviewRatingStats> getRatingAndReviewStat(@PathVariable final UUID productId) {
        log.info("Received the request to get the review's and rating's statistics for the product with the id = '{}'", productId);
        final ProductReviewRatingStats stats = productRatingProvider.getRatingAndReviewStat(productId);
        log.info("Product review and rating statistics were retrieved successfully for the product with the id = '{}'", productId);
        return ResponseEntity.ok().body(stats);
    }
}
