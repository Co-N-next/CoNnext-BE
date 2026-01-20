package com.umc.connext.common.exception;

import com.umc.connext.common.code.ErrorCode;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final ErrorCode errorCode;

    public GeneralException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static GeneralException notFound(String message) {
        return new GeneralException(ErrorCode.NOT_FOUND, message);
    }
}