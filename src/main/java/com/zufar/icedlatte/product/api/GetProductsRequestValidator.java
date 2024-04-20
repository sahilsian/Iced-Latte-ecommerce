package com.zufar.icedlatte.product.api;

import com.zufar.icedlatte.product.exception.GetProductsBadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class GetProductsRequestValidator {

    private static final Set<String> ALLOWED_SORT_DIRECTION_VALUES = Set.of("asc", "desc");
    private static final Set<String> ALLOWED_SORT_ATTRIBUTES_VALUES = Set.of("name", "price", "quantity", "averageRating", "reviewsCount", "brandName", "sellerName");
    private static final Set<Integer> ALLOWED_MINIMUM_AVERAGE_RATING_VALUES = Set.of(1, 2, 3, 4);

    public void validate(final Integer pageNumber,
                         final Integer pageSize,
                         final String sortAttribute,
                         final String sortDirection,
                         final BigDecimal minPrice,
                         final BigDecimal maxPrice,
                         final Integer minimumAverageRating) {
        StringBuilder errorMessages = new StringBuilder();

        validatePaginationAttributes(pageNumber, pageSize, sortAttribute, sortDirection, errorMessages);
        validateGetProductsRequestParameters(minPrice, maxPrice, minimumAverageRating, errorMessages);

        if (!errorMessages.isEmpty()) {
            throw new GetProductsBadRequestException(errorMessages.toString());
        }
    }

    private void validateGetProductsRequestParameters(BigDecimal minPrice, BigDecimal maxPrice, Integer minimumAverageRating, StringBuilder errorMessages) {
        if (minPrice != null && minPrice.signum() < 0) {
            String errorMessage = String.format("'%s' is incorrect 'minPrice' value. 'MinPrice' value should be non negative integer or decimal number value.", minPrice);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            String errorMessage = String.format("'%s' is incorrect 'maxPrice' value. 'MaxPrice' value should be non negative integer or decimal number value.", maxPrice);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            String errorMessage = String.format("'%s' and '%s' are incorrect 'minPrice' and 'maxPrice' values. 'MaxPrice' value should be bigger than 'MinPrice' value.", minPrice, maxPrice);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (minimumAverageRating != null && !ALLOWED_MINIMUM_AVERAGE_RATING_VALUES.contains(minimumAverageRating)) {
            String errorMessage = String.format("'%s' is incorrect 'minimumAverageRating' value. Allowed 'minimumAverageRating' values are '%s'.",
                            minimumAverageRating, ALLOWED_MINIMUM_AVERAGE_RATING_VALUES);
            errorMessages.append(createErrorMessage(errorMessage));
        }
    }

    private void validatePaginationAttributes(Integer pageNumber, Integer pageSize, String sortAttribute, String sortDirection, StringBuilder errorMessages) {
        if (pageNumber != null && pageNumber < 0) {
            String errorMessage = String.format("'%s' is the incorrect 'PageNumber' attribute value. " +
                    "'PageNumber' value should be non negative integer number value.", pageNumber);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (pageSize != null && pageSize < 1) {
            String errorMessage = String.format("'%s' is the incorrect 'PageSize' attribute value. " +
                    "'PageSize' value should be non negative integer number value which is bigger than 1.", pageSize);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (sortAttribute != null && !ALLOWED_SORT_ATTRIBUTES_VALUES.contains(sortAttribute.toLowerCase())) {
            String errorMessage = String.format("'%s' is incorrect 'sortAttribute' value. Allowed 'sortAttribute' values are '%s'.",
                    sortAttribute, ALLOWED_SORT_ATTRIBUTES_VALUES);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (sortDirection != null && !ALLOWED_SORT_DIRECTION_VALUES.contains(sortDirection.toLowerCase())) {
            String errorMessage = String.format("'%s' is incorrect 'sortDirection' value. Allowed 'sortDirection' values are '%s'.",
                    sortDirection, ALLOWED_SORT_DIRECTION_VALUES);
            errorMessages.append(createErrorMessage(errorMessage));
        }
    }

    private static String createErrorMessage(String errorMessage) {
        return String.format("Error: { %s }. ", errorMessage);
    }
}
