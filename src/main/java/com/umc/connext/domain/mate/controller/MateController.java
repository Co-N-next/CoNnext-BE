package com.umc.connext.domain.mate.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.mate.dto.MateReqDTO;
import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.domain.mate.dto.TodayMateResDTO;
import com.umc.connext.domain.mate.service.MateService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mate", description = "메이트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mates")
@Validated
public class MateController implements MateControllerDocs {

    private final MateService mateService;

    // 메이트 요청
    @PostMapping("/request")
    @Override
    public ResponseEntity<Response<MateResDTO.MateRequestResDTO>> requestMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MateReqDTO.MateRequestDTO dto
    ) {
        Long requesterId = userDetails.getMemberId();
        Long addresseeId = dto.addresseeId();

        MateResDTO.MateRequestResDTO result = mateService.sendMateRequest(requesterId, addresseeId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.CREATED, result, "메이트 요청 성공"));
    }

    // 메이트 요청 수락
    @PostMapping("/{mateId}/accept")
    @Override
    public ResponseEntity<Response<Void>> acceptMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.acceptMateRequest(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.UPDATE_SUCCESS, "메이트 요청 수락 성공"));
    }

    // 메이트 요청 거절
    @PostMapping("/{mateId}/reject")
    @Override
    public ResponseEntity<Response<Void>> rejectMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.rejectMateRequest(mateId, memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.UPDATE_SUCCESS, "메이트 요청 거절 성공"));
    }

    // 메이트 목록 조회
    @GetMapping("")
    @Override
    public ResponseEntity<Response<List<MateResDTO.MateListResDTO>>> getMyMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<MateResDTO.MateListResDTO> result = mateService.getMyMates(memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "메이트 목록 조회 성공"));
    }

    // 메이트 삭제
    @DeleteMapping("/{mateId}")
    @Override
    public ResponseEntity<Response<Void>> deleteMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.deleteMate(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "메이트 삭제 성공"));
    }

    // 메이트 검색
    @Override
    @GetMapping("/search")
    public ResponseEntity<Response<List<MateResDTO.MateSearchResDTO>>> searchMates(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page
    ) {
        Page<MateResDTO.MateSearchResDTO> result = mateService.searchMates(
                userDetails.getMemberId(), keyword, page
        );
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "메이트 검색 성공"));
    }

    // ============== 자주 찾는 메이트 ==============

    // 자주 찾는 메이트 추가
    @PostMapping("/{mateId}/favorite")
    @Override
    public ResponseEntity<Response<Void>> addFavoriteMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.addFavoriteMate(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.CREATED, "메이트 즐겨찾기 추가 성공"));
    }

    // 자주 찾는 메이트 제거
    @DeleteMapping("/{mateId}/favorite")
    @Override
    public ResponseEntity<Response<Void>> removeFavoriteMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.removeFavoriteMate(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "메이트 즐겨찾기 제거 성공"));
    }

    // 자주 찾는 메이트 목록 조회
    @GetMapping("/favorite")
    @Override
    public ResponseEntity<Response<List<MateResDTO.FavoriteMateResDTO>>> getFavoriteMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<MateResDTO.FavoriteMateResDTO> result = mateService.getFavoriteMates(memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "즐겨찾기 메이트 목록 조회 성공"));
    }

    // ==========================================

    // 오늘의 공연 메이트
    @GetMapping("/today")
    @Override
    public ResponseEntity<Response<List<TodayMateResDTO>>> getTodayMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<TodayMateResDTO> result = mateService.getTodayMates(memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "오늘의 공연 메이트 목록 조회 성공"));
    }

    // 메이트 프로필 조회
    @GetMapping("/{mateId}/profile")
    @Override
    public ResponseEntity<Response<MateResDTO.MateProfileResDTO>> getMateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ){
        Long memberId = userDetails.getMemberId();
        MateResDTO.MateProfileResDTO result = mateService.getMateProfile(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "메이트 프로필 조회 성공"));
    }
}
