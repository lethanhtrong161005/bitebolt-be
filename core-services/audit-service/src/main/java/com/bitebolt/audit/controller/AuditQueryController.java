package com.bitebolt.audit.controller;

import com.bitebolt.audit.constant.AuditMessageConstant;
import com.bitebolt.audit.dto.request.AuditLogSearchRequest;
import com.bitebolt.audit.dto.request.AuditLogSortCondition;
import com.bitebolt.audit.dto.response.AuditLogResponse;
import com.bitebolt.audit.service.AuditQueryService;
import com.bitebolt.common.dto.ApiResponse;
import com.bitebolt.common.dto.request.PayloadSearchRequest;
import com.bitebolt.common.dto.response.PageResponse;
import com.bitebolt.common.utils.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing search endpoints and metadata endpoints for audit compliance logs.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Query", description = "Endpoints for searching and retrieving audit compliance logs")
public class AuditQueryController
{
  private final AuditQueryService auditQueryService;

  /**
   * Search audit compliance events matching criteria using standardized PayloadSearchRequest payload.
   */
  @PostMapping("/search")
  @Operation(
      summary = "Search audit log events (Generic PayloadSearchRequest)",
      description = "Returns paginated audit log events filtered by page, sort, and search criteria using enterprise PayloadSearchRequest specification")
  public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchAuditLogs(
      @RequestBody @Valid PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest> searchRequest) {
    log.info("[AuditQuery] Processing payload search request: {}", searchRequest);
    PageResponse<AuditLogResponse> response = auditQueryService.searchAuditLogs(searchRequest);
    return ResponseEntity.ok(ResponseHelper.successWithData(response, AuditMessageConstant.SUCCESS_AUDIT_LOG_QUERY));
  }

  /**
   * Get dynamic list of available enterprise audit action names.
   */
  @GetMapping("/actions")
  @Operation(
      summary = "Get available audit actions",
      description = "Returns dynamic list of all enterprise audit actions defined in the system registry")
  public ResponseEntity<ApiResponse<List<String>>> getAvailableActions() {
    List<String> actions = auditQueryService.getAvailableActions();
    return ResponseEntity.ok(ResponseHelper.successWithData(actions, AuditMessageConstant.SUCCESS_AUDIT_LOG_QUERY));
  }

  /**
   * Get list of available audit execution statuses.
   */
  @GetMapping("/statuses")
  @Operation(
      summary = "Get available audit statuses",
      description = "Returns list of execution outcome statuses (SUCCESS, FAILURE)")
  public ResponseEntity<ApiResponse<List<String>>> getAvailableStatuses() {
    List<String> statuses = auditQueryService.getAvailableStatuses();
    return ResponseEntity.ok(ResponseHelper.successWithData(statuses, AuditMessageConstant.SUCCESS_AUDIT_LOG_QUERY));
  }
}
