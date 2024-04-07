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

    public RatingMap convertToRatingMap(List<Object[]> listOfMappings) {
        var map = new RatingMap();
        for (Object[] arr : listOfMappings) {
            var rating = (Integer) arr[0];
            var count = ((Long) arr[1]).intValue();
            switch (rating) {
                case 5:
                    map._5(count);
                    break;
                case 4:
                    map._4(count);
                    break;
                case 3:
                    map._3(count);
                    break;
                case 2:
                    map._2(count);
                    break;
                case 1:
                    map._1(count);
                    break;
                default:
                    assert false : "Unexpected rating value";
            }
        }
        return map;
    }
}
