package com.zufar.icedlatte.review.api;

import com.zufar.icedlatte.openapi.dto.ProductReviewRatingStats;
import com.zufar.icedlatte.openapi.dto.ProductReviewDto;
import com.zufar.icedlatte.openapi.dto.ProductReviewsAndRatingsWithPagination;
import com.zufar.icedlatte.product.exception.ProductNotFoundException;
import com.zufar.icedlatte.review.converter.ProductReviewDtoConverter;
import com.zufar.icedlatte.review.repository.ProductReviewRepository;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.zufar.icedlatte.common.util.Utils.createPageableObject;
import static com.zufar.icedlatte.review.converter.ProductReviewDtoConverter.EMPTY_PRODUCT_REVIEW_RESPONSE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewsProvider {

    private final ProductReviewRepository reviewRepository;
    private final ProductReviewDtoConverter productReviewDtoConverter;
    private final ProductReviewValidator productReviewValidator;
    private final SecurityPrincipalProvider securityPrincipalProvider;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public ProductReviewsAndRatingsWithPagination getProductReviews(final UUID productId,
                                                                    final Integer page,
                                                                    final Integer size,
                                                                    final String sortAttribute,
                                                                    final String sortDirection) {
        productReviewValidator.validateProductExists(productId);
        Pageable pageable = createPageableObject(page, size, sortAttribute, sortDirection);
        Page<ProductReviewDto> responsePage = reviewRepository
                .findByProductInfoProductId(productId, pageable)
                .map(productReviewDtoConverter::toProductReviewDto);
        return productReviewDtoConverter.toProductReviewsAndRatingsWithPagination(responsePage);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public ProductReviewDto getProductReviewForUser(final UUID productId) {
        productReviewValidator.validateProductExists(productId);
        var userId = securityPrincipalProvider.getUserId();
        return reviewRepository.findByUserIdAndProductInfoProductId(userId, productId)
                .map(productReviewDtoConverter::toProductReviewDto)
                .orElse(EMPTY_PRODUCT_REVIEW_RESPONSE);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public ProductReviewRatingStats getRatingAndReviewStat(final UUID productId) {
        List<Object[]> productRatingCountPairs = reviewRepository.getRatingsMapByProductId(productId);
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