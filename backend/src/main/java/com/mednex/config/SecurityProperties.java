package com.mednex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mednex.security")
public record SecurityProperties(String issuer, String jwkSetUri) {}
