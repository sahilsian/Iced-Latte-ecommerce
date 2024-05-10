package com.zufar.icedlatte.favorite.converter;

import com.zufar.icedlatte.favorite.dto.FavoriteItemDto;
import com.zufar.icedlatte.favorite.dto.FavoriteListDto;
import com.zufar.icedlatte.favorite.entity.FavoriteItemEntity;
import com.zufar.icedlatte.favorite.entity.FavoriteListEntity;
import com.zufar.icedlatte.openapi.dto.ProductInfoDto;
import com.zufar.icedlatte.product.entity.ProductInfo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = FavoriteItemDtoConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.FIELD)
public interface FavoriteListDtoConverter {

    default FavoriteListDto toDto(final FavoriteListEntity favoriteListEntity) {
        UUID id = favoriteListEntity.getId();
        UUID userId = favoriteListEntity.getUserId();
        Set<FavoriteItemDto> favoriteItemsDto = new HashSet<>();

        for (FavoriteItemEntity itemEntity : favoriteListEntity.getFavoriteItems()) {
            UUID favoriteItemEntityId = itemEntity.getId();
            ProductInfo productInfo = itemEntity.getProductInfo();

            ProductInfoDto productInfoDto = convertProductInfoDto(productInfo);

            FavoriteItemDto favoriteItemDto = new FavoriteItemDto(favoriteItemEntityId, productInfoDto);
            favoriteItemsDto.add(favoriteItemDto);
        }

        return new FavoriteListDto(id, userId, favoriteItemsDto, favoriteListEntity.getUpdatedAt());
    }

    private static ProductInfoDto convertProductInfoDto(ProductInfo productInfo) {
        return new ProductInfoDto(
                productInfo.getProductId(),
                productInfo.getName(),
                productInfo.getDescription(),
                productInfo.getPrice(),
                productInfo.getQuantity(),
                productInfo.getActive(),
                productInfo.getAverageRating(),
                productInfo.getReviewsCount(),
                productInfo.getBrandName(),
                productInfo.getSellerName()

        );
    }
}