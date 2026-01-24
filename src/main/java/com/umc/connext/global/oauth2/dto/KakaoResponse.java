package com.umc.connext.global.oauth2.dto;

import com.umc.connext.global.oauth2.enums.OAuth2Provider;

import java.util.Map;

public class KakaoResponse implements  OAuth2Response{

    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public OAuth2Provider getProvider() {return OAuth2Provider.KAKAO;}

    @Override
    public String getProviderId() {return attribute.get("id").toString();}

    @Override
    public String getEmail() {
        return ((Map<String, Object>) attribute.get("kakao_account"))
                .get("email")
                .toString();
    }

    @Override
    public String getName() {
        return ((Map<String, Object>)
                ((Map<String, Object>) attribute.get("kakao_account"))
                        .get("profile"))
                .get("nickname")
                .toString();
    }
}
