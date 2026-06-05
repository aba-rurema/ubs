package com.ubs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
		@Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
		@Index(name = "idx_audit_logs_action", columnList = "action"),
		@Index(name = "idx_audit_logs_entity", columnList = "entity_type,entity_id"),
		@Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(length = 50)
	private String username;

	@Column(length = 100)
	private String role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AuditAction action;

	@Column(name = "entity_type", length = 50)
	private String entityType;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(length = 1000)
	private String details;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

}
