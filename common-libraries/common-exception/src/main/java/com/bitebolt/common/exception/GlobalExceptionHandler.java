package com.bitebolt.common.exception;

import com.bitebolt.common.dto.ApiResponse;
import com.bitebolt.common.dto.LocalizedMessageDto;
import com.bitebolt.common.utils.MessageUtils;
import com.bitebolt.common.utils.ResponseHelper;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class centralizes exception handling across the application, ensuring that all exceptions
 * are converted into a standardized {@link ApiResponse} before being returned to the client.
 *
 * <p>Standard Execution Steps:
 *
 * <ol>
 *   <li><strong>HttpException:</strong> Maps application HTTP status exceptions to ApiResponse error envelopes.
 *   <li><strong>MethodArgumentNotValidException:</strong> Processes all field and class-level ObjectErrors (such as {@code @ValidSort}).
 *   <li><strong>Unwrap & Localization:</strong> Unwraps {@link ConstraintViolation} to extract raw message code key template (e.g. {@code {ERROR_INVALID_SORT_FIELD}}) and resolves VI/EN translations via {@link MessageUtils}.
 * </ol>
 *
 * @author bitebolt Team
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles {@link HttpException} and converts it into a standardized API error response.
   *
   * @param ex the thrown {@link HttpException}
   * @return a {@link ResponseEntity} containing the standardized error {@link ApiResponse}
   */
  @ExceptionHandler(HttpException.class)
  public ResponseEntity<ApiResponse<Object>> handleHttpException(HttpException ex) {
    ApiResponse<Object> response =
        ResponseHelper.error(ex.getStatusCode(), ex.getLocalizedMessage());

    return ResponseEntity.status(ex.getStatusCode()).body(response);
  }

  /**
   * Handles JSR-380 validation exceptions including both field-level and class-level constraint violations (e.g. {@code @ValidSort}).
   *
   * @param ex MethodArgumentNotValidException thrown during @Valid request processing.
   * @return ResponseEntity with HTTP 400 Bad Request containing localized error details.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();

    if (ObjectUtils.isEmpty(allErrors)) {
      return buildFallbackResponse();
    }

    List<LocalizedMessageDto> errorsList = new ArrayList<>();

    for (ObjectError error : allErrors) {
      LocalizedMessageDto detailMessage = extractLocalizedMessage(error);
      if (detailMessage != null) {
        errorsList.add(detailMessage);
      }
    }

    if (errorsList.isEmpty()) {
      return buildFallbackResponse();
    }

    ApiResponse<Object> response = ResponseHelper.error(400, errorsList);
    return ResponseEntity.status(400).body(response);
  }

  private ResponseEntity<ApiResponse<Object>> buildFallbackResponse() {
    LocalizedMessageDto defaultMsg = MessageUtils.getMessage("ERROR_BAD_REQUEST");
    ApiResponse<Object> response = ResponseHelper.error(400, defaultMsg);
    return ResponseEntity.status(400).body(response);
  }

  private LocalizedMessageDto extractLocalizedMessage(ObjectError error) {
    try {
      ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
      if (violation != null && violation.getConstraintDescriptor() != null) {
        String template = violation.getConstraintDescriptor().getMessageTemplate();
        if (!ObjectUtils.isEmpty(template)) {
          String key = template.replaceAll("[{}]", "");
          return MessageUtils.getMessage(key);
        }
      }
    } catch (Exception ignored) {
      // Fallback if unwrap is not available
    }

    String defaultMessage = error.getDefaultMessage();
    if (!ObjectUtils.isEmpty(defaultMessage)) {
      String messageKey = defaultMessage.replaceAll("[{}]", "");
      return MessageUtils.getMessage(messageKey);
    }
    return MessageUtils.getMessage("ERROR_BAD_REQUEST");
  }
}
