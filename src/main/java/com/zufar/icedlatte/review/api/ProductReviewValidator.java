package com.zufar.icedlatte.review.api;

import com.zufar.icedlatte.product.repository.ProductInfoRepository;
import com.zufar.icedlatte.review.exception.*;
import com.zufar.icedlatte.review.repository.ProductReviewRepository;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewValidator {

    private final SecurityPrincipalProvider securityPrincipalProvider;
    private final ProductReviewProvider productReviewProvider;
    private final ProductReviewRepository productReviewRepository;
    private final ProductInfoRepository productInfoRepository;

    /**
     * Check if the product review's text is not empty
     */
    public void validateReviewText(final String productReviewText) {
        if (productReviewText.isEmpty()) {
            throw new EmptyProductReviewException();
        }
    }

    /**
     * Check if the product exists
     */
    public void validateProductExists(final UUID productId) {
        var productInfo = productInfoRepository.findById(productId);
        if (productInfo.isEmpty()) {
            throw new ProductNotFoundForReviewException(productId);
        }
    }

    /**
     * Check if the user has already created a review for this product
     */
    public void validateReviewExists(final UUID userId,
                                     final UUID productId) {
        var productReview = productReviewRepository.findByUserIdAndProductInfoProductId(userId, productId);
        if (productReview.isEmpty()) {
            throw new DeniedProductReviewCreationException(productId, userId, productReview.get().getId());
        }
    }

    /**
     * Check if the user has already created a review for this product
     */
    public void validateReviewExists(final UUID productReviewId) {
        var productReview = productReviewRepository.findById(productReviewId);
        if (productReview.isEmpty()) {
            throw new ProductReviewNotFoundException(productReviewId);
        }
    }

    /**
     * Check if the product's review deletion is allowed
     */
    public void validateProductReviewDeletionAllowed(final UUID productReviewId) {
        var currentUserId = securityPrincipalProvider.getUserId();
        var creatorId = productReviewProvider.getReviewEntityById(productReviewId).getUser().getId();

        if (!currentUserId.equals(creatorId)) {
            throw new DeniedProductReviewDeletionException(productReviewId, currentUserId);
        }
    }

    /**
     * Check if the product's review deletion is allowed
     */
    public void validateProductIdIsValid(final UUID productId,
                                         final UUID productReviewId) {
        var product = productInfoRepository.findById(productId);
        var productReview = productReviewProvider.getReviewEntityById(productReviewId);

        if (!product.get().getProductId().equals(productReview.getProductInfo().getProductId())) {
            throw new ProductIdsAreNotMatchException(productReviewId);
        }
    }
}
