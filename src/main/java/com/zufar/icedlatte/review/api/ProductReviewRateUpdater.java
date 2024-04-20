package com.zufar.icedlatte.review.api;

import com.zufar.icedlatte.openapi.dto.ProductReviewRateDto;
import com.zufar.icedlatte.review.converter.ProductReviewDtoConverter;
import com.zufar.icedlatte.review.entity.ProductReviewRate;
import com.zufar.icedlatte.review.repository.ProductReviewRateRepository;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import com.zufar.icedlatte.user.api.SingleUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewRateUpdater {

    private final ProductReviewRateRepository repository;
    private final SecurityPrincipalProvider securityPrincipalProvider;
    private final SingleUserProvider singleUserProvider;
    private final ProductReviewProvider productReviewProvider;
    private final ProductReviewDtoConverter converter;
    private final ProductReviewValidator validator;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public ProductReviewRateDto updateReviewRate(UUID productId, ProductReviewRateDto request) {
        var userId = securityPrincipalProvider.getUserId();
        var reviewId = request.getProductReviewId();

        var reviewRateEntry = repository.findByUserIdAndReviewId(userId, reviewId)
                .orElseGet(() -> createProductReviewRate(userId, reviewId));
        validator.validateProductReviewRateUpdateAllowed(productId, request, reviewRateEntry);
        reviewRateEntry.setIsLike(request.getIsLike());
        repository.save(reviewRateEntry);
        return converter.toProductReviewRateDto(reviewRateEntry);
    }

    private ProductReviewRate createProductReviewRate(UUID userId, UUID reviewId) {
        return ProductReviewRate.builder()
                .user(singleUserProvider.getUserEntityById(userId))
                .review(productReviewProvider.getReviewEntityById(reviewId))
                .build();
    }
}
