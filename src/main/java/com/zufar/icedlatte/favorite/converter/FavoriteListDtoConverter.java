package com.zufar.icedlatte.favorite.converter;

import com.zufar.icedlatte.favorite.dto.FavoriteItemDto;
import com.zufar.icedlatte.favorite.dto.FavoriteListDto;
import com.zufar.icedlatte.favorite.entity.FavoriteItemEntity;
import com.zufar.icedlatte.favorite.entity.FavoriteListEntity;
import com.zufar.icedlatte.openapi.dto.ProductInfoDto;
import com.zufar.icedlatte.product.entity.ProductInfo;
import org.mapstruct.*;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = FavoriteItemDtoConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.FIELD)
public interface FavoriteListDtoConverter {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", expression = "java(favoriteListEntity.getUser() != null ? favoriteListEntity.getUser().getId() : null)")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "favoriteItems", source = "favoriteItems", qualifiedByName = "mapFavoriteItems")
    FavoriteListDto toDto(final FavoriteListEntity favoriteListEntity);

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "averageRating", source = "averageRating")
    @Mapping(target = "reviewsCount", source = "reviewsCount")
    @Mapping(target = "brandName", source = "brandName")
    @Mapping(target = "sellerName", source = "sellerName")
    ProductInfoDto convertProductInfoDto(ProductInfo productInfo);

    @Named("mapFavoriteItems")
    default FavoriteItemDto toFavoriteItemDto(FavoriteItemEntity itemEntity) {
        UUID favoriteItemEntityId = itemEntity.getId();
        ProductInfo productInfo = itemEntity.getProductInfo();

        ProductInfoDto productInfoDto = convertProductInfoDto(productInfo);

        return new FavoriteItemDto(favoriteItemEntityId, productInfoDto);
    }
}