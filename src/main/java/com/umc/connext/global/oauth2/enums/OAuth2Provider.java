package com.umc.connext.global.oauth2.enums;

public enum OAuth2Provider {
    NAVER("naver"),
    GOOGLE("google"),
    KAKAO("kakao");

    private final String value;

    OAuth2Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
