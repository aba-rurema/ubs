package com.ubs.repository;

import com.ubs.entity.AuditAction;
import com.ubs.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	@Query("""
			SELECT a FROM AuditLog a
			WHERE (:userId IS NULL OR a.userId = :userId)
			  AND (:action IS NULL OR a.action = :action)
			  AND (:entityType IS NULL OR a.entityType = :entityType)
			  AND (:entityId IS NULL OR a.entityId = :entityId)
			  AND (:from IS NULL OR a.createdAt >= :from)
			  AND (:to IS NULL OR a.createdAt <= :to)
			""")
	Page<AuditLog> search(@Param("userId") Long userId,
						  @Param("action") AuditAction action,
						  @Param("entityType") String entityType,
						  @Param("entityId") Long entityId,
						  @Param("from") Instant from,
						  @Param("to") Instant to,
						  Pageable pageable);

}
