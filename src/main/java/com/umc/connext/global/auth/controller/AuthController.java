package com.umc.connext.global.auth.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.member.dto.*;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.service.MemberService;
import com.umc.connext.domain.member.service.NicknameService;
import com.umc.connext.domain.member.service.TermService;
import com.umc.connext.global.auth.dto.*;
import com.umc.connext.global.auth.enums.TokenCategory;
import com.umc.connext.global.auth.service.AuthService;
import com.umc.connext.global.auth.service.ReissueService;
import com.umc.connext.global.auth.util.JWTUtil;
import com.umc.connext.global.auth.util.SecurityResponseWriter;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import com.umc.connext.global.auth.util.JWTProperties;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ReissueService reissueService;
    private final RefreshTokenService refreshTokenService;
    private final JWTProperties jwtProperties;
    private final MemberService memberService;
    private final NicknameService nicknameService;
    private final TermService termService;
    private final JWTUtil jwtUtil;
    private final SecurityResponseWriter securityResponseWriter;
    private final AuthenticationManager authenticationManager;

    @Operation(
            summary = "Local 회원가입",
            description = """
            email, password, 약관 동의 목록을 받아 회원가입을 진행합니다.
        
            회원가입 성공 시 자동으로 로그인 처리됩니다.
        
            - Access Token은 응답 헤더(Authorization)에 포함됩니다.
            - Refresh Token은 HttpOnly 쿠키(refresh)에 저장됩니다.
            """

            ,
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
                    @ApiResponse(responseCode = "403", description = "탈퇴한 회원입니다. 문의바랍니다."),
                    @ApiResponse(responseCode = "409", description = "중복된 email")
            }
    )
    @PostMapping("/signup/local")
    public ResponseEntity<Response<LoginResponseDTO>> joinLocal(@RequestBody @Valid JoinLocalRequestDTO joinLocalRequestDTO,
                                                    HttpServletResponse response){

        Member member = authService.joinLocal(joinLocalRequestDTO);
        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, member.getRole().name(), member.getId());
        String access = jwtUtil.createJwt(TokenCategory.ACCESS, member.getRole().name(), member.getId());

        refreshTokenService.saveRefreshToken(refresh, member.getId());
        addRefreshCookie(response, refresh);

        response.setHeader("Authorization", "Bearer " + access);


        LoginResponseDTO loginResponseDto = LoginResponseDTO.of(member.getEmail(), member.getNickname());

        return ResponseEntity
                .status(SuccessCode.JOIN_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.JOIN_SUCCESS, loginResponseDto));
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인된 사용자를 탈퇴 처리합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.")
            }
    )
    @DeleteMapping("/delete")
    public ResponseEntity<Response<Void>> delete(@AuthenticationPrincipal CustomUserDetails userDetails) {

        authService.withdrawCurrentUser(userDetails.getMemberId());

        return ResponseEntity
                .status(SuccessCode.DELETE_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.DELETE_SUCCESS));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token을 이용해 Access Token을 재발급합니다. Refresh Token은 쿠키, Access Token은 헤더를 통해 발급됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
                    @ApiResponse(responseCode = "401", description = "Refresh Token 만료 또는 없음")
            }
    )
    @PostMapping("/reissue")
    public ResponseEntity<Response<Void>> reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ReissueResultDTO result = reissueService.reissue(extractRefreshToken(request));

        response.setHeader("Authorization", "Bearer " + result.getAccessToken());
        addRefreshCookie(response, result.getRefreshToken());

        return ResponseEntity
                .status(SuccessCode.TOKEN_REISSUE_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.TOKEN_REISSUE_SUCCESS));
    }

    @Operation(
            summary = "비밀번호 형식 검사",
            description = "비밀번호가 형식에 맞는지 검사합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유효한 비밀번호 형식"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 비밀번호 형식")
            }
    )
    @PostMapping("/check-password-format")
    public ResponseEntity<Response<Boolean>> checkPasswordFormat(
            @RequestBody @Valid PasswordValidationCheckDTO passwordValidationCheckDTO) {

        return ResponseEntity
                .status(SuccessCode.VALID_PASSWORD_FORMAT.getStatusCode())
                .body(Response.success(SuccessCode.VALID_PASSWORD_FORMAT));
    }

    @Operation(
            summary = "ID 사용 가능 여부 확인",
            description = "이메일 형식 검증 및 중복 여부를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용 가능한 이메일"),
                    @ApiResponse(responseCode = "400", description = "이메일 형식 오류"),
                    @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
            }
    )
    @GetMapping("/email/availability")
    public ResponseEntity<Response<Void>> checkEmailAvailability(@RequestParam String email) {
        memberService.validateEmail(email);
        memberService.checkEmailDuplicate(email);
        return ResponseEntity
                .status(SuccessCode.AVAILABLE_EMAIL.getStatusCode())
                .body(Response.success(SuccessCode.AVAILABLE_EMAIL));
    }

    @Operation(
            summary = "nickname 사용 가능 여부 확인",
            description = """
                비밀번호가 형식에 맞는지 검사합니다.

                [비밀번호 조건]
                - 8자 이상 20자 이하
                - 영문자(A-Z, a-z) 최소 1자 포함
                - 숫자(0-9) 최소 1자 포함
                """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
                    @ApiResponse(responseCode = "400", description = "닉네임 길이 오류 (2자 이상 20자 이하)"),
                    @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임")
            }
    )
    @GetMapping("/nickname/availability")
    public ResponseEntity<Response<Void>> checkNicknameAvailability(
            @RequestParam @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하만 가능합니다.")
            String nickname) {
        nicknameService.checkNicknameDuplicate(nickname);
        return ResponseEntity
                .status(SuccessCode.AVAILABLE_NICKNAME.getStatusCode())
                .body(Response.success(SuccessCode.AVAILABLE_NICKNAME));
    }

    @Operation(
            summary = "랜덤 닉네임 생성",
            description = "서버에서 랜덤 닉네임을 생성하여 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "닉네임 생성 성공",
                            content = @Content(schema = @Schema(implementation = RandomNicknameResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "닉네임 생성 실패 (중복으로 인한 서버 내부 오류)")
            }
    )
    @GetMapping("/nickname/random")
    public ResponseEntity<Response<RandomNicknameResponseDTO>> generateRandomNickname() {
        String nickname = nicknameService.generateRandomNickname();
        return ResponseEntity
                .status(SuccessCode.NICKNAME_GENERATION_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.NICKNAME_GENERATION_SUCCESS,
                        RandomNicknameResponseDTO.of(nickname)));
    }

    @Operation(
            summary = "닉네임 변경",
            description = "로그인한 사용자의 닉네임을 변경합니다. 새 닉네임은 2~20자여야 합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임 형식 또는 현재와 동일한 닉네임"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임")
            })
    @PatchMapping("/nickname")
    public ResponseEntity<Response<Void>> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @RequestBody @Valid NicknameChangeRequestDTO nicknameChangeRequestDTO){

        nicknameService.changeNickname(userDetails.getMemberId(), nicknameChangeRequestDTO.getNickname());
        return ResponseEntity
                .status(SuccessCode.NICKNAME_UPDATE_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.NICKNAME_UPDATE_SUCCESS));
    }

    @Operation(
            summary = "로컬 로그인",
            description = "ID(email)와 password를 JSON으로 받아 로그인을 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "로그인 실패 (아이디 또는 비밀번호 불일치)"),
                    @ApiResponse(responseCode = "403", description = "탈퇴한 회원입니다.")
            }
    )
    @PostMapping("/login/local")
    public void loginLocal(@RequestBody LoginRequestDTO loginRequest,
                           HttpServletResponse response) throws IOException {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getMemberId();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String access = jwtUtil.createJwt(TokenCategory.ACCESS, role, memberId);
        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, role, memberId);

        refreshTokenService.removeAllByAuthKey(memberId);
        refreshTokenService.saveRefreshToken(refresh, memberId);

        response.setHeader("Authorization", "Bearer " + access);

        Cookie cookie = new Cookie("refresh", refresh);
        cookie.setMaxAge(Math.toIntExact(jwtProperties.getRefreshTokenValiditySeconds()));
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        LoginResponseDTO loginResponseDto = LoginResponseDTO.of(userDetails.getUsername(), userDetails.getNickname());
        Response<LoginResponseDTO> body = Response.success(SuccessCode.LOGIN_SUCCESS, loginResponseDto);

        securityResponseWriter.write(response, body);
    }

//    public Response<LoginResponseDTO> loginLocal(@RequestBody LoginRequestDTO loginRequest) {
//        throw new IllegalStateException("This method is intercepted by Spring Security Filter.");
//    }

    @Operation(
            summary = "로그아웃",
            description = "쿠키의 Refresh Token을 무효화하고 로그아웃을 진행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그아웃 성공 (쿠키 삭제 및 토큰 폐기)"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그아웃 실패 (토큰을 찾을 수 없거나 유효하지 않음)"
                    )
            }
    )
    @PostMapping("/logout")
    public Response<Void> logout() {
        throw new IllegalStateException("This method is intercepted by CustomLogoutFilter.");
    }

    @Operation(summary = "약관 목록 조회", description = "회원가입 시 사용되는 약관 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/terms")
    public ResponseEntity<Response<List<ActiveTermResponseDTO>>> getTerms() {
        List<ActiveTermResponseDTO> result = termService.getActiveTerms();
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "약관 목록 조회 성공"));
    }

    @Operation(summary = "약관 세부 정보 조회", description = "회원가입 시 사용되는 약관 세부 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 약관")
    })
    @GetMapping("/terms/{termsId}")
    public ResponseEntity<Response<TermsDetailResponseDTO>> getTermsDetail(@PathVariable Long termsId) {
        TermsDetailResponseDTO result = termService.getTermsDetail(termsId);
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "약관 세부 정보 조회 성공"));
    }

    @Operation(summary = "동의한 optional 약관 목록 조회",
            description = "사용자가 동의한 optional 약관 목록을 조회합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/terms/me")
    public ResponseEntity<Response<List<MyTermResponseDTO>>> myTerms(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MyTermResponseDTO> result = termService.getMyOptionalTerms(userDetails.getMemberId());
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "동의한 약관 목록 조회 성공"));
    }

    @Operation(summary = "동의한 약관 수정",
            description = "사용자가 동의한 optional 약관 목록을 수정합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "필수 약관 변경 시도 또는 잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "약관 또는 약관 정보가 존재하지 않음")
    })
    @PatchMapping("/terms/me")
    public ResponseEntity<Response<Void>> changeOptionalTerms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid OptionalTermsChangeRequestDTO request
    ) {

        termService.changeOptionalTerms(userDetails.getMemberId(), request.getAgreements());

        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, "동의한 약관 수정 성공"));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN, "리프레시 토큰이 존재하지 않습니다.");
        }

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN, "리프레시 토큰이 존재하지 않습니다.");
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenValiditySeconds()));
        response.addCookie(cookie);
    }
}
