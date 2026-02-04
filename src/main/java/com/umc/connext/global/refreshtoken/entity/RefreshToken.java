package com.umc.connext.global.refreshtoken.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor
@RedisHash(value = "refresh_token")
public class RefreshToken {

    @Id
    private String refreshToken;

    @Indexed
    private Long authKey;

    //리프레시 토큰의 생명 주기(14일)
    @TimeToLive
    private Long ttl;

    @Builder
    public RefreshToken(String refreshToken, Long authKey, Long ttl) {
        this.refreshToken = refreshToken;
        this.authKey = authKey;
        this.ttl = ttl;
    }
}
