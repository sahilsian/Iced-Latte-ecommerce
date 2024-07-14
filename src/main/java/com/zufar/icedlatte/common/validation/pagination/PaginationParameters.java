package com.zufar.icedlatte.common.validation.pagination;

import java.util.Set;

public class PaginationParameters {

    private final Integer pageNumber;
    private final Integer pageSize;
    private final String sortAttribute;
    private final String sortDirection;
    private final Set<String> allowedSortAttributeValues;

    public PaginationParameters(Integer pageNumber, Integer pageSize, String sortAttribute, String sortDirection, Set<String> allowedSortAttributeValues) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortAttribute = sortAttribute;
        this.sortDirection = sortDirection;
        this.allowedSortAttributeValues = allowedSortAttributeValues;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getSortAttribute() {
        return sortAttribute;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public Set<String> getAllowedSortAttributeValues() {
        return allowedSortAttributeValues;
    }
}
