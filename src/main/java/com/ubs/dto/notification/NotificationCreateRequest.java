package com.ubs.dto.notification;

import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record NotificationCreateRequest(
		@NotNull(message = "Customer ID is required")
		Long customerId,

		@NotBlank(message = "Title is required")
		@Size(max = 150, message = "Title must not exceed 150 characters")
		String title,

		@NotBlank(message = "Message is required")
		@Size(max = 1000, message = "Message must not exceed 1000 characters")
		String message,

		@NotNull(message = "Notification type is required")
		NotificationType notificationType,

		@NotNull(message = "Channel is required")
		NotificationChannel channel
) {
}
