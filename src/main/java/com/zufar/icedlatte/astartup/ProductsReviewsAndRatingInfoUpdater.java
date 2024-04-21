package com.zufar.icedlatte.astartup;

import com.zufar.icedlatte.product.entity.ProductInfo;
import com.zufar.icedlatte.product.repository.ProductInfoRepository;
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        for (ProductInfo productInfo : productInfoRepository.findAll()) {
            UUID productId = productInfo.getProductId();
            productInfoRepository.updateAverageRating(productId);
            productInfoRepository.updateReviewsCount(productId);
        }
    }
}
