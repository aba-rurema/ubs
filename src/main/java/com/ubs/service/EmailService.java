package com.ubs.service;

import com.ubs.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final MailProperties mailProperties;
	private final JavaMailSender mailSender;

	public EmailService(MailProperties mailProperties,
						@Autowired(required = false) JavaMailSender mailSender) {
		this.mailProperties = mailProperties;
		this.mailSender = mailSender;
	}

	public void sendPlainText(String to, String subject, String body) {
		if (!mailProperties.enabled()) {
			log.info("[MAIL DISABLED] To: {} | Subject: {} | Body: {}", to, subject, body);
			return;
		}

		if (mailSender == null) {
			throw new IllegalStateException(
					"Email is enabled but JavaMailSender is not configured. Set spring.mail.* properties.");
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
			helper.setFrom(mailProperties.from(), mailProperties.fromName());
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, false);
			mailSender.send(message);
			log.info("Email sent to {}", to);
		} catch (MessagingException | java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException("Failed to send email to " + to, ex);
		}
	}

	public void sendHtml(String to, String subject, String htmlBody) {
		if (!mailProperties.enabled()) {
			log.info("[MAIL DISABLED] To: {} | Subject: {} | HTML: {}", to, subject, htmlBody);
			return;
		}

		if (mailSender == null) {
			throw new IllegalStateException(
					"Email is enabled but JavaMailSender is not configured. Set spring.mail.* properties.");
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
			helper.setFrom(mailProperties.from(), mailProperties.fromName());
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);
			mailSender.send(message);
			log.info("Email sent to {}", to);
		} catch (MessagingException | java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException("Failed to send email to " + to, ex);
		}
	}

}
