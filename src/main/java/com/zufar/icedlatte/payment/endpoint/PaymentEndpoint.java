package com.zufar.icedlatte.payment.endpoint;

import com.stripe.exception.StripeException;
import com.zufar.icedlatte.cart.api.ShoppingCartManager;
import com.zufar.icedlatte.openapi.dto.CreatePaymentRequest;
import com.zufar.icedlatte.openapi.dto.CreateCardDetailsTokenRequest;
import com.zufar.icedlatte.openapi.dto.ProcessedPaymentDetailsDto;
import com.zufar.icedlatte.openapi.dto.ProcessedPaymentWithClientSecretDto;
import com.zufar.icedlatte.payment.api.StripeLineItemsConverter;
import com.zufar.icedlatte.payment.api.StripeShippingOptionsProvider;
import com.zufar.icedlatte.payment.api.customer.CardDetailsProcessor;
import com.zufar.icedlatte.payment.api.event.PaymentEventProcessor;
import com.zufar.icedlatte.payment.api.intent.PaymentProcessor;
import com.zufar.icedlatte.payment.api.intent.PaymentRetriever;
import com.zufar.icedlatte.payment.dto.PaymentSession;
import com.zufar.icedlatte.payment.dto.PaymentSessionStatus;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import com.zufar.icedlatte.user.api.SingleUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final SecurityPrincipalProvider securityPrincipalProvider;
    private final ShoppingCartManager shoppingCartManager;
    private final SingleUserProvider singleUserProvider;
    private final StripeShippingOptionsProvider stripeShippingOptionsProvider;
    private final StripeLineItemsConverter stripeLineItemsConverter;

    @Value("${stripe.secret-key}")
    public String secretKey;

    // TEST CARD: 4242424242424242
    @GetMapping
    public ResponseEntity<PaymentSession> processPaymentTest() {
        var userId = securityPrincipalProvider.getUserId();
        log.info("Received request for processing payment for user {}", userId);
        // TODO: create order
        var user = singleUserProvider.getUserById(userId);
        var shoppingCart = shoppingCartManager.getShoppingCartByUserId(userId);
        log.info("For user {}", userId);
        Stripe.apiKey = secretKey;
        String DOMAIN = "http://localhost:80"; // FIXME: get this from request
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setCustomerEmail(user.getEmail())
                        .setReturnUrl(DOMAIN + "/orders?sessionId={CHECKOUT_SESSION_ID}")
                        .addAllLineItem(stripeLineItemsConverter.getLineItems(shoppingCart))
                        .addAllShippingOption(stripeShippingOptionsProvider.getShippingOptions())
                        .setAutomaticTax(
                                SessionCreateParams.AutomaticTax.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e); // FIXME: create specific exception
        } finally {
            // FIXME: add record to the DB
        }
        shoppingCartManager.deleteByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(new PaymentSession(session.getClientSecret()));
    }

    // TODO: update order status even if it wasn't explicitly requested https://docs.stripe.com/payments/checkout/fulfill-orders
    // TODO: handle scenario when payment is declined
    // TODO: handle scenario with 3D secure
    // TODO: allow saving card details
    @GetMapping("/stripe/session-status")
    public ResponseEntity<PaymentSessionStatus> handleStripePaymentResponse(@RequestParam String sessionID) {
        log.info("Received request for session status, session id {}", sessionID);
        Session session;
        try {
            session = Session.retrieve(sessionID);
        } catch (StripeException e) {
            throw new RuntimeException(e); // FIXME: create specific exception
        } finally {
            // FIXME: add record to the DB, include shipping option (is it possible to access it from session? <-- investigate)
        }
        var response = new PaymentSessionStatus(session.getStatus(), session.getCustomerEmail());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


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