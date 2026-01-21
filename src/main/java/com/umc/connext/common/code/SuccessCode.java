package com.umc.connext.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements Code {

    OK(200, "요청 처리 성공"),
    CREATED(201, "리소스 생성 성공"),
    NO_CONTENT(204, "요청 성공, 데이터 없음"),
    GET_SUCCESS(200, "조회 성공"),
    LOGIN_SUCCESS(200, "로그인 성공"),
    DELETE_SUCCESS(200, "삭제 성공"),
    INSERT_SUCCESS(201, "삽입 성공"),
    UPDATE_SUCCESS(204, "업데이트 성공");

    private final int statusCode;
    private final String message;

}