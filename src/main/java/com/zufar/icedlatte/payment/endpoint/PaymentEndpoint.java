package com.zufar.icedlatte.payment.endpoint;

import com.stripe.exception.StripeException;
import com.zufar.icedlatte.openapi.dto.CreatePaymentRequest;
import com.zufar.icedlatte.openapi.dto.CreateCardDetailsTokenRequest;
import com.zufar.icedlatte.openapi.dto.ProcessedPaymentDetailsDto;
import com.zufar.icedlatte.openapi.dto.ProcessedPaymentWithClientSecretDto;
import com.zufar.icedlatte.payment.api.customer.CardDetailsProcessor;
import com.zufar.icedlatte.payment.api.event.PaymentEventProcessor;
import com.zufar.icedlatte.payment.api.intent.PaymentProcessor;
import com.zufar.icedlatte.payment.api.intent.PaymentRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(PaymentEndpoint.PAYMENT_URL)
public class PaymentEndpoint implements com.zufar.icedlatte.openapi.payment.api.PaymentApi {

    public static final String PAYMENT_URL = "/api/v1/payment";

    private final PaymentRetriever paymentRetriever;
    private final PaymentProcessor paymentProcessor;
    private final PaymentEventProcessor paymentEventProcessor;
    private final CardDetailsProcessor cardDetailsProcessor;

    @Override
    @PostMapping
    public ResponseEntity<ProcessedPaymentWithClientSecretDto> processPayment(@RequestBody CreatePaymentRequest paymentRequest) {
        ProcessedPaymentWithClientSecretDto processedPayment = paymentProcessor.processPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processedPayment);
    }

    @Override
    @GetMapping("/{paymentId}")
    public ResponseEntity<ProcessedPaymentDetailsDto> getPaymentDetails(@PathVariable final Long paymentId) {
        ProcessedPaymentDetailsDto retrievedPayment = paymentRetriever.getPaymentDetails(paymentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(retrievedPayment);
    }

    @GetMapping
    public ResponseEntity<String> processPaymentTest() { // FIXME: pass cart ID ? and/or JSON
        log.info("Got request for processPaymentTest");
        // TODO: create order
        Stripe.apiKey = "sk_test_51PJxciHA4AopuQMMeXaJNETc7RUAITeMTKJei07L8iEHrRiWLQalKsr756dnOzmKPUXufkUVNUSaiPyktJG9dGY500x0cM817f"; // FIXME: get this from env variables
        String DOMAIN = "http://localhost:8083" + PAYMENT_URL; // FIXME: get this from env variables
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(DOMAIN + "/stripe/callback?success=true") // FIXME: extract to constant
                        .setCancelUrl(DOMAIN + "/stripe/callback?success=false") // FIXME: extract to constant
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L) // FIXME: extract this from DB entity
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("USD")
                                                        // convert to cents
                                                        .setUnitAmount((long) (4.99 * 100)) // FIXME: extract this from DB entity
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Espresso") // FIXME: extract this from DB entity
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();
        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e); // FIXME: create specific exception
        }
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .header("Location", session.getUrl()).build();
    }

    @GetMapping("/stripe/callback")
    public ResponseEntity<Void> handleStripePaymentResponse(@RequestParam Boolean success) {
        log.info("Received callback for Stripe payment, transaction {}", success ? "succeeded" : "failed");
        // TODO: add entry to DB
        return new ResponseEntity<>(HttpStatus.OK); // FIXME: return JSON with some info?
    }

    @Override
    @PostMapping("/event")
    public ResponseEntity<Void> paymentEventProcess(@RequestBody final String paymentIntentPayload,
                                                    @RequestHeader("Stripe-Signature") final String stripeSignatureHeader) {
        paymentEventProcessor.processPaymentEvent(paymentIntentPayload, stripeSignatureHeader);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PostMapping("/card")
    public ResponseEntity<String> processCardDetailsToken(@RequestBody final CreateCardDetailsTokenRequest createCardDetailsTokenRequest) {
        String cardDetailsTokenId = cardDetailsProcessor.processCardDetails(createCardDetailsTokenRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardDetailsTokenId);
    }
}