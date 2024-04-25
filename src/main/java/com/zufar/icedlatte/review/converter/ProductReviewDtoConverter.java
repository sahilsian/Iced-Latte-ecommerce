package com.zufar.icedlatte.review.converter;

import com.zufar.icedlatte.openapi.dto.ProductReviewDto;
import com.zufar.icedlatte.openapi.dto.ProductReviewsAndRatingsWithPagination;
import com.zufar.icedlatte.openapi.dto.RatingMap;
import com.zufar.icedlatte.review.dto.ProductRatingCount;
import com.zufar.icedlatte.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductReviewDtoConverter {

    public static final ProductReviewDto EMPTY_PRODUCT_REVIEW_RESPONSE =
            new ProductReviewDto(null, null,null, null, null, null, null, null, null);

    public ProductReviewDto toProductReviewDto(ProductReview productReview) {
        return new ProductReviewDto(
                productReview.getId(),
                productReview.getProductId(),
                productReview.getProductRating(),
                productReview.getText(),
                productReview.getCreatedAt(),
                productReview.getUser().getFirstName(),
                productReview.getUser().getLastName(),
                productReview.getLikesCount(),
                productReview.getDislikesCount());
    }

    public ProductReviewsAndRatingsWithPagination toProductReviewsAndRatingsWithPagination(final Page<ProductReviewDto> page) {
        var result = new ProductReviewsAndRatingsWithPagination();
        result.setPage(page.getTotalPages());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setReviewsWithRatings(page.getContent());
        return result;
    }

    public RatingMap convertToProductRatingMap(List<ProductRatingCount> productRatingCountPairs) {
        var productRatingMap = new RatingMap(0, 0, 0, 0, 0);

        for (ProductRatingCount productRatingCount : productRatingCountPairs) {
            var productRating = productRatingCount.productRating();
            var count = (int) productRatingCount.count();

            switch (productRating) {
                case 5:
                    productRatingMap.setStar5(count);
                    break;
                case 4:
                    productRatingMap.setStar4(count);
                    break;
                case 3:
                    productRatingMap.setStar3(count);
                    break;
                case 2:
                    productRatingMap.setStar2(count);
                    break;
                case 1:
                    productRatingMap.setStar1(count);
                    break;
                default:
                    assert false : "Unexpected product's rating value";
            }
        }
        return productRatingMap;
    }
}
