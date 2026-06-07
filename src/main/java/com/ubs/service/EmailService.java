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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private static final List<String> DEFAULT_DEV_REDIRECT_DOMAINS =
			List.of("ubs.local", "example.com", "localhost", "test");

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

		DeliveryTarget delivery = resolveDeliveryTarget(to, subject, body, false);
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
			helper.setFrom(mailProperties.from(), mailProperties.fromName());
			helper.setTo(delivery.recipient());
			helper.setSubject(delivery.subject());
			helper.setText(delivery.body(), false);
			mailSender.send(message);
			if (delivery.redirected()) {
				log.info("Email sent to {} (dev redirect from {})", delivery.recipient(), to);
			} else {
				log.info("Email sent to {}", to);
			}
		} catch (MessagingException | java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException("Failed to send email to " + delivery.recipient(), ex);
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

		DeliveryTarget delivery = resolveDeliveryTarget(to, subject, htmlBody, true);
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
			helper.setFrom(mailProperties.from(), mailProperties.fromName());
			helper.setTo(delivery.recipient());
			helper.setSubject(delivery.subject());
			helper.setText(delivery.body(), true);
			mailSender.send(message);
			if (delivery.redirected()) {
				log.info("Email sent to {} (dev redirect from {})", delivery.recipient(), to);
			} else {
				log.info("Email sent to {}", to);
			}
		} catch (MessagingException | java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException("Failed to send email to " + delivery.recipient(), ex);
		}
	}

	private DeliveryTarget resolveDeliveryTarget(String intendedTo, String subject, String body, boolean html) {
		String redirectTo = mailProperties.devRedirectTo();
		if (!StringUtils.hasText(redirectTo) || !shouldRedirect(intendedTo, redirectTo.trim())) {
			return new DeliveryTarget(intendedTo, subject, body, false);
		}

		String redirectPrefix = html
				? "<p><em>[Dev redirect] Originally for: " + intendedTo + "</em></p>"
				: "[Dev redirect] Originally for: " + intendedTo + "\n\n";
		return new DeliveryTarget(
				redirectTo.trim(),
				"[Dev: " + intendedTo + "] " + subject,
				redirectPrefix + body,
				true
		);
	}

	private boolean shouldRedirect(String intendedTo, String redirectTo) {
		if (intendedTo.equalsIgnoreCase(redirectTo)) {
			return false;
		}
		String domain = extractDomain(intendedTo);
		if (domain == null) {
			return true;
		}
		List<String> redirectDomains = mailProperties.devRedirectDomains();
		if (redirectDomains == null || redirectDomains.isEmpty()) {
			redirectDomains = DEFAULT_DEV_REDIRECT_DOMAINS;
		}
		return redirectDomains.stream()
				.anyMatch(domainName -> domainName.equalsIgnoreCase(domain));
	}

	private String extractDomain(String email) {
		if (!StringUtils.hasText(email)) {
			return null;
		}
		int atIndex = email.lastIndexOf('@');
		if (atIndex < 0 || atIndex == email.length() - 1) {
			return null;
		}
		return email.substring(atIndex + 1).trim().toLowerCase(Locale.ROOT);
	}

	private record DeliveryTarget(String recipient, String subject, String body, boolean redirected) {
	}

}
