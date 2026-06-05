package com.ubs.dto.notification;

import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationStatus;
import com.ubs.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
		Long id,
		Long customerId,
		String customerFullNames,
		String customerEmail,
		String title,
		String message,
		NotificationType notificationType,
		NotificationChannel channel,
		NotificationStatus status,
		Instant sentAt,
		Instant readAt,
		String failureReason,
		Instant createdAt,
		Instant updatedAt
) {
}
