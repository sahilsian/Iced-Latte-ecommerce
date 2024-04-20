package com.zufar.icedlatte.review.exception;

import java.util.UUID;

public class DeniedProductReviewRateUpdateException extends RuntimeException {
    public DeniedProductReviewRateUpdateException(final UUID userId, final UUID productReviewId) {
        super(String.format("Update of the product review rate with productReviewId = '%s' is denied for the user with userId = '%s'",
                productReviewId, userId));
    }
}
