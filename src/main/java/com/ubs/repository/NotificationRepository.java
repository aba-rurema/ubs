package com.ubs.repository;

import com.ubs.entity.Notification;
import com.ubs.entity.NotificationChannel;
import com.ubs.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByCustomerId(Long customerId, Pageable pageable);

	Page<Notification> findByCustomerIdAndStatus(Long customerId, NotificationStatus status, Pageable pageable);

	Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

	Page<Notification> findByStatusAndChannel(NotificationStatus status,
											  NotificationChannel channel,
											  Pageable pageable);

	long countByCustomerIdAndStatus(Long customerId, NotificationStatus status);

}
