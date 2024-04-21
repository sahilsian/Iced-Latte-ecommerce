package com.zufar.icedlatte.astartup;

import com.zufar.icedlatte.product.entity.ProductInfo;
import com.zufar.icedlatte.product.repository.ProductInfoRepository;
import com.zufar.icedlatte.review.entity.ProductReview;
import com.zufar.icedlatte.review.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductsReviewsAndRatingInfoUpdater implements ApplicationRunner {

    private final ProductInfoRepository productInfoRepository;
    private final ProductReviewRepository productReviewRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        for (ProductInfo productInfo : productInfoRepository.findAll()) {
            UUID productId = productInfo.getProductId();
            productInfoRepository.updateAverageRating(productId);
            productInfoRepository.updateReviewsCount(productId);
        }
        for (ProductReview productReview : productReviewRepository.findAll()) {
            UUID productReviewId = productReview.getId();
            productReviewRepository.updateLikesCount(productReviewId);
            productReviewRepository.updateDislikesCount(productReviewId);
        }
    }
}
