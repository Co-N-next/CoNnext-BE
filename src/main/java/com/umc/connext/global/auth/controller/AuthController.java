package com.umc.connext.global.auth.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.member.service.MemberService;
import com.umc.connext.global.auth.dto.JoinDTO;
import com.umc.connext.global.auth.dto.PasswordValidationCheckDTO;
import com.umc.connext.global.auth.dto.ReissueResultDTO;
import com.umc.connext.global.auth.service.AuthService;
import com.umc.connext.global.auth.service.ReissueService;
import com.umc.connext.global.util.JWTProperties;
import com.umc.connext.global.util.SecurityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ReissueService reissueService;
    private final JWTProperties jwtProperties;
    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<Response<Void>> join(@RequestBody @Valid JoinDTO joinDTO){

        authService.join(joinDTO);
        return ResponseEntity
                .status(SuccessCode.JOIN_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.JOIN_SUCCESS));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Response<Void>> delete() {

        authService.withdrawCurrentUser(SecurityUtil.getCurrentUsername());

        return ResponseEntity
                .status(SuccessCode.DELETE_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.DELETE_SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<Response<Void>> reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ReissueResultDTO result = reissueService.reissue(extractRefreshToken(request));

        response.setHeader("Authorization", "Bearer " + result.getAccessToken());
        addRefreshCookie(response, result.getRefreshToken());

        return ResponseEntity
                .status(SuccessCode.TOKEN_REISSUE_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.TOKEN_REISSUE_SUCCESS));
    }

    @PostMapping("/check-password-format")
    public ResponseEntity<Response<Boolean>> checkPasswordFormat(
            @RequestBody @Valid PasswordValidationCheckDTO passwordValidationCheckDTO) {

        return ResponseEntity
                .status(SuccessCode.VALID_PASSWORD_FORMAT.getStatusCode())
                .body(Response.success(SuccessCode.VALID_PASSWORD_FORMAT));
    }

    @GetMapping("/nickname/availability")
    public ResponseEntity<Response<Void>> checkUsername(@RequestParam String username) {
        memberService.validateUsername(username);
        memberService.checkUsernameDuplicate(username);
        return ResponseEntity
                .status(SuccessCode.AVAILABLE_USERNAME.getStatusCode())
                .body(Response.success(SuccessCode.AVAILABLE_USERNAME));
    }

    @GetMapping("/username/availability")
    public ResponseEntity<Response<Void>> checkNickname(@RequestParam String nickname) {
        memberService.checkNicknameDuplicate(nickname);
        return ResponseEntity
                .status(SuccessCode.AVAILABLE_NICKNAME.getStatusCode())
                .body(Response.success(SuccessCode.AVAILABLE_NICKNAME));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"");
        }

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"");
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenValidity() / 1000));
        response.addCookie(cookie);
    }
}
