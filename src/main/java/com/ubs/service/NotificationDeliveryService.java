package com.ubs.service;

import com.ubs.entity.Notification;
import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationStatus;
import com.ubs.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationDeliveryService {

	private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

	private final NotificationRepository notificationRepository;
	private final EmailService emailService;

	public NotificationDeliveryService(NotificationRepository notificationRepository,
									   EmailService emailService) {
		this.notificationRepository = notificationRepository;
		this.emailService = emailService;
	}

	@Transactional
	public void deliver(Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId).orElse(null);
		if (notification == null || notification.getStatus() != NotificationStatus.PENDING) {
			return;
		}
		deliverNotification(notification);
	}

	@Transactional
	public void deliverNotification(Notification notification) {
		if (notification.getStatus() != NotificationStatus.PENDING) {
			return;
		}

		try {
			switch (notification.getChannel()) {
				case EMAIL -> sendEmail(notification);
				case IN_APP -> markSent(notification);
				case SMS -> markFailed(notification, "SMS delivery is not configured");
			}
		} catch (Exception ex) {
			log.error("Failed to deliver notification {}", notification.getId(), ex);
			markFailed(notification, truncate(ex.getMessage()));
		}
	}

	private void sendEmail(Notification notification) {
		String recipient = notification.getCustomer().getEmail();
		String body = """
				Dear %s,

				%s

				—
				Utility Billing System
				""".formatted(notification.getCustomer().getFullNames(), notification.getMessage());

		emailService.sendPlainText(recipient, notification.getTitle(), body);
		markSent(notification);
	}

	private void markSent(Notification notification) {
		notification.setStatus(NotificationStatus.SENT);
		notification.setSentAt(Instant.now());
		notification.setFailureReason(null);
		notificationRepository.save(notification);
	}

	private void markFailed(Notification notification, String reason) {
		notification.setStatus(NotificationStatus.FAILED);
		notification.setFailureReason(reason);
		notification.setSentAt(null);
		notificationRepository.save(notification);
	}

	private String truncate(String message) {
		if (message == null) {
			return "Delivery failed";
		}
		return message.length() > 500 ? message.substring(0, 500) : message;
	}

}
