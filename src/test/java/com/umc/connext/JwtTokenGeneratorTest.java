package com.umc.connext;

import com.umc.connext.global.auth.util.JWTProperties;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import javax.crypto.SecretKey;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://cssssssom:3306/connext",
        "spring.datasource.username=connextadmin",
        "spring.datasource.password=sssssssssssss!",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "jwt.secret=ssssssssssss",
        "jwt.access-token-validity=3600000",
        "jwt.refresh-token-validity=604800000",
        "jwt.signup-token-validity=600000"
})
class JwtTokenGeneratorTest {

    @Autowired
    private JWTProperties jwtProperties;

    @Test
    void generateMasterToken() {
        Long targetMemberId = 1L;
        String role = "USER";

        long expireTime = 1000L * 60 * 60 * 24 * 365 * 100;

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        SecretKey key = jwtProperties.getSecretKey();

        String masterToken = Jwts.builder()
                .claim("category", "ACCESS")
                .claim("role", role)
                .claim("memberId", targetMemberId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        System.out.println("\n\n========================================================");
        System.out.println("⬇️ [토큰 생성 완료] ⬇️");
        System.out.println("Bearer " + masterToken);
        System.out.println("========================================================\n\n");
    }
}