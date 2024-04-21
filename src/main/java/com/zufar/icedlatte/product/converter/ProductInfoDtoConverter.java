package com.zufar.icedlatte.product.converter;

import com.zufar.icedlatte.openapi.dto.ProductInfoDto;
import com.zufar.icedlatte.openapi.dto.ProductListWithPaginationInfoDto;
import com.zufar.icedlatte.product.entity.ProductInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductInfoDtoConverter {

    @Named("toProductInfoDto")
    @Mapping(target = "id", source = "productId")
    @Mapping(target = "averageRating", source = "averageRating", qualifiedByName = "roundAverageRatingValue")
    ProductInfoDto toDto(final ProductInfo entity);

    @Mapping(target = "products", source = "content")
    @Mapping(target = "page", source = "number")
    @Mapping(target = "size", source = "size")
    ProductListWithPaginationInfoDto toProductPaginationDto(final Page<ProductInfoDto> pageProductResponseDto);

    @Named("roundAverageRatingValue")
    default BigDecimal roundAverageRatingValue(BigDecimal averageRating) {
        if (averageRating != null) {
            averageRating = averageRating.setScale(1, RoundingMode.HALF_DOWN);
        }
        return averageRating;
    }
}
