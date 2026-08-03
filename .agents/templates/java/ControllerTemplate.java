package com.bitebolt.example.controller;

import com.bitebolt.common.dto.ApiResponse;
import com.bitebolt.example.service.ServiceTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;

import jakarta.validation.Valid;

/**
 * REST Controller for [DOMAIN] API endpoints.
 * 
 * Handles HTTP requests and delegates business logic to the service layer.
 * Responsible for:
 * - Request validation and conversion
 * - Response formatting and HTTP status codes
 * - API documentation via Swagger/OpenAPI
 * - Input/output DTO mapping
 * - Authorization checks (delegated to Security layer)
 * 
 * Base URL: /api/v1/[resource-name]
 * 
 * All endpoints return standardized ApiResponse wrapper.
 * All endpoints require authentication except where specified.
 * 
 * Example HTTP calls:
 * <pre>
 *   GET    /api/v1/users/123
 *   POST   /api/v1/users (with request body)
 *   PUT    /api/v1/users/123 (with request body)
 *   DELETE /api/v1/users/123
 * </pre>
 * 
 * @see ServiceTemplate for business logic
 * @see ApiResponse for response wrapper
 */
@RestController
@RequestMapping("/api/v1/[resource-name]")
@RequiredArgsConstructor
@Validated
@Tag(
    name = "[DOMAIN] API",
    description = "Endpoints for managing [DOMAIN] entities"
)
public class ControllerTemplate {

  private static final Logger logger = LoggerFactory.getLogger(ControllerTemplate.class);

  /**
   * Service layer injected by Spring.
   */
  private final ServiceTemplate service;

  // ==================== CREATE ====================

  /**
   * Creates a new [ENTITY].
   * 
   * HTTP Method: POST
   * Endpoint: /api/v1/[resource]/
   * Authorization: Required (ADMIN or STAFF role)
   * Request Body: CreateRequestTemplate (JSON)
   * Response Status: 201 Created
   * Response Body: ApiResponse containing ResponseTemplate
   * 
   * @param createRequest DTO with entity details to create
   * @return ApiResponse wrapping the created entity response
   * 
   * Example request:
   * <pre>
   *   POST /api/v1/users/
   *   Content-Type: application/json
   *   
   *   {
   *     "name": "John Doe",
   *     "email": "john@bitebolt.com"
   *   }
   * </pre>
   * 
   * Example response:
   * <pre>
   *   {
   *     "success": true,
   *     "data": {
   *       "id": 123,
   *       "name": "John Doe",
   *       "email": "john@bitebolt.com",
   *       "createdAt": "2024-08-03T10:30:00Z"
   *     },
   *     "message": "ENTITY_CREATED_SUCCESSFULLY"
   *   }
   * </pre>
   */
  @PostMapping
  @Operation(
      summary = "Create a new [ENTITY]",
      description = "Creates a new [ENTITY] with the provided details."
  )
  public ResponseEntity<ApiResponse<ResponseTemplate>> create(
      @Valid @RequestBody CreateRequestTemplate createRequest) {
    logger.debug("Received create request for [ENTITY]");

    ResponseTemplate response = service.create(createRequest);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(response, "ENTITY_CREATED_SUCCESSFULLY"));
  }

  // ==================== READ ====================

  /**
   * Retrieves a specific [ENTITY] by ID.
   * 
   * HTTP Method: GET
   * Endpoint: /api/v1/[resource]/{id}
   * Authorization: Required
   * Path Parameter: id (Long, > 0)
   * Response Status: 200 OK
   * Response Body: ApiResponse containing ResponseTemplate
   * 
   * @param id The unique identifier of the entity
   * @return ApiResponse wrapping the entity response
   * 
   * Example request:
   * <pre>
   *   GET /api/v1/users/123
   * </pre>
   * 
   * Example response:
   * <pre>
   *   {
   *     "success": true,
   *     "data": {
   *       "id": 123,
   *       "name": "John Doe",
   *       "createdAt": "2024-08-01T00:00:00Z"
   *     }
   *   }
   * </pre>
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get [ENTITY] by ID",
      description = "Retrieves a specific [ENTITY] using its unique identifier."
  )
  public ResponseEntity<ApiResponse<ResponseTemplate>> getById(
      @PathVariable
      @Parameter(description = "Entity ID (must be > 0)")
      Long id) {
    logger.debug("Fetching entity with id: {}", id);

    ResponseTemplate response = service.getById(id);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * Retrieves all [ENTITY] records with optional filtering and pagination.
   * 
   * HTTP Method: GET
   * Endpoint: /api/v1/[resource]/
   * Authorization: Required
   * Query Parameters: 
   *   - page (int, default 0): Page number (0-indexed)
   *   - size (int, default 10): Records per page
   *   - sort (String, default "id,desc"): Sort field and direction
   * Response Status: 200 OK
   * Response Body: ApiResponse containing paged results
   * 
   * @param pageable Pagination parameters (automatically resolved by Spring)
   * @return ApiResponse wrapping paged list of entities
   * 
   * Example request:
   * <pre>
   *   GET /api/v1/users/?page=0&size=20&sort=id,desc
   * </pre>
   * 
   * Example response:
   * <pre>
   *   {
   *     "success": true,
   *     "data": {
   *       "content": [
   *         { "id": 123, "name": "John Doe" },
   *         { "id": 122, "name": "Jane Doe" }
   *       ],
   *       "totalElements": 50,
   *       "totalPages": 3
   *     }
   *   }
   * </pre>
   */
  @GetMapping
  @Operation(
      summary = "Get all [ENTITY] records",
      description = "Retrieves paginated list of [ENTITY] records."
  )
  public ResponseEntity<ApiResponse<Page<ResponseTemplate>>> getAll(
      @ParameterObject
      Pageable pageable) {
    logger.debug("Fetching all entities with pagination: page={}, size={}", 
        pageable.getPageNumber(), pageable.getPageSize());

    Page<ResponseTemplate> response = service.getAll(pageable);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ==================== UPDATE ====================

  /**
   * Updates an existing [ENTITY].
   * 
   * HTTP Method: PUT
   * Endpoint: /api/v1/[resource]/{id}
   * Authorization: Required (typically ADMIN or STAFF)
   * Path Parameter: id (Long, > 0)
   * Request Body: UpdateRequestTemplate (JSON)
   * Response Status: 200 OK
   * Response Body: ApiResponse containing updated ResponseTemplate
   * 
   * Note: This is a partial update. Send only fields that need updating.
   * 
   * @param id The unique identifier of the entity to update
   * @param updateRequest DTO with fields to update (all optional)
   * @return ApiResponse wrapping the updated entity response
   * 
   * Example request:
   * <pre>
   *   PUT /api/v1/users/123
   *   Content-Type: application/json
   *   
   *   {
   *     "name": "Jane Doe"
   *   }
   * </pre>
   * 
   * Example response:
   * <pre>
   *   {
   *     "success": true,
   *     "data": {
   *       "id": 123,
   *       "name": "Jane Doe",
   *       "updatedAt": "2024-08-03T11:45:00Z"
   *     }
   *   }
   * </pre>
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update [ENTITY]",
      description = "Updates an existing [ENTITY] with provided fields."
  )
  public ResponseEntity<ApiResponse<ResponseTemplate>> update(
      @PathVariable
      @Parameter(description = "Entity ID (must be > 0)")
      Long id,
      @Valid @RequestBody UpdateRequestTemplate updateRequest) {
    logger.debug("Received update request for entity with id: {}", id);

    ResponseTemplate response = service.update(id, updateRequest);

    return ResponseEntity.ok(ApiResponse.success(response, "ENTITY_UPDATED_SUCCESSFULLY"));
  }

  // ==================== DELETE ====================

  /**
   * Deletes (soft-deletes) an [ENTITY].
   * 
   * HTTP Method: DELETE
   * Endpoint: /api/v1/[resource]/{id}
   * Authorization: Required (typically ADMIN or STAFF)
   * Path Parameter: id (Long, > 0)
   * Response Status: 204 No Content
   * Response Body: Empty
   * 
   * Note: This performs a soft delete. Data is not physically removed.
   * 
   * @param id The unique identifier of the entity to delete
   * @return Empty response with 204 No Content status
   * 
   * Example request:
   * <pre>
   *   DELETE /api/v1/users/123
   * </pre>
   * 
   * Example response:
   * <pre>
   *   HTTP/1.1 204 No Content
   * </pre>
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete [ENTITY]",
      description = "Soft-deletes an [ENTITY]. Data is marked as deleted but not physically removed."
  )
  public ResponseEntity<Void> delete(
      @PathVariable
      @Parameter(description = "Entity ID (must be > 0)")
      Long id) {
    logger.debug("Received delete request for entity with id: {}", id);

    service.delete(id);

    logger.info("Entity with id: {} deleted successfully", id);
    return ResponseEntity.noContent().build();
  }
}
