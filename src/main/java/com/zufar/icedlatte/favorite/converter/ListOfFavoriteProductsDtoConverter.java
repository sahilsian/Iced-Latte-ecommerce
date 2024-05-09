package com.zufar.icedlatte.favorite.converter;

import com.zufar.icedlatte.favorite.dto.FavoriteItemDto;
import com.zufar.icedlatte.favorite.dto.FavoriteListDto;
import com.zufar.icedlatte.openapi.dto.ListOfFavoriteProductsDto;
import com.zufar.icedlatte.openapi.dto.ProductInfoDto;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.FIELD)
public interface ListOfFavoriteProductsDtoConverter {

    @Mapping(target = "products", source = "favoriteItems", qualifiedByName = "toListProductInfoDto")
    ListOfFavoriteProductsDto toListProductDto(FavoriteListDto favoriteList);

    @Named("toListProductInfoDto")
    default List<ProductInfoDto> toProductInfoDto(final Set<FavoriteItemDto> favoriteItems) {
        return favoriteItems.stream()
                .map(FavoriteItemDto::productInfo)
                .toList();
    }
}