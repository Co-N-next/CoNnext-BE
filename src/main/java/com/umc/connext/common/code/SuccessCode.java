package com.umc.connext.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements Code {

    GET_SUCCESS(200, "조회 성공"),
    LOGIN_SUCCESS(200, "로그인 성공"),
    DELETE_SUCCESS(200, "삭제 성공"),
    INSERT_SUCCESS(201, "삽입 성공"),
    UPDATE_SUCCESS(204, "업데이트 성공");

    private final int statusCode;
    private final String message;

}