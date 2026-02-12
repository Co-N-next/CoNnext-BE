package com.umc.connext.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements Code {

    // ==================== 공통 에러 ====================
    INTERNAL_SERVER_ERROR(500, "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(400, "잘못된 요청입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(405, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(415, "지원하지 않는 미디어 타입입니다."),

    // ==================== 입력값 검증 ====================
    VALIDATION_ERROR(400, "입력값이 올바르지 않습니다."),

    // ==================== 인증/로그인 ====================
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(401, "리프레시 토큰이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_TOKEN_CATEGORY(401, "토큰 타입이 올바르지 않습니다."),
    UNSUPPORTED_TOKEN(401, "지원하지 않는 토큰입니다."),
    NOT_FOUND_TOKEN(401, "토큰이 존재하지 않습니다."),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),

    // ==================== 검색 ====================
    INVALID_SEARCH_KEYWORD(400, "검색어는 공백일 수 없습니다."),

    // ==================== 페이징 ====================
    INVALID_PAGE_REQUEST(400, "page는 0 이상, size는 1~100 사이여야 합니다."),

    // ==================== 멤버 ====================
    NOT_FOUND_MEMBER(404, "존재하지 않는 회원입니다."),
    ID_ALREADY_EXISTS(409, "이미 존재하는 아이디입니다."),
    MEMBER_DELETED(403, "탈퇴 처리된 회원입니다."),
    INVALID_MEMBER_ROLE(403, "유효하지 않은 권한입니다."),
    EMAIL_ALREADY_USED_BY_LOCAL(409, "이미 자체 회원가입으로 등록된 이메일입니다."),
    EMAIL_ALREADY_USED_BY_SOCIAL(409,  "이미 다른 소셜 계정으로 등록된 이메일입니다."),
    INVALID_LOGIN_TYPE(400, "잘못된 로그인 방식입니다."),

    // ==================== 닉네임 ====================
    NICKNAME_GENERATION_FAILED(500, "랜덤 닉네임 생성에 실패했습니다. 다시 시도해 주세요."),
    NICKNAME_ALREADY_EXISTS(409, "이미 사용 중인 닉네임입니다."),

    // ==================== 아이디 ====================
    INVALID_MEMBER_ID(400, "유효하지 않은 아이디 형식입니다."),

    // ==================== 약관동의 ====================
    INVALID_TERM_TYPE(400, "수정할 수 없는 약관 타입입니다."),
    MISSING_REQUIRED_TERM(400, "필수 약관에 동의해야 합니다."),

    // ==================== 메이트 ====================
    MATE_CONFLICT(409, "이미 메이트 관계이거나 요청이 진행 중입니다."),
    MATE_NOT_FOUND(400, "메이트를 찾을 수 없습니다.");

    private final int statusCode;
    private final String message;
}