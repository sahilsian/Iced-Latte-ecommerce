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

    public static final ProductReviewResponse EMPTY_REVIEW_RESPONSE;

    static {
        var response = new ProductReviewResponse();
        response.setProductReviewId(null);
        response.setCreatedAt(null);
        response.setText(null);
        response.setRating(null);
        EMPTY_REVIEW_RESPONSE = response;
    }

    public ProductReviewResponse toReviewResponse(ProductReview productReview){
        var response = new ProductReviewResponse();
        response.setProductReviewId(productReview.getId());
        response.setCreatedAt(productReview.getCreatedAt());
        response.setText(productReview.getText());
        response.setRating(productReview.getProductRating());
        response.setUserName(productReview.getUser().getFirstName());
        response.setUserLastName(productReview.getUser().getLastName());
        return response;
    }

    public ProductReviewsAndRatingsWithPagination toProductReviewsAndRatingsWithPagination(final Page<ProductReviewResponse> page){
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
