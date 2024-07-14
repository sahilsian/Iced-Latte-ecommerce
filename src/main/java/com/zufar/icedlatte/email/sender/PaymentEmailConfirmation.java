package com.zufar.icedlatte.email.sender;

import com.stripe.model.PaymentIntent;
import com.zufar.icedlatte.email.message.EmailConfirmMessage;
import com.zufar.icedlatte.email.message.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentEmailConfirmation extends AbstractEmailSender<EmailConfirmMessage> {

    private static final String DEFAULT_SUCCESSFUL_MESSAGE_TEMPLATE = "You're payment with total amount - %d %s was successfully processed";
    private static final String DEFAULT_SUBJECT = "Payment Confirmation for Your Recent Purchase";

    @Autowired
    public PaymentEmailConfirmation(JavaMailSender javaMailSender,
                                    SimpleMailMessage mailMessage,
                                    List<MessageBuilder<EmailConfirmMessage>> messageBuilders) {
        super(javaMailSender, mailMessage, messageBuilders);
    }

    public void send(PaymentIntent paymentIntent) {
        String recipientEmail = paymentIntent.getReceiptEmail();
        String message = formatSuccessfulMessage(paymentIntent.getAmount(), paymentIntent.getCurrency());
        String subject = getDefaultSubject();

        sendNotification(recipientEmail, message, subject);
    }

    private String formatSuccessfulMessage(long amount, String currency) {
        return DEFAULT_SUCCESSFUL_MESSAGE_TEMPLATE.formatted(amount, currency);
    }

    private String getDefaultSubject() {
        return DEFAULT_SUBJECT;
    }
}
