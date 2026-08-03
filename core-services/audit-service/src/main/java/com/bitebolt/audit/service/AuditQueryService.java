package com.bitebolt.audit.service;

import com.bitebolt.audit.dto.request.AuditLogSearchRequest;
import com.bitebolt.audit.dto.request.AuditLogSortCondition;
import com.bitebolt.audit.dto.response.AuditLogResponse;
import com.bitebolt.common.dto.request.PayloadSearchRequest;
import com.bitebolt.common.dto.response.PageResponse;

import java.util.List;

/**
 * Service contract for searching and retrieving audit compliance events using generic PayloadSearchRequest pattern.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Request Reception:</strong> Accepts validated generic {@link PayloadSearchRequest}.
 *   <li><strong>Helper Delegation:</strong> Delegates specification building and pagination to {@code AuditLogHelper}.
 *   <li><strong>Page Execution:</strong> Queries repository and returns mapped {@link PageResponse<AuditLogResponse>}.
 * </ol>
 */
public interface AuditQueryService
{
  /**
   * Search audit compliance events matching search, sort, and pagination criteria.
   *
   * @param request Generic PayloadSearchRequest container wrapping page, sort, and search payloads.
   * @return PageResponse envelope containing mapped AuditLogResponse DTO records.
   */
  PageResponse<AuditLogResponse> searchAuditLogs(
      PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest> request);

  /**
   * Returns list of all available enterprise audit action names dynamically from AuditAction enum registry.
   *
   * @return List of action string names.
   */
  List<String> getAvailableActions();

  /**
   * Returns list of available execution outcome statuses.
   *
   * @return List of status strings.
   */
  List<String> getAvailableStatuses();
}
