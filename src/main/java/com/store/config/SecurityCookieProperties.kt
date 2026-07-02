package com.store.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.cookie")
data class SecurityCookieProperties(
    val isSecure: Boolean = false,
    val sameSite: String = "Lax",
    val path: String = "/api/account",
    val maxAgeSeconds: Long = 0,
)
