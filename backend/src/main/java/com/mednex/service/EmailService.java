package com.mednex.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;

	public EmailService(@org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendAppointmentConfirmation(String to, String subject, String body) {
		if (mailSender == null) {
			log.info("No mail sender configured – email to {} skipped: {}", to, subject);
			return;
		}
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(to);
			msg.setSubject(subject);
			msg.setText(body);
			mailSender.send(msg);
		} catch (Exception e) {
			log.warn("Email dispatch failed: {}", e.getMessage());
		}
	}
}

