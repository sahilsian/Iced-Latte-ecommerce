package com.zufar.icedlatte.payment.api;

import com.stripe.param.checkout.SessionCreateParams;
import com.zufar.icedlatte.openapi.dto.ShoppingCartDto;
import com.zufar.icedlatte.openapi.dto.ShoppingCartItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StripeLineItemsConverter {

    // FIXME: use JPA entities instead of converted DTO
    public List<SessionCreateParams.LineItem> getLineItems(ShoppingCartDto shoppingCart) {
        var result = new ArrayList<SessionCreateParams.LineItem>();
        for (ShoppingCartItemDto item : shoppingCart.getItems()) {
            result.add(convertToLineItem(item));
        }
        return result;
    }

    private SessionCreateParams.LineItem convertToLineItem(ShoppingCartItemDto item) {
        var quantity = item.getProductQuantity();
        var unitAmount = convertToCents(item.getProductInfo().getPrice());
        var productName = item.getProductInfo().getName();
        var priceData = createPriceData(unitAmount, productName);
        return buildLineItem(quantity, priceData);
    }

    private long convertToCents(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(long unitAmount, String productName) {
        var productData = createProductData(productName);
        return SessionCreateParams.LineItem.PriceData.builder()
                .setUnitAmount(unitAmount)
                .setCurrency("USD")
                .setProductData(productData)
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(String productName) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(productName)
                .build();
    }

    private SessionCreateParams.LineItem buildLineItem(int quantity, SessionCreateParams.LineItem.PriceData priceData) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) quantity)
                .setPriceData(priceData)
                .build();
    }
}
