package com.bitebolt.example.service;

import com.bitebolt.common.exception.BadRequestException;
import com.bitebolt.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for [DOMAIN] business operations.
 * 
 * Handles all business logic related to [DOMAIN] entities.
 * Orchestrates calls to repositories, external services, and applies business rules.
 * 
 * Key responsibilities:
 * - Validate input data before database operations
 * - Implement soft delete pattern using isDeleted flag
 * - Handle transaction boundaries appropriately
 * - Log significant business events
 * - Throw domain-specific exceptions for error conditions
 * 
 * Dependencies:
 * - [EntityName]Repository for data persistence
 * - [OtherService]Service for cross-service operations
 * - PasswordEncoder or similar for data transformation
 * 
 * Thread-safe: Yes (Spring singleton, no mutable state)
 * 
 * Example usage:
 * <pre>
 *   &#64;Autowired
 *   private MyService myService;
 *   
 *   public void doSomething() {
 *     MyResponse response = myService.create(request);
 *   }
 * </pre>
 * 
 * @see [EntityName]Repository
 * @see [Request]DTO
 * @see [Response]DTO
 */
@Service
@RequiredArgsConstructor
public class ServiceTemplate {

  private static final Logger logger = LoggerFactory.getLogger(ServiceTemplate.class);

  /**
   * Repository for data access operations.
   * Auto-injected by Spring through constructor (Lombok @RequiredArgsConstructor).
   */
  private final RepositoryTemplate repository;

  /**
   * Other service dependency for cross-cutting concerns.
   * Auto-injected by Spring through constructor.
   */
  // private final OtherService otherService;

  // ==================== CREATE OPERATIONS ====================

  /**
   * Creates a new [ENTITY] in the system.
   * 
   * Business logic flow:
   * 1. Validate request input for required fields and format
   * 2. Check for business rule violations (e.g., duplicates, constraints)
   * 3. Perform any necessary transformations or enrichments
   * 4. Save entity to database
   * 5. Return response DTO (never return entity directly)
   * 
   * @param createRequest DTO containing creation parameters (never null)
   * @return Response DTO containing created entity details
   * @throws BadRequestException if input validation fails
   * @throws DuplicateEntityException if entity already exists with unique constraint
   * @throws SystemException if database operation fails unexpectedly
   * 
   * Example:
   * <pre>
   *   CreateRequest request = new CreateRequest("test@bitebolt.com");
   *   Response response = service.create(request);
   * </pre>
   */
  @Transactional
  public ResponseTemplate create(CreateRequestTemplate createRequest) {
    // Step 1: Validate input
    validateCreateRequest(createRequest);
    logger.debug("Creating new entity with request: {}", createRequest.getId());

    // Step 2: Check for duplicates or business rule violations
    if (repository.existsByName(createRequest.getName())) {
      logger.warn("Attempted to create entity with duplicate name: {}", createRequest.getName());
      throw new BadRequestException("ENTITY_NAME_ALREADY_EXISTS");
    }

    // Step 3: Perform transformations
    // Example: hash password, encode data, set defaults
    EntityTemplate entity = EntityTemplate.builder()
        .name(createRequest.getName())
        .description(createRequest.getDescription())
        .isDeleted(false)
        .build();

    // Step 4: Save to database
    EntityTemplate savedEntity = repository.save(entity);
    logger.info("Entity created successfully with id: {}", savedEntity.getId());

    // Step 5: Convert to response DTO and return
    return toResponseDTO(savedEntity);
  }

  // ==================== READ OPERATIONS ====================

  /**
   * Retrieves a specific [ENTITY] by its unique identifier.
   * 
   * Business logic flow:
   * 1. Validate input ID format
   * 2. Query database for active (non-deleted) entity
   * 3. Return response DTO
   * 
   * @param id The unique identifier of the entity (must be > 0)
   * @return Response DTO if entity found
   * @throws InvalidIdException if id format is invalid
   * @throws EntityNotFoundException if entity not found or is soft-deleted
   * 
   * Example:
   * <pre>
   *   Response response = service.getById(123L);
   * </pre>
   */
  @Transactional(readOnly = true)
  public ResponseTemplate getById(Long id) {
    // Step 1: Validate ID
    if (id == null || id <= 0) {
      logger.warn("Invalid ID provided: {}", id);
      throw new BadRequestException("INVALID_ID_FORMAT");
    }

    // Step 2: Query database (note: repository should filter isDeleted=false)
    EntityTemplate entity = repository.findById(id)
        .orElseThrow(() -> {
          logger.warn("Entity not found with id: {}", id);
          return new NotFoundException("ENTITY_NOT_FOUND");
        });

    // Step 3: Return response DTO
    return toResponseDTO(entity);
  }

  /**
   * Retrieves all active [ENTITY] records matching optional filters.
   * 
   * Business logic flow:
   * 1. Build query with filters
   * 2. Execute paginated query (excludes soft-deleted)
   * 3. Convert results to response DTOs
   * 
   * @param filter Optional filter criteria (can be null)
   * @return List of response DTOs (never null, empty list if none found)
   * 
   * Example:
   * <pre>
   *   List<Response> results = service.getAll(filter);
   * </pre>
   */
  @Transactional(readOnly = true)
  public java.util.List<ResponseTemplate> getAll(FilterTemplate filter) {
    // Step 1: Build query based on filters
    logger.debug("Querying entities with filter: {}", filter);

    // Step 2: Execute query (repository method must filter isDeleted=false)
    java.util.List<EntityTemplate> entities = repository.findAllActive();

    // Step 3: Convert to response DTOs
    return entities.stream()
        .map(this::toResponseDTO)
        .toList();
  }

  // ==================== UPDATE OPERATIONS ====================

  /**
   * Updates an existing [ENTITY] with new values.
   * 
   * Business logic flow:
   * 1. Find existing entity by ID
   * 2. Validate update request
   * 3. Check for business rule violations
   * 4. Merge changes into entity
   * 5. Save updated entity
   * 6. Return updated response DTO
   * 
   * @param id The ID of entity to update (must be > 0)
   * @param updateRequest DTO containing new values (never null)
   * @return Updated response DTO
   * @throws EntityNotFoundException if entity not found
   * @throws BadRequestException if update violates business rules
   * 
   * Example:
   * <pre>
   *   Response response = service.update(123L, updateRequest);
   * </pre>
   */
  @Transactional
  public ResponseTemplate update(Long id, UpdateRequestTemplate updateRequest) {
    // Step 1: Validate inputs
    if (id == null || id <= 0) {
      throw new BadRequestException("INVALID_ID_FORMAT");
    }
    validateUpdateRequest(updateRequest);
    logger.debug("Updating entity with id: {}", id);

    // Step 2: Find existing entity
    EntityTemplate entity = repository.findById(id)
        .orElseThrow(() -> {
          logger.warn("Entity not found for update with id: {}", id);
          return new NotFoundException("ENTITY_NOT_FOUND");
        });

    // Step 3: Check business rule violations
    if (updateRequest.getName() != null && 
        !updateRequest.getName().equals(entity.getName()) &&
        repository.existsByName(updateRequest.getName())) {
      throw new BadRequestException("ENTITY_NAME_ALREADY_EXISTS");
    }

    // Step 4: Merge changes
    if (updateRequest.getName() != null) {
      entity.setName(updateRequest.getName());
    }
    if (updateRequest.getDescription() != null) {
      entity.setDescription(updateRequest.getDescription());
    }
    // Set updatedAt timestamp (if your entity has it)
    // entity.setUpdatedAt(LocalDateTime.now());

    // Step 5: Save changes
    EntityTemplate updatedEntity = repository.save(entity);
    logger.info("Entity updated successfully with id: {}", id);

    // Step 6: Return updated response
    return toResponseDTO(updatedEntity);
  }

  // ==================== DELETE OPERATIONS ====================

  /**
   * Soft-deletes a [ENTITY] by marking it as deleted.
   * 
   * Soft delete philosophy:
   * - Does not physically remove data from database
   * - Sets isDeleted flag to true
   * - Data remains available for audit/compliance purposes
   * - Soft-deleted entities excluded from normal queries
   * 
   * Business logic flow:
   * 1. Find entity by ID
   * 2. Set isDeleted = true
   * 3. Update timestamps if applicable
   * 4. Save to database
   * 5. Log deletion event
   * 
   * @param id The ID of entity to delete (must be > 0)
   * @return Empty response or confirmation
   * @throws EntityNotFoundException if entity not found
   * @throws BadRequestException if id is invalid
   * 
   * Example:
   * <pre>
   *   service.delete(123L);
   * </pre>
   */
  @Transactional
  public void delete(Long id) {
    // Step 1: Validate ID
    if (id == null || id <= 0) {
      logger.warn("Invalid ID for deletion: {}", id);
      throw new BadRequestException("INVALID_ID_FORMAT");
    }

    // Step 2: Find entity
    EntityTemplate entity = repository.findById(id)
        .orElseThrow(() -> {
          logger.warn("Entity not found for deletion with id: {}", id);
          return new NotFoundException("ENTITY_NOT_FOUND");
        });

    // Step 3: Set soft delete flag
    entity.setIsDeleted(true);
    // entity.setDeletedAt(LocalDateTime.now());  // If tracking deletion timestamp

    // Step 4: Save changes
    repository.save(entity);
    logger.info("Entity soft-deleted with id: {}", id);
  }

  // ==================== PRIVATE HELPER METHODS ====================

  /**
   * Validates create request for required fields and format constraints.
   * 
   * @param request The request to validate
   * @throws BadRequestException if validation fails
   */
  private void validateCreateRequest(CreateRequestTemplate request) {
    if (request == null) {
      throw new BadRequestException("REQUEST_CANNOT_BE_NULL");
    }
    if (request.getName() == null || request.getName().trim().isEmpty()) {
      throw new BadRequestException("NAME_REQUIRED");
    }
    if (request.getName().length() > 255) {
      throw new BadRequestException("NAME_TOO_LONG");
    }
  }

  /**
   * Validates update request for format constraints.
   * Note: Unlike create, update fields are optional.
   * 
   * @param request The request to validate
   * @throws BadRequestException if validation fails
   */
  private void validateUpdateRequest(UpdateRequestTemplate request) {
    if (request == null) {
      throw new BadRequestException("REQUEST_CANNOT_BE_NULL");
    }
    if (request.getName() != null && request.getName().length() > 255) {
      throw new BadRequestException("NAME_TOO_LONG");
    }
  }

  /**
   * Converts entity to response DTO.
   * IMPORTANT: Never expose sensitive or internal fields in response.
   * 
   * @param entity The entity to convert
   * @return Response DTO
   */
  private ResponseTemplate toResponseDTO(EntityTemplate entity) {
    return ResponseTemplate.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        // Do NOT include: password, internalFlags, auditData
        .build();
  }

  /**
   * Converts update request to entity.
   * Used for merging partial updates.
   * 
   * @param request The update request
   * @return Entity with updated fields
   */
  private EntityTemplate toEntity(UpdateRequestTemplate request) {
    return EntityTemplate.builder()
        .name(request.getName())
        .description(request.getDescription())
        .build();
  }
}
