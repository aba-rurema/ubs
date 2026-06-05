package com.ubs.scheduler;

import com.ubs.entity.Notification;
import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationStatus;
import com.ubs.repository.NotificationRepository;
import com.ubs.service.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class NotificationDeliveryScheduler {

	private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryScheduler.class);

	private final NotificationRepository notificationRepository;
	private final NotificationDeliveryService notificationDeliveryService;

	public NotificationDeliveryScheduler(NotificationRepository notificationRepository,
										   NotificationDeliveryService notificationDeliveryService) {
		this.notificationRepository = notificationRepository;
		this.notificationDeliveryService = notificationDeliveryService;
	}

	@Scheduled(fixedDelayString = "${app.mail.delivery-poll-ms:60000}")
	@Transactional
	public void deliverPendingNotifications() {
		List<Notification> pending = notificationRepository
				.findByStatusAndChannel(NotificationStatus.PENDING, NotificationChannel.EMAIL, PageRequest.of(0, 50))
				.getContent();

		if (pending.isEmpty()) {
			return;
		}

		log.debug("Delivering {} pending email notification(s)", pending.size());
		for (Notification notification : pending) {
			notificationDeliveryService.deliverNotification(notification);
		}
	}

}
