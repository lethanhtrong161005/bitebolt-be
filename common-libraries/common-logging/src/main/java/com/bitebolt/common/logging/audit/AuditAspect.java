package com.bitebolt.common.logging.audit;

import com.bitebolt.common.constant.AppConstant;
import com.bitebolt.common.logging.constant.AuditConstant;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * Aspect-Oriented Programming (AOP) interceptor for methods annotated with {@link Auditable}.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Intercept:</strong> Wraps method execution matching the {@code @Auditable}
 *       pointcut.
 *   <li><strong>Execute:</strong> Allows the actual target method to proceed ({@code
 *       joinPoint.proceed()}).
 *   <li><strong>Evaluate Status:</strong> Sets status to {@code SUCCESS} if no exceptions are
 *       thrown, or {@code FAILURE} otherwise.
 *   <li><strong>Extract Context:</strong> Extracts trace and actor metadata from MDC and evaluates
 *       SpEL expressions for dynamic resource IDs.
 *   <li><strong>Publish:</strong> Constructs an {@link AuditEvent} and sends it to Kafka via {@link
 *       AuditKafkaPublisher} in a {@code finally} block.
 * </ol>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

  private final AuditKafkaPublisher publisher;
  private final ExpressionParser parser = new SpelExpressionParser();

  @Value("${spring.application.name:" + AuditConstant.UNKNOWN_SERVICE + "}")
  private String serviceName;

  @Around("@annotation(auditable)")
  public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
    Object result = null;
    String status = AuditConstant.STATUS_SUCCESS;
    String details = null;

    try {
      result = joinPoint.proceed();
      return result;
    } catch (Throwable t) {
      status = AuditConstant.STATUS_FAILURE;
      details = t.getMessage();
      throw t;
    } finally {
      String resourceId = resolveResourceId(joinPoint, auditable);

      AuditEvent event =
          AuditEvent.builder()
              .logType(AuditConstant.LOG_TYPE_AUDIT)
              .traceId(MDC.get(AppConstant.TRACE_ID_KEY))
              .actorId(MDC.get(AuditConstant.MDC_KEY_ACTOR_ID))
              .actorIp(MDC.get(AuditConstant.MDC_KEY_CLIENT_IP))
              .action(auditable.action().name())
              .resourceType(auditable.resourceType())
              .resourceId(resourceId)
              .status(status)
              .service(serviceName)
              .details(details)
              .timestamp(Instant.now())
              .build();

      publisher.publish(event);
    }
  }

  private String resolveResourceId(ProceedingJoinPoint joinPoint, Auditable auditable) {
    if (auditable.resourceIdParam().isEmpty()) {
      return null;
    }

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Object[] args = joinPoint.getArgs();
    String[] parameterNames = signature.getParameterNames();

    EvaluationContext context = new StandardEvaluationContext();
    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    try {
      Object value = parser.parseExpression(auditable.resourceIdParam()).getValue(context);
      return value != null ? value.toString() : null;
    } catch (Exception e) {
      return AuditConstant.UNKNOWN_EVAL_ERROR;
    }
  }
}
