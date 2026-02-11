package com.umc.connext.domain.searchhistory.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryCreateRequestDTO;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryResponseDTO;
import com.umc.connext.domain.searchhistory.entity.SearchType;
import com.umc.connext.domain.searchhistory.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@Tag(name = "SearchHistory", description = "검색 관련 API")
@RestController
@RequestMapping("/searchHistory")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @Operation(
            summary = "최근 검색어 가져오기",
            description = "최근 검색어를 최대 7개까지 가져옵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 검색어 가져오기 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<Response<List<SearchHistoryResponseDTO>>> getSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam SearchType type
    ) {
        List<SearchHistoryResponseDTO> result =
                searchHistoryService.getSearchHistory(user.getMemberId(), type);

        return ResponseEntity.ok()
                .body(Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "최근 검색어 조회 성공"
                ));
    }

    @Operation(
            summary = "검색어 저장하기",
            description = "검색창에 입력한 검색어를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색어 저장하기 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    public ResponseEntity<Response<Void>> addSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody SearchHistoryCreateRequestDTO request
    ) {
        searchHistoryService.addSearchHistory(user.getMemberId(), request);
        return ResponseEntity.ok(Response.success(SuccessCode.OK, "최근 검색어 저장 성공"));
    }

    @Operation(
            summary = "검색어 한개 삭제하기",
            description = "최근 검색어 중 한개를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 검색어 한개 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "검색 기록이 존재하지 않음")
    })
    @DeleteMapping("/{searchHistoryId}")
    public ResponseEntity<Response<Void>> deleteSearchHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long searchHistoryId
    ) {
        searchHistoryService.deleteSearchHistory(user.getMemberId(), searchHistoryId);
        return ResponseEntity.ok(Response.success(SuccessCode.DELETE_SUCCESS, "최근 검색어 한개 삭제 성공"));
    }

    @Operation(
            summary = "검색어 전체 삭제하기",
            description = "최근 검색어에 있는 모든 검색어를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 검색어 전체 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
    })
    @DeleteMapping("/all")
    public ResponseEntity<Response<Void>> deleteAllSearchHistory(
            @RequestParam SearchType type,
            @AuthenticationPrincipal CustomUserDetails user
            ) {
        searchHistoryService.deleteAllSearchHistory(user.getMemberId(), type);
        return ResponseEntity.ok(Response.success(SuccessCode.DELETE_SUCCESS, "최근 검색어 전체 삭제 성공"));
    }

}
