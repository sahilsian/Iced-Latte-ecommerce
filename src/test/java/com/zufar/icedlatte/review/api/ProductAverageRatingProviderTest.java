package com.zufar.icedlatte.review.api;

import com.zufar.icedlatte.openapi.dto.ProductReviewRatingStats;
import com.zufar.icedlatte.openapi.dto.RatingMap;
import com.zufar.icedlatte.product.exception.ProductNotFoundException;
import com.zufar.icedlatte.review.converter.ProductReviewDtoConverter;
import com.zufar.icedlatte.review.dto.ProductRatingCount;
import com.zufar.icedlatte.review.repository.ProductReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAverageRatingProviderTest {

    @InjectMocks
    ProductAverageRatingProvider productAverageRatingProvider;

    @Mock
    ProductReviewRepository reviewRepository;

    @Mock
    ProductReviewDtoConverter productReviewDtoConverter;

    @Test
    void getAvgRatingByProductId() {
        var randomID = UUID.randomUUID();
        var randomAverageRating = Math.random();
        when(reviewRepository.getAvgRatingByProductId(randomID)).thenReturn(randomAverageRating);

        assertEquals(randomAverageRating, productAverageRatingProvider.getAvgRatingByProductId(randomID));

        verify(reviewRepository, times(1)).getAvgRatingByProductId(randomID);
    }

    @Test
    void getRatingAndReviewStatsSuccessful() {
        var randomID = UUID.randomUUID();
        var listOfMappings = List.of(new ProductRatingCount(1, 1));
        var reviewsCount = 0;

        var expectedRatingMap = new RatingMap(1, 0, 0, 0, 0);
        var expectedStats = new ProductReviewRatingStats(randomID, "0.0", reviewsCount, expectedRatingMap);
        when(reviewRepository.getRatingsMapByProductId(randomID)).thenReturn(listOfMappings);
        when(reviewRepository.getAvgRatingByProductId(randomID)).thenReturn(0.0);
        when(reviewRepository.getReviewCountProductById(randomID)).thenReturn(reviewsCount);
        when(productReviewDtoConverter.convertToProductRatingMap(listOfMappings)).thenReturn(expectedRatingMap);

        assertEquals(expectedStats, productAverageRatingProvider.getRatingAndReviewStat(randomID));

        verify(reviewRepository, times(1)).getRatingsMapByProductId(randomID);
        verify(reviewRepository, times(1)).getAvgRatingByProductId(randomID);
        verify(reviewRepository, times(1)).getReviewCountProductById(randomID);
        verify(productReviewDtoConverter, times(1)).convertToProductRatingMap(listOfMappings);
    }

    @Test
    void getRatingAndReviewStatsFails() {
        var randomID = UUID.randomUUID();
        var listOfMappings = List.of(new ProductRatingCount(1, 1));
        when(reviewRepository.getRatingsMapByProductId(randomID)).thenReturn(null);
        when(reviewRepository.getAvgRatingByProductId(randomID)).thenReturn(null);

        assertThrows(ProductNotFoundException.class, () -> productAverageRatingProvider.getRatingAndReviewStat(randomID));

        verify(reviewRepository, times(1)).getRatingsMapByProductId(randomID);        verify(reviewRepository, times(1)).getRatingsMapByProductId(randomID);
        verify(reviewRepository, times(1)).getAvgRatingByProductId(randomID);
        verify(reviewRepository, times(0)).getReviewCountProductById(randomID);
        verify(productReviewDtoConverter, times(0)).convertToProductRatingMap(listOfMappings);
    }
}
