package com.zufar.icedlatte.review.converter;

import com.zufar.icedlatte.openapi.dto.ProductReviewResponse;
import com.zufar.icedlatte.openapi.dto.ProductReviewsAndRatingsWithPagination;
import com.zufar.icedlatte.openapi.dto.RatingMap;
import com.zufar.icedlatte.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductReviewDtoConverter {

    public static final ProductReviewResponse EMPTY_PRODUCT_REVIEW_RESPONSE =
            new ProductReviewResponse(null, null, null, null, null, null);

    public ProductReviewResponse toReviewResponse(ProductReview productReview) {
        return new ProductReviewResponse(
                productReview.getId(),
                productReview.getProductRating(),
                productReview.getText(),
                productReview.getCreatedAt(),
                productReview.getUser().getFirstName(),
                productReview.getUser().getLastName());
    }

    public ProductReviewsAndRatingsWithPagination toProductReviewsAndRatingsWithPagination(final Page<ProductReviewResponse> page) {
        var result = new ProductReviewsAndRatingsWithPagination();
        result.setPage(page.getTotalPages());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setReviewsWithRatings(page.getContent());
        return result;
    }

    public RatingMap convertToProductRatingMap(List<Object[]> listOfMappings) {
        var productRatingMap = new RatingMap();
        for (Object[] arr : listOfMappings) {
            var productRating = (Integer) arr[0];
            var count = ((Long) arr[1]).intValue();
            switch (productRating) {
                case 5:
                    productRatingMap._5(count);
                    break;
                case 4:
                    productRatingMap._4(count);
                    break;
                case 3:
                    productRatingMap._3(count);
                    break;
                case 2:
                    productRatingMap._2(count);
                    break;
                case 1:
                    productRatingMap._1(count);
                    break;
                default:
                    assert false : "Unexpected product's rating value";
            }
        }
        return productRatingMap;
    }
}
