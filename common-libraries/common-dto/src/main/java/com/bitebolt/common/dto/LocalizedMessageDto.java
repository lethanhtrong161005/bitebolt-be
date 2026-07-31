package com.bitebolt.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a localized message.
 *
 * <p>This class encapsulates a message code along with its localized
 * representations in Vietnamese and English. It is primarily used in
 * standardized API responses to support internationalization (i18n),
 * allowing clients to display messages in the user's preferred language.
 *
 * <p>Example:
 * <pre>{@code
 * {
 *   "code": "USER_NOT_FOUND",
 *   "vi": "Không tìm thấy người dùng.",
 *   "en": "User not found."
 * }
 * }</pre>
 *
 * @author bitebolt Team
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalizedMessageDto {

    /**
     * Unique message identifier used for localization and client-side processing.
     */
    private String code;

    /**
     * Localized message in Vietnamese.
     */
    private String vi;

    /**
     * Localized message in English.
     */
    private String en;

}