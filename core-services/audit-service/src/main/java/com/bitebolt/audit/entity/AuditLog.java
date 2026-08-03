package com.bitebolt.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * JPA Entity representing a persisted audit log record in the database.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Mapping:</strong> Maps to the {@code audit_logs} table in the PostgreSQL database.</li>
 *   <li><strong>Instantiation:</strong> Built by the {@link AuditKafkaListener} from an incoming Kafka event.</li>
 *   <li><strong>Auditing:</strong> Automatically populates the {@code createdAt} timestamp via Spring Data JPA's {@link AuditingEntityListener}.</li>
 * </ol>
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "actor_ip")
    private String actorIp;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "status")
    private String status;

    @Column(name = "service")
    private String service;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
