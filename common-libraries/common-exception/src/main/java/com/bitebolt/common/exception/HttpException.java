package com.bitebolt.common.exception;

import com.bitebolt.common.dto.LocalizedMessageDto;
import com.bitebolt.common.utils.MessageUtils;
import lombok.Getter;

/**
 * Custom runtime exception representing an HTTP error.
 *
 * <p>This exception encapsulates an HTTP status code together with a localized error message. It is
 * intended to be thrown from the service layer and handled by a global exception handler to produce
 * a standardized API error response.
 *
 * <p>The localized message is resolved immediately using {@link MessageUtils}, allowing dynamic
 * message formatting with placeholder arguments.
 *
 * <p>Example:
 *
 * <pre>{@code
 * throw new HttpException(
 *     404,
 *     "USER_NOT_FOUND",
 *     userId
 * );
 * }</pre>
 *
 * @author bitebolt Team
 * @since 1.0
 */
@Getter
public class HttpException extends RuntimeException {

  /** HTTP status code associated with this exception. */
  private final int statusCode;

  /** Localized error message containing translations in supported languages. */
  private final LocalizedMessageDto errorMessage;

  /**
   * Creates a new HTTP exception.
   *
   * <p>The message is resolved from the application's resource bundles using the provided message
   * code and optional formatting arguments.
   *
   * @param statusCode HTTP status code (e.g. 400, 404, 500)
   * @param messageCode message key defined in the localization resource files
   * @param args optional arguments used to replace placeholders (e.g. "{0}", "{1}") in the
   *     localized message
   */
  public HttpException(int statusCode, String messageCode, Object... args) {
    super(messageCode);
    this.statusCode = statusCode;
    this.errorMessage = MessageUtils.getMessage(messageCode, args);
  }
}
