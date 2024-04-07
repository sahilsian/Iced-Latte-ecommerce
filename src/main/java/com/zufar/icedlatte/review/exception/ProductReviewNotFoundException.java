package com.zufar.icedlatte.review.exception;

import com.zufar.icedlatte.common.exception.ResourceNotFoundException;
import java.util.UUID;

public class ProductReviewNotFoundException extends ResourceNotFoundException {

    public ProductReviewNotFoundException(UUID productReviewId) {
        super(String.format("Product's review with productReviewId = '%s' was not found", productReviewId));
    }
}
