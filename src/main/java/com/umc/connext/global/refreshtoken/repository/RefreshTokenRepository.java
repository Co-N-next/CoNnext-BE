package com.umc.connext.global.refreshtoken.repository;

import com.umc.connext.global.refreshtoken.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    void deleteAllByAuthKey(Long authKey);
}
