package com.ecomove.common.utils;

import com.ecomove.common.constant.AppConstant;
import com.ecomove.common.dto.ApiResponse;
import com.ecomove.common.dto.LocalizedMessageDto;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

/**
 * Utility class for creating standardized API responses.
 *
 * <p>This helper centralizes the creation of success and error responses,
 * ensuring a consistent response structure across all services. It also
 * automatically includes:
 * <ul>
 *     <li>HTTP status code</li>
 *     <li>Localized message</li>
 *     <li>Response payload</li>
 *     <li>Trace request identifier from MDC</li>
 *     <li>Response timestamp</li>
 * </ul>
 *
 * <p>This class is designed as a utility class and cannot be instantiated.
 *
 * @author Eco-Move Team
 * @since 1.0
 */
public final class ResponseHelper {

    /**
     * Prevents instantiation of this utility class.
     */
    private ResponseHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Builds a successful API response.
     *
     * @param message localized response message
     * @param data response payload
     * @param <T> payload type
     * @return a standardized success response
     */
    private static <T> ApiResponse<T> buildSuccess(LocalizedMessageDto message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .traceRequest(MDC.get(AppConstant.TRACE_ID_KEY))
                .time(Instant.now())
                .build();
    }

    /**
     * Creates a successful API response containing data.
     *
     * <p>The localized message is resolved using the specified message code
     * and optional formatting arguments.
     *
     * @param data response payload
     * @param messageCode localization message key
     * @param args optional arguments for message formatting
     * @param <T> payload type
     * @return a success response with data
     */
    public static <T> ApiResponse<T> successWithData(
            T data,
            String messageCode,
            Object... args) {

        LocalizedMessageDto message = MessageUtils.getMessage(messageCode, args);
        return buildSuccess(message, data);
    }

    /**
     * Creates a successful API response without a payload.
     *
     * <p>The localized message is resolved using the specified message code
     * and optional formatting arguments.
     *
     * @param messageCode localization message key
     * @param args optional arguments for message formatting
     * @param <T> payload type
     * @return a success response without data
     */
    public static <T> ApiResponse<T> success(
            String messageCode,
            Object... args) {

        LocalizedMessageDto message = MessageUtils.getMessage(messageCode, args);
        return buildSuccess(message, null);
    }

    /**
     * Creates a standardized error response.
     *
     * <p>The localized error message is resolved using the specified message
     * code and optional formatting arguments.
     *
     * @param httpStatus HTTP status code
     * @param messageCode localization message key
     * @param args optional arguments for message formatting
     * @param <T> payload type
     * @return an error response
     */
    public static <T> ApiResponse<T> error(
            int httpStatus,
            String messageCode,
            Object... args) {

        LocalizedMessageDto message = MessageUtils.getMessage(messageCode, args);

        return ApiResponse.<T>builder()
                .status(httpStatus)
                .message(message)
                .data(null)
                .traceRequest(MDC.get(AppConstant.TRACE_ID_KEY))
                .time(Instant.now())
                .build();
    }


    /**
     * Creates a standardized error API response.
     *
     * <p>This method builds an error response containing the specified HTTP
     * status code and localized message. The response payload is set to
     * {@code null}, while the current trace identifier and timestamp are
     * automatically included.
     *
     * @param httpStatus the HTTP status code (e.g. 400, 404, 500)
     * @param message the localized error message
     * @param <T> the type of the response payload
     * @return a standardized error {@link ApiResponse}
     */
    public static <T> ApiResponse<T> error(int httpStatus, LocalizedMessageDto message) {
        return ApiResponse.<T>builder()
                .status(httpStatus)
                .message(message)
                .traceRequest(MDC.get(AppConstant.TRACE_ID_KEY))
                .time(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int httpStatus, List<LocalizedMessageDto> errors) {
        return ApiResponse.<T>builder()
                .status(httpStatus)
                .message(null)
                .data(null)
                .errors(errors)
                .traceRequest(MDC.get(AppConstant.TRACE_ID_KEY))
                .time(Instant.now())
                .build();
    }

}