package com.umc.connext.domain.mate.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.mate.dto.MateReqDTO;
import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.domain.mate.dto.TodayMateResDTO;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/mates")
public interface MateControllerDocs {

    // 메이트 요청
    @Operation(
            summary = "메이트 요청",
            description = "다른 회원에게 메이트 요청을 보냅니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 요청 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음")
    })
    @PostMapping("/request")
    ResponseEntity<Response<MateResDTO.MateRequestResDTO>> requestMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MateReqDTO.MateRequestDTO dto
    );

    // 메이트 요청 수락
    @Operation(
            summary = "메이트 요청 수락",
            description = "받은 메이트 요청을 수락합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 요청 수락 성공"),
            @ApiResponse(responseCode = "404", description = "메이트 요청이 존재하지 않습니다.")
    })
    @PostMapping("/{mateId}/accept")
    ResponseEntity<Response<Void>> acceptMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 메이트 요청 거절
    @Operation(
            summary = "메이트 요청 거절",
            description = "받은 메이트 요청을 거절합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 요청 거절 성공"),
            @ApiResponse(responseCode = "404", description = "메이트 요청이 존재하지 않습니다.")
    })
    @PostMapping("/{mateId}/reject")
    ResponseEntity<Response<Void>> rejectMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 메이트 목록 조회
    @Operation(
            summary = "메이트 목록 조회",
            description = "내 메이트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 목록 조회 성공")
    })
    @GetMapping("")
    ResponseEntity<Response<List<MateResDTO.MateListResDTO>>> getMyMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    // 메이트 삭제
    @Operation(
            summary = "메이트 삭제",
            description = "메이트 목록에서 메이트를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "메이트가 존재하지 않습니다.")
    })
    @DeleteMapping("/{mateId}")
    ResponseEntity<Response<Void>> deleteMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 메이트 검색
    @Operation(
            summary = "메이트 검색",
            description = "닉네임으로 메이트를 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 검색 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 검색어입니다.")
    })
    @GetMapping("/search")
    ResponseEntity<Response<List<MateResDTO.MateSearchResDTO>>> searchMates(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank(message = "검색어는 공백일 수 없습니다.") String keyword,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page는 0 이상이어야 합니다.") Integer page
    );

    // 자주 찾는 메이트 추가
    @Operation(
            summary = "자주 찾는 메이트 추가",
            description = "메이트 목록에서 자주 찾는 메이트로 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자주 찾는 메이트 추가 성공"),
            @ApiResponse(responseCode = "404", description = "메이트가 존재하지 않습니다.")
    })
    @PostMapping("/{mateId}/favorite")
    ResponseEntity<Response<Void>> addFavoriteMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 자주 찾는 메이트 제거
    @Operation(
            summary = "자주 찾는 메이트 제거",
            description = "메이트 목록에서 자주 찾는 메이트를 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자주 찾는 메이트 제거 성공"),
            @ApiResponse(responseCode = "404", description = "메이트가 존재하지 않습니다.")
    })
    @DeleteMapping("/{mateId}/favorite")
    ResponseEntity<Response<Void>> removeFavoriteMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 자주 찾는 메이트 목록 조회
    @Operation(
            summary = "자주 찾는 메이트 목록 조회",
            description = "내 자주 찾는 메이트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자주 찾는 메이트 목록 조회 성공")
    })
    @GetMapping("/favorite")
    ResponseEntity<Response<List<MateResDTO.FavoriteMateResDTO>>> getFavoriteMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    // 오늘의 공연 메이트
    @Operation(
            summary = "오늘의 공연 메이트",
            description = "오늘 공연에 함께하는 메이트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "오늘의 공연 메이트 조회 성공")
    })
    @GetMapping("/today")
    ResponseEntity<Response<List<TodayMateResDTO>>> getTodayMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    // 메이트 프로필 조회
    @Operation(
            summary = "메이트 프로필 조회",
            description = "메이트의 프로필을 조회합니다. 해당 메이트와의 관계 ID가 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "메이트가 존재하지 않습니다.")
    })
    @GetMapping("/{mateId}/profile")
    ResponseEntity<Response<MateResDTO.MateProfileResDTO>> getMateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

}
