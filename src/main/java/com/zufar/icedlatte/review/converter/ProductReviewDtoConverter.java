package com.zufar.icedlatte.review.converter;

import com.zufar.icedlatte.openapi.dto.ProductReviewRateDto;
import com.zufar.icedlatte.openapi.dto.ProductReviewResponse;
import com.zufar.icedlatte.openapi.dto.ProductReviewsAndRatingsWithPagination;
import com.zufar.icedlatte.openapi.dto.RatingMap;
import com.zufar.icedlatte.review.dto.ProductRatingCount;
import com.zufar.icedlatte.review.entity.ProductReview;
import com.zufar.icedlatte.review.entity.ProductReviewRate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductReviewDtoConverter {

    public static final ProductReviewResponse EMPTY_PRODUCT_REVIEW_RESPONSE =
            new ProductReviewResponse(null, null, null, null, null, null, null, null);

    public ProductReviewResponse toReviewResponse(ProductReview productReview) {
        var likes = productReview.getReviewRates() == null ? 0 : (int) productReview.getReviewRates().stream().filter(ProductReviewRate::getIsLike).count();
        var disLikes = productReview.getReviewRates() == null ? 0 : (int) productReview.getReviewRates().stream().filter(r -> !r.getIsLike()).count();
        return new ProductReviewResponse(
                productReview.getId(),
                productReview.getProductRating(),
                productReview.getText(),
                productReview.getCreatedAt(),
                productReview.getUser().getFirstName(),
                productReview.getUser().getLastName(),
                likes, disLikes);
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

    public RatingMap convertToProductRatingMap(List<ProductRatingCount> productRatingCountPairs) {
        var productRatingMap = new RatingMap(0, 0, 0, 0, 0);

        for (ProductRatingCount prc : productRatingCountPairs) {
            var productRating = prc.productRating();
            var count = (int) prc.count();
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

    public ProductReviewRateDto toProductReviewRateDto(ProductReviewRate entity) {
        return new ProductReviewRateDto(entity.getReview().getId(), entity.getIsLike());
    }
}
