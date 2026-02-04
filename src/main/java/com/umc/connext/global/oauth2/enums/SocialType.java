package com.umc.connext.global.oauth2.enums;

import lombok.Getter;

@Getter
public enum SocialType {
    NAVER("naver"),
    GOOGLE("google"),
    KAKAO("kakao"),
    LOCAL("local"); //자체로그인

    private final String value;

    SocialType(String value) {
        this.value = value;
    }
}
