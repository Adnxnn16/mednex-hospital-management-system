package com.mednex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @InjectMocks EmailService emailService;

    @Test
    void sendAppointmentConfirmation_sendsEmail() {
        String to = "test@example.com";
        String subject = "Confirmed";
        String body = "See you soon.";

        emailService.sendAppointmentConfirmation(to, subject, body);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentConfirmation_setsCorrectRecipient() {
        String to = "patient@mednex.com";
        String subject = "Appointment Confirmation";
        String body = "Your appointment is confirmed.";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        
        emailService.sendAppointmentConfirmation(to, subject, body);

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getTo()).containsExactly(to);
        assertThat(msg.getSubject()).isEqualTo(subject);
        assertThat(msg.getText()).isEqualTo(body);
    }

    @Test
    void sendAppointmentConfirmation_skips_when_sender_null() {
        // Test the constructor that skips when sender is null
        EmailService serviceNoMail = new EmailService(null);
        serviceNoMail.sendAppointmentConfirmation("to@test.com", "sub", "body");
        // No Exception should occur, just log skip
        verifyNoInteractions(mailSender);
    }
}
