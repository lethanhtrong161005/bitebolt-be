package com.bitebolt.audit.repository;

import com.bitebolt.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for persisting and querying {@link AuditLog} entities.
 */
@Repository
public interface AuditLogRepository
    extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {}
