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
    LOGOUT_SUCCESS(200, "로그아웃 성공"),
    DELETE_SUCCESS(200, "삭제 성공"),
    INSERT_SUCCESS(201, "삽입 성공"),
    UPDATE_SUCCESS(204, "업데이트 성공"),
    JOIN_SUCCESS(200, "회원가입 성공"),

    //토큰 재발급
    TOKEN_REISSUE_SUCCESS(200, "토큰 재발급 성공"),

    //아이디 중복 체크
    AVAILABLE_USERNAME(200, "사용 가능한 아이디입니다."),

    // 비밀번호 관련
    VALID_PASSWORD_FORMAT(200,"사용 가능한 비밀번호 형식입니다."),

    //닉네임 관련
    NICKNAME_GENERATION_SUCCESS(200, "랜덤 닉네임이 성공적으로 생성되었습니다."),
    AVAILABLE_NICKNAME(200, "사용 가능한 닉네임입니다."),
    NICKNAME_UPDATE_SUCCESS(200, "닉네임이 성공적으로 변경되었습니다.");

    private final int statusCode;
    private final String message;

}