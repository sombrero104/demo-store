package com.store.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.cookie")
@Data
public class SecurityCookieProperties {

    private boolean secure;

    private String sameSite = "Lax";

    private String path = "/api/account";

    private long maxAgeSeconds;
}
