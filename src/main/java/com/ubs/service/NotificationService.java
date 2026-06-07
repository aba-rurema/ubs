package com.ubs.service;

import com.ubs.dto.notification.NotificationCreateRequest;
import com.ubs.dto.notification.NotificationFailureRequest;
import com.ubs.dto.notification.NotificationResponse;
import com.ubs.entity.Customer;
import com.ubs.entity.Notification;
import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationStatus;
import com.ubs.entity.NotificationType;
import com.ubs.exception.BusinessRuleViolationException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.CustomerRepository;
import com.ubs.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;
	private final CustomerRepository customerRepository;
	private final NotificationDeliveryService notificationDeliveryService;

	public NotificationService(NotificationRepository notificationRepository,
							   CustomerRepository customerRepository,
							   NotificationDeliveryService notificationDeliveryService) {
		this.notificationRepository = notificationRepository;
		this.customerRepository = customerRepository;
		this.notificationDeliveryService = notificationDeliveryService;
	}

	@Transactional
	public NotificationResponse create(NotificationCreateRequest request) {
		Customer customer = findCustomerOrThrow(request.customerId());

		Notification notification = Notification.builder()
				.customer(customer)
				.title(request.title().trim())
				.message(request.message().trim())
				.notificationType(request.notificationType())
				.channel(request.channel())
				.status(NotificationStatus.PENDING)
				.build();

		return deliverAfterSave(notificationRepository.save(notification));
	}

	@Transactional
	public void createSystemNotification(Customer customer,
										 String title,
										 String message,
										 NotificationType notificationType) {
		Instant now = Instant.now();
		Notification notification = Notification.builder()
				.customer(customer)
				.title(title.trim())
				.message(message.trim())
				.notificationType(notificationType)
				.channel(NotificationChannel.IN_APP)
				.status(NotificationStatus.SENT)
				.sentAt(now)
				.build();

		notificationRepository.save(notification);
		sendEmailCopy(notification);
	}

	private NotificationResponse deliverAfterSave(Notification notification) {
		if (notification.getChannel() == NotificationChannel.EMAIL) {
			notificationDeliveryService.deliver(notification.getId());
			notification = notificationRepository.findById(notification.getId()).orElse(notification);
			return toResponse(notification);
		}

		if (notification.getChannel() == NotificationChannel.IN_APP) {
			notification.setStatus(NotificationStatus.SENT);
			notification.setSentAt(Instant.now());
			notification = notificationRepository.save(notification);
			sendEmailCopy(notification);
		}

		return toResponse(notification);
	}

	private void sendEmailCopy(Notification notification) {
		try {
			notificationDeliveryService.sendEmailForNotification(notification);
		} catch (Exception ex) {
			log.warn("Saved notification {} but email delivery failed: {}", notification.getId(), ex.getMessage());
		}
	}

	@Transactional
	public NotificationResponse markAsSent(Long id) {
		Notification notification = findNotificationOrThrow(id);
		validateStatusTransition(notification, NotificationStatus.PENDING);

		notification.setStatus(NotificationStatus.SENT);
		notification.setSentAt(Instant.now());
		notification.setFailureReason(null);

		return toResponse(notificationRepository.save(notification));
	}

	@Transactional
	public NotificationResponse markAsFailed(Long id, NotificationFailureRequest request) {
		Notification notification = findNotificationOrThrow(id);
		validateStatusTransition(notification, NotificationStatus.PENDING);

		notification.setStatus(NotificationStatus.FAILED);
		notification.setFailureReason(request.failureReason().trim());
		notification.setSentAt(null);

		return toResponse(notificationRepository.save(notification));
	}

	@Transactional
	public NotificationResponse markAsRead(Long id) {
		Notification notification = findNotificationOrThrow(id);

		if (notification.getStatus() != NotificationStatus.SENT) {
			throw new BusinessRuleViolationException(
					"Only sent notifications can be marked as read. Current status: " + notification.getStatus());
		}

		notification.setStatus(NotificationStatus.READ);
		notification.setReadAt(Instant.now());

		return toResponse(notificationRepository.save(notification));
	}

	@Transactional(readOnly = true)
	public NotificationResponse getById(Long id) {
		return toResponse(findNotificationOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getAll(Pageable pageable) {
		return notificationRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getByCustomerId(Long customerId, Pageable pageable) {
		findCustomerOrThrow(customerId);
		return notificationRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getByCustomerIdAndStatus(Long customerId,
															   NotificationStatus status,
															   Pageable pageable) {
		findCustomerOrThrow(customerId);
		return notificationRepository.findByCustomerIdAndStatus(customerId, status, pageable)
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getByStatus(NotificationStatus status, Pageable pageable) {
		return notificationRepository.findByStatus(status, pageable).map(this::toResponse);
	}

	@Transactional
	public void delete(Long id) {
		Notification notification = findNotificationOrThrow(id);
		notificationRepository.delete(notification);
	}

	private void validateStatusTransition(Notification notification, NotificationStatus expectedStatus) {
		if (notification.getStatus() != expectedStatus) {
			throw new BusinessRuleViolationException(
					"Notification must be in " + expectedStatus + " status. Current status: "
							+ notification.getStatus());
		}
	}

	private Customer findCustomerOrThrow(Long customerId) {
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
	}

	private Notification findNotificationOrThrow(Long id) {
		return notificationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
	}

	private NotificationResponse toResponse(Notification notification) {
		Customer customer = notification.getCustomer();

		return new NotificationResponse(
				notification.getId(),
				customer.getId(),
				customer.getFullNames(),
				customer.getEmail(),
				notification.getTitle(),
				notification.getMessage(),
				notification.getNotificationType(),
				notification.getChannel(),
				notification.getStatus(),
				notification.getSentAt(),
				notification.getReadAt(),
				notification.getFailureReason(),
				notification.getCreatedAt(),
				notification.getUpdatedAt()
		);
	}

}
