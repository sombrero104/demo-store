package com.store.service.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class AiPromptSanitizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+?\\d{1,3}[-. ]?)?(?:\\d{2,4}[-. ]?){2,4}\\d{2,4}\\b");
    private static final Pattern JWT_PATTERN = Pattern.compile("\\beyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9._-]+\\.[A-Za-z0-9._-]+\\b");
    private static final Pattern LONG_NUMBER_PATTERN = Pattern.compile("\\b\\d{4,}\\b");

    public String sanitize(String message) {
        String sanitized = EMAIL_PATTERN.matcher(message).replaceAll("[EMAIL]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[PHONE]");
        sanitized = JWT_PATTERN.matcher(sanitized).replaceAll("[TOKEN]");
        sanitized = LONG_NUMBER_PATTERN.matcher(sanitized).replaceAll("[NUMBER]");
        return sanitized.trim();
    }
}
