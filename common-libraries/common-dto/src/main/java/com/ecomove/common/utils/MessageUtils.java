package com.ecomove.common.utils;

import com.ecomove.common.dto.LocalizedMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Utility component for resolving localized messages from resource bundles.
 *
 * <p>This class provides a centralized mechanism for retrieving application
 * messages in multiple languages using Spring's {@link MessageSource}.
 * It currently supports:
 * <ul>
 *     <li>Vietnamese (vi)</li>
 *     <li>English (en)</li>
 * </ul>
 *
 * <p>The resolved messages are wrapped in a {@link LocalizedMessageDto},
 * allowing API responses to include both language versions simultaneously.
 *
 * <p>Message definitions are loaded from the application's
 * {@code messages.properties} resource bundles.
 *
 * <p>This utility exposes static methods for convenient access throughout
 * the application while relying on Spring Dependency Injection to initialize
 * the underlying {@link MessageSource}.
 *
 * @author Eco-Move Team
 * @since 1.0
 */
@Component
public class MessageUtils {

    /**
     * Spring message source used to resolve localized messages.
     */
    private static MessageSource messageSource;

    /**
     * Initializes the message source used by this utility.
     *
     * @param messageSource the Spring {@link MessageSource} bean
     */
    @Autowired
    public MessageUtils(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    /**
     * Retrieves a localized message in both Vietnamese and English.
     *
     * <p>If the specified message code cannot be found, the message code itself
     * will be returned as the default message.
     *
     * @param messageCode the message key defined in the resource bundle
     * @param args optional arguments used for placeholder substitution
     *             (e.g. "{0}", "{1}")
     * @return a {@link LocalizedMessageDto} containing the message code and
     *         its Vietnamese and English translations
     */
    public static LocalizedMessageDto getMessage(String messageCode, Object... args) {

        String vi = messageSource.getMessage(
                messageCode,
                args,
                messageCode,
                new Locale("vi")
        );

        String en = messageSource.getMessage(
                messageCode,
                args,
                messageCode,
                new Locale("en")
        );

        return LocalizedMessageDto.builder()
                .code(messageCode)
                .vi(vi)
                .en(en)
                .build();
    }
}