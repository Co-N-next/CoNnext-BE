package com.umc.connext.global.refreshtoken.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@NoArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    @Indexed
    private String jwtRefreshToken;

    // 맴버 이메일로 설정
    private String authKey;

    //리프레시 토큰의 생명 주기(14일)
    @TimeToLive
    private Long ttl;

    @Builder
    public RefreshToken(String jwtRefreshToken, String authKey, Long ttl) {
        this.jwtRefreshToken = jwtRefreshToken;
        this.authKey = authKey;
        this.ttl = ttl;
    }
}
