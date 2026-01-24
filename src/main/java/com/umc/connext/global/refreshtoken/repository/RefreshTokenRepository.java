package com.umc.connext.global.refreshtoken.repository;

import com.umc.connext.global.refreshtoken.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findRefreshTokenByJwtRefreshToken(String refreshToken);
    void deleteAllByAuthKey(String authKey);
}
