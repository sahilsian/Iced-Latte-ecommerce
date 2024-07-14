package com.zufar.icedlatte.common.validation.pagination;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PaginationParametersValidator {

    private static final Set<String> ALLOWED_SORT_DIRECTION_VALUES = Set.of("asc", "desc");

    public StringBuilder validate(final PaginationParameters params) {
        final StringBuilder errorMessages = new StringBuilder();
        if (params.getPageNumber() != null && params.getPageNumber() < 0) {
            String errorMessage = String.format("'%s' is the incorrect 'PageNumber' attribute value. " +
                    "'PageNumber' value should be non negative integer number value.", params.getPageNumber());
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (params.getPageSize() != null && params.getPageSize() < 1) {
            String errorMessage = String.format("'%s' is the incorrect 'PageSize' attribute value. " +
                    "'PageSize' value should be non negative integer number value which is bigger than 1.", params.getPageSize());
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (params.getSortAttribute() != null && !params.getAllowedSortAttributeValues().contains(params.getSortAttribute())) {
            String errorMessage = String.format("'%s' is incorrect 'sortAttribute' value. Allowed 'sortAttribute' values are '%s'.",
            params.getSortAttribute(), params.getAllowedSortAttributeValues());
            errorMessages.append(createErrorMessage(errorMessage));
        }
        if (params.getSortDirection() != null && !ALLOWED_SORT_DIRECTION_VALUES.contains(params.getSortDirection().toLowerCase())) {
            String errorMessage = String.format("'%s' is incorrect 'sortDirection' value. Allowed 'sortDirection' values are '%s'.",
            params.getSortDirection(), ALLOWED_SORT_DIRECTION_VALUES);
            errorMessages.append(createErrorMessage(errorMessage));
        }
        return errorMessages;
    }

    private static String createErrorMessage(String errorMessage) {
        return String.format(" Error: { %s }. ", errorMessage);
    }
}
