package com.umc.connext.domain.searchhistory.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryCreateRequestDTO;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryResponseDTO;
import com.umc.connext.domain.searchhistory.entity.SearchType;
import com.umc.connext.domain.searchhistory.service.SearchHistoryService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "SearchHistory", description = "검색 관련 API")
@RestController
@RequestMapping("/searchHistory")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    // 최근 검색어 조회
    @GetMapping
    public ResponseEntity<Response<List<SearchHistoryResponseDTO>>> getSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam SearchType type
    ) {

        Long memberId = user.getMemberId();

        List<SearchHistoryResponseDTO> result =
                searchHistoryService.getSearchHistory(memberId, type);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "최근 검색어 조회 성공"
                )
        );
    }

    // 검색어 저장
    @PostMapping
    public ResponseEntity<Response<Void>> addSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody SearchHistoryCreateRequestDTO request
    ) {

        Long memberId = user.getMemberId();

        searchHistoryService.addSearchHistory(memberId, request);

        return ResponseEntity.ok(
                Response.success(SuccessCode.OK, "최근 검색어 저장 성공")
        );
    }

    // 검색어 한개 삭제
    @DeleteMapping("/{searchHistoryId}")
    public ResponseEntity<Response<Void>> deleteSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long searchHistoryId
    ) {

        Long memberId = user.getMemberId();

        searchHistoryService.deleteSearchHistory(memberId, searchHistoryId);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.DELETE_SUCCESS,
                        "최근 검색어 한개 삭제 성공"
                )
        );
    }

    // 검색어 전체 삭제
    @DeleteMapping("/all")
    public ResponseEntity<Response<Void>> deleteAllSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam SearchType type
    ) {

        Long memberId = user.getMemberId();

        searchHistoryService.deleteAllSearchHistory(memberId, type);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.DELETE_SUCCESS,
                        "최근 검색어 전체 삭제 성공"
                )
        );
    }
}
