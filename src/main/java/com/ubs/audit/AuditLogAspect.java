package com.ubs.audit;

import com.ubs.entity.AuditAction;
import com.ubs.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditLogAspect {

	private final AuditLogService auditLogService;

	public AuditLogAspect(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
	public void logAction(JoinPoint joinPoint, Auditable auditable, Object result) {
		Long entityId = extractEntityId(joinPoint, result);
		String details = buildDetails(joinPoint, result);
		auditLogService.log(
				auditable.action(),
				auditable.entityType(),
				entityId,
				details
		);
	}

	private Long extractEntityId(JoinPoint joinPoint, Object result) {
		for (Object arg : joinPoint.getArgs()) {
			if (arg instanceof Long id) {
				return id;
			}
		}

		Object body = unwrapBody(result);
		if (body != null) {
			Long fromBody = extractIdFromObject(body);
			if (fromBody != null) {
				return fromBody;
			}
		}
		return null;
	}

	private Object unwrapBody(Object result) {
		if (result instanceof ResponseEntity<?> responseEntity) {
			return responseEntity.getBody();
		}
		return result;
	}

	private Long extractIdFromObject(Object body) {
		try {
			Method idMethod = body.getClass().getMethod("id");
			Object idValue = idMethod.invoke(body);
			if (idValue instanceof Long id) {
				return id;
			}
		} catch (ReflectiveOperationException ignored) {
			// no id() on response type
		}
		return null;
	}

	private String buildDetails(JoinPoint joinPoint, Object result) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String method = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
		Object body = unwrapBody(result);

		if (body == null) {
			return method + " completed";
		}
		return method + " -> " + body.getClass().getSimpleName();
	}

}
