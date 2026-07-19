package com.ecomove.common.exception;

import com.ecomove.common.dto.ApiResponse;
import com.ecomove.common.dto.LocalizedMessageDto;
import com.ecomove.common.utils.MessageUtils;
import com.ecomove.common.utils.ResponseHelper;
import com.ecomove.common.validation.RequireField;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class centralizes exception handling across the application,
 * ensuring that all exceptions are converted into a standardized
 * {@link ApiResponse} before being returned to the client.
 *
 * <p>Using a global exception handler provides the following benefits:
 * <ul>
 *     <li>Consistent API response format</li>
 *     <li>Centralized error handling logic</li>
 *     <li>Reduced duplicate try-catch blocks in controllers</li>
 *     <li>Improved maintainability and readability</li>
 * </ul>
 *
 * <p>Additional exception handlers can be added as the application grows
 * to handle validation errors, authentication failures, database exceptions,
 * and unexpected system errors.
 *
 * @author Eco-Move Team
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link HttpException} and converts it into a standardized
     * API error response.
     *
     * <p>The HTTP status code and localized message are extracted from
     * the exception and used to build the response body.
     *
     * @param ex the thrown {@link HttpException}
     * @return a {@link ResponseEntity} containing the standardized
     *         error {@link ApiResponse}
     */
    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpException(HttpException ex) {

        ApiResponse<Object> response = ResponseHelper.error(
                ex.getStatusCode(),
                ex.getLocalizedMessage()
        );

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        if (ObjectUtils.isEmpty(fieldErrors)) {
            return buildFallbackResponse();
        }

        List<LocalizedMessageDto> errorsList = new ArrayList<>();

        for (FieldError fieldError : fieldErrors) {
            LocalizedMessageDto detailMessage = extractMessageFromAnnotation(fieldError);
            if (!ObjectUtils.isEmpty(detailMessage)) {
                errorsList.add(detailMessage);
            }
        }

        ApiResponse<Object> response = ResponseHelper.error(400, errorsList);
        return ResponseEntity.status(400).body(response);
    }


    private ResponseEntity<ApiResponse<Object>> buildFallbackResponse() {
        LocalizedMessageDto defaultMsg = MessageUtils.getMessage("ERROR_BAD_REQUEST");
        ApiResponse<Object> response = ResponseHelper.error(400, defaultMsg);
        return ResponseEntity.status(400).body(response);
    }

    private LocalizedMessageDto extractMessageFromAnnotation(FieldError fieldError) {
        try {
            ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
            Annotation annotation = violation.getConstraintDescriptor().getAnnotation();

            if (annotation instanceof RequireField requireField) {
                return formatRequireFieldMessage(requireField);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process validation annotation for field: " + fieldError.getField(), e);
        }
        return null;
    }

    private LocalizedMessageDto formatRequireFieldMessage(RequireField requireField) {
        String msgCode = requireField.messageCode();
        String argVi = requireField.i18n().vi();
        String argEn = requireField.i18n().en();

        LocalizedMessageDto templateMsg = MessageUtils.getMessage(msgCode);

        return LocalizedMessageDto.builder()
                .code(msgCode)
                .vi(String.format(templateMsg.getVi(), argVi))
                .en(String.format(templateMsg.getEn(), argEn))
                .build();
    }

}