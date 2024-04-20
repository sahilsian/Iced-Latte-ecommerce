package com.zufar.icedlatte.product.api.rating;

import com.zufar.icedlatte.openapi.dto.ProductInfoDto;
import com.zufar.icedlatte.review.api.ProductAverageRatingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAverageRatingUpdater {

    private final ProductAverageRatingProvider productRatingProvider;
    public ProductInfoDto update(ProductInfoDto productInfoDto) {
        final UUID productId = productInfoDto.getId();
        final Double result = productRatingProvider.getAvgRatingByProductId(productId);
        productInfoDto.setAverageRating(result == null ? BigDecimal.ZERO : BigDecimal.valueOf(result));
        return productInfoDto;
    }
}
