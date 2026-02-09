package com.umc.connext.global.auth.enums;

import lombok.Getter;

@Getter
public enum TokenCategory {
    ACCESS("access"),
    REFRESH("refresh"),
    SIGNUP("signup"),;

    private final String value;

    TokenCategory(String value) {
        this.value = value;
    }

    public static TokenCategory from(String value) {
        try {
            return TokenCategory.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰 카테고리: " + value);
        }
    }
}
