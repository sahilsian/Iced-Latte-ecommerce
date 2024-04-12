package com.zufar.icedlatte.payment.endpoint;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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