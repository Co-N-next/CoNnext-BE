package com.umc.connext.global.auth.util;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JWTProperties {
    private String secret;
    private Long accessTokenValidity;
    private Long refreshTokenValidity;
    private Long signupTokenValidity;   // ✅ 추가


    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValidity / 1000;
    }
    public Long getSignupTokenValiditySeconds() {
        return signupTokenValidity / 1000;
    }
}
