package com.umc.connext.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements Code {

    OK(200, "요청 처리 성공"),
    CREATED(201, "리소스 생성 성공"),
    NO_CONTENT(204, "요청 성공, 데이터 없음"),

    ;

    private final int statusCode;
    private final String message;

}