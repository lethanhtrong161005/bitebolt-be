package com.bitebolt.audit.service.impl;

import com.bitebolt.audit.document.AuditLogDocument;
import com.bitebolt.audit.dto.request.AuditLogSearchRequest;
import com.bitebolt.audit.dto.request.AuditLogSortCondition;
import com.bitebolt.audit.dto.response.AuditLogResponse;
import com.bitebolt.audit.entity.AuditLog;
import com.bitebolt.audit.helper.AuditLogHelper;
import com.bitebolt.audit.repository.AuditLogRepository;
import com.bitebolt.audit.service.AuditQueryService;
import com.bitebolt.common.dto.request.PayloadSearchRequest;
import com.bitebolt.common.dto.response.PageResponse;
import com.bitebolt.common.logging.audit.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation executing audit log queries via Elasticsearch Full-Text search
 * (covering actorId, actorEmail, actorName) with graceful fallback to PostgreSQL JPA Specifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditQueryServiceImpl implements AuditQueryService {
  private final AuditLogRepository auditLogRepository;
  private final AuditLogHelper auditLogHelper;
  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  @Transactional(readOnly = true)
  public PageResponse<AuditLogResponse> searchAuditLogs(
      PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest> request) {
    log.info("[AuditQuery] Processing payload search request for audit logs");

    // 1. Try querying Elasticsearch first (Full-Text search on traceId, actorId, actorEmail, actorName)
    try {
      CriteriaQuery query = auditLogHelper.toCriteriaQuery(request);
      SearchHits<AuditLogDocument> hits = elasticsearchOperations.search(query, AuditLogDocument.class);

      if (hits != null && hits.getTotalHits() > 0) {
        List<AuditLogResponse> content =
            hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(auditLogHelper::toResponse)
                .collect(Collectors.toList());

        Pageable pageable = auditLogHelper.toPageable(request);
        Page<AuditLogResponse> page = new PageImpl<>(content, pageable, hits.getTotalHits());

        log.info("[AuditQuery] Elasticsearch search returned {} results", hits.getTotalHits());
        return PageResponse.of(page, res -> res);
      }
    } catch (Exception e) {
      log.warn("[AuditQuery] Elasticsearch search failed or empty, falling back to PostgreSQL: {}", e.getMessage());
    }

    // 2. Fallback to PostgreSQL JPA Specification
    Specification<AuditLog> spec = auditLogHelper.toSpecification(request);
    Pageable pageable = auditLogHelper.toPageable(request);

    Page<AuditLog> auditLogs = auditLogRepository.findAll(spec, pageable);

    return auditLogHelper.toPageResponse(auditLogs);
  }

  @Override
  public List<String> getAvailableActions() {
    return Arrays.stream(AuditAction.values())
        .map(Enum::name)
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getAvailableStatuses() {
    return Arrays.asList("SUCCESS", "FAILURE");
  }
}
