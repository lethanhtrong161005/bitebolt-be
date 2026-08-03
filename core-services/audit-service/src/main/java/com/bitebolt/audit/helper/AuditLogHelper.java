package com.bitebolt.audit.helper;

import com.bitebolt.audit.dto.request.AuditLogSearchRequest;
import com.bitebolt.audit.dto.request.AuditLogSortCondition;
import com.bitebolt.audit.dto.response.AuditLogResponse;
import com.bitebolt.audit.entity.AuditLog;
import com.bitebolt.common.dto.request.PageRequest;
import com.bitebolt.common.dto.request.PayloadSearchRequest;
import com.bitebolt.common.dto.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper component responsible for encapsulating specification construction, pagination setup, and entity-to-DTO conversion for audit logs.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Specification Assembly:</strong> Constructs dynamic JPA {@link Specification} based on {@link PayloadSearchRequest}.
 *   <li><strong>Pagination Configuration:</strong> Builds Spring Data {@link Pageable} from {@link PayloadSearchRequest}.
 *   <li><strong>Entity Mapping:</strong> Maps single {@link AuditLog} entity or {@link Page} into response DTOs.
 * </ol>
 */
@Component
public class AuditLogHelper {
  /**
   * Constructs dynamic JPA Specification criteria predicate from search request payload.
   *
   * @param request Search request payload container.
   * @return Composite JPA Specification predicate for AuditLog entity.
   */
  public Specification<AuditLog> toSpecification(
      PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest> request) {
    AuditLogSearchRequest searchReq = request != null ? request.getSearch() : null;
    if (searchReq == null) {
      return Specification.where(null);
    }

    Specification<AuditLog> domainSpec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          if (searchReq.getTraceId() != null && !searchReq.getTraceId().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("traceId"), searchReq.getTraceId().trim()));
          }

          if (searchReq.getActorId() != null && !searchReq.getActorId().trim().isEmpty()) {
            predicates.add(
                cb.like(
                    cb.lower(root.get("actorId")),
                    "%" + searchReq.getActorId().trim().toLowerCase() + "%"));
          }

          if (searchReq.getAction() != null && !searchReq.getAction().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("action"), searchReq.getAction().trim()));
          }

          if (searchReq.getStatus() != null && !searchReq.getStatus().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("status"), searchReq.getStatus().trim()));
          }

          if (searchReq.getStartDate() != null) {
            predicates.add(
                cb.greaterThanOrEqualTo(root.get("createdAt"), searchReq.getStartDate()));
          }

          if (searchReq.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), searchReq.getEndDate()));
          }

          if (predicates.isEmpty()) {
            return cb.conjunction();
          }

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return domainSpec;
  }

  /**
   * Builds Spring Data Pageable object from PageRequest and SortBaseCondition criteria.
   *
   * @param request Search request payload container.
   * @return Formatted Pageable instance.
   */
  public Pageable toPageable(
      PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest> request) {
    PageRequest pageReq =
        request != null && request.getPage() != null
            ? request.getPage()
            : PageRequest.builder().build();

    AuditLogSortCondition sortCond =
        request != null && request.getSort() != null
            ? request.getSort()
            : AuditLogSortCondition.builder().build();

    Sort.Direction sortDirection =
        "ASC".equalsIgnoreCase(sortCond.getSortOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;

    Set<String> allowedFields = sortCond.getAllowedFields();
    String requestedSort = sortCond.getSortBy();
    String sortField = "createdAt";

    if (requestedSort != null && !requestedSort.trim().isEmpty()) {
      String cleanSort = requestedSort.trim();
      if (allowedFields != null && allowedFields.contains(cleanSort)) {
        sortField = cleanSort;
      }
    }

    int pageZeroIndex = Math.max(0, pageReq.getPageNumber() - 1);
    int pageSizeLimit = Math.max(1, pageReq.getPageSize());

    return org.springframework.data.domain.PageRequest.of(
        pageZeroIndex, pageSizeLimit, Sort.by(sortDirection, sortField));
  }

  /**
   * Convert AuditLog entity record into AuditLogResponse DTO.
   *
   * @param entity AuditLog database entity.
   * @return Mapped response DTO.
   */
  public AuditLogResponse toResponse(AuditLog entity) {
    if (entity == null) {
      return null;
    }

    return AuditLogResponse.builder()
        .id(entity.getId())
        .traceId(entity.getTraceId())
        .actorId(entity.getActorId())
        .actorIp(entity.getActorIp())
        .action(entity.getAction())
        .resourceType(entity.getResourceType())
        .resourceId(entity.getResourceId())
        .status(entity.getStatus())
        .service(entity.getService())
        .details(entity.getDetails())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  /**
   * Convert AuditLogDocument Elasticsearch document into AuditLogResponse DTO.
   */
  public AuditLogResponse toResponse(com.bitebolt.audit.document.AuditLogDocument entity) {
    if (entity == null) {
      return null;
    }

    Long id = null;
    if (entity.getId() != null) {
      try {
        id = Long.parseLong(entity.getId());
      } catch (NumberFormatException ignored) {}
    }

    return AuditLogResponse.builder()
        .id(id)
        .traceId(entity.getTraceId())
        .actorId(entity.getActorId())
        .actorEmail(entity.getActorEmail())
        .actorName(entity.getActorName())
        .actorIp(entity.getActorIp())
        .action(entity.getAction())
        .resourceType(entity.getResourceType())
        .resourceId(entity.getResourceId())
        .status(entity.getStatus())
        .service(entity.getService())
        .details(entity.getDetails())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  /**
   * Convert Spring Data Page of AuditLog entities into standardized PageResponse envelope.
   *
   * @param page Spring Data Page instance containing AuditLog entities.
   * @return Formatted PageResponse wrapping AuditLogResponse DTOs.
   */
  public PageResponse<AuditLogResponse> toPageResponse(Page<AuditLog> page) {
    if (page == null) {
      return PageResponse.<AuditLogResponse>builder().build();
    }

    return PageResponse.of(page, this::toResponse);
  }

  /**
   * Constructs Elasticsearch CriteriaQuery for multi-field full-text search across traceId, actorId, actorEmail, actorName.
   */
  public org.springframework.data.elasticsearch.core.query.CriteriaQuery toCriteriaQuery(
      PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest> request) {
    AuditLogSearchRequest searchReq = request != null ? request.getSearch() : null;
    org.springframework.data.elasticsearch.core.query.Criteria criteria =
        new org.springframework.data.elasticsearch.core.query.Criteria();

    if (searchReq != null) {
      if (searchReq.getActorId() != null && !searchReq.getActorId().trim().isEmpty()) {
        criteria = criteria.and("actorId").contains(searchReq.getActorId().trim());
      }

      if (searchReq.getActorEmail() != null && !searchReq.getActorEmail().trim().isEmpty()) {
        criteria = criteria.and("actorEmail").matches(searchReq.getActorEmail().trim());
      }

      if (searchReq.getActorName() != null && !searchReq.getActorName().trim().isEmpty()) {
        criteria = criteria.and("actorName").matches(searchReq.getActorName().trim());
      }

      if (searchReq.getAction() != null && !searchReq.getAction().trim().isEmpty()) {
        criteria = criteria.and("action").is(searchReq.getAction().trim());
      }

      if (searchReq.getStatus() != null && !searchReq.getStatus().trim().isEmpty()) {
        criteria = criteria.and("status").is(searchReq.getStatus().trim());
      }

      if (searchReq.getStartDate() != null) {
        criteria = criteria.and("createdAt").greaterThanEqual(searchReq.getStartDate());
      }

      if (searchReq.getEndDate() != null) {
        criteria = criteria.and("createdAt").lessThanEqual(searchReq.getEndDate());
      }
    }

    org.springframework.data.elasticsearch.core.query.CriteriaQuery query =
        new org.springframework.data.elasticsearch.core.query.CriteriaQuery(criteria);
    query.setPageable(toPageable(request));
    return query;
  }
}
