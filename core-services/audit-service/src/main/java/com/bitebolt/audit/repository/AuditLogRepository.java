package com.bitebolt.audit.repository;

import com.bitebolt.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for persisting and querying {@link AuditLog} entities.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Integration:</strong> Automatically implemented by Spring Data JPA at runtime.</li>
 *   <li><strong>Persistence:</strong> Provides {@code save()} methods used by the Kafka listener to store events.</li>
 * </ol>
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
