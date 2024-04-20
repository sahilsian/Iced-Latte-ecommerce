package com.zufar.icedlatte.review.api;

import com.zufar.icedlatte.openapi.dto.ProductReviewRateDto;
import com.zufar.icedlatte.review.converter.ProductReviewDtoConverter;
import com.zufar.icedlatte.review.repository.ProductReviewRateRepository;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewRateProvider {

    private final ProductReviewRateRepository repository;
    private final SecurityPrincipalProvider securityPrincipalProvider;
    private final ProductReviewDtoConverter converter;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<ProductReviewRateDto> getProductReviewRates(UUID productId) {
        var userId = securityPrincipalProvider.getUserId();
        return repository.findAllByUserIdAndReviewProductInfoProductId(userId, productId)
                .stream().map(converter::toProductReviewRateDto).toList();
    }
}
