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
            var quantity = item.getProductQuantity();
            // convert to cents
            var unitAmount = item.getProductInfo().getPrice().multiply(BigDecimal.valueOf(100)).longValue();
            var productName = item.getProductInfo().getName();
            var productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(productName)
                    .build();
            var priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setUnitAmount(unitAmount)
                    .setCurrency("USD")
                    .setProductData(productData)
                    .build();
            var lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity((long) quantity)
                    .setPriceData(priceData)
                    .build();
            result.add(lineItem);
        }
        return result;
    }
}
