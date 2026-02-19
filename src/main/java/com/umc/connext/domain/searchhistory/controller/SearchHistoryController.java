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

import java.util.List;

@Tag(name = "SearchHistory", description = "검색 관련 API")
@RestController
@RequestMapping("/searchHistory")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    private static final Long TEMP_MEMBER_ID = 1L;

    // 최근 검색어 조회
    @GetMapping
    public ResponseEntity<Response<List<SearchHistoryResponseDTO>>> getSearchHistory(
            @RequestParam SearchType type
    ) {
        List<SearchHistoryResponseDTO> result =
                searchHistoryService.getSearchHistory(TEMP_MEMBER_ID, type);

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
            @Valid @RequestBody SearchHistoryCreateRequestDTO request
    ) {
        searchHistoryService.addSearchHistory(TEMP_MEMBER_ID, request);

        return ResponseEntity.ok(
                Response.success(SuccessCode.OK, "최근 검색어 저장 성공")
        );
    }

    // 검색어 한개 삭제
    @DeleteMapping("/{searchHistoryId}")
    public ResponseEntity<Response<Void>> deleteSearchHistory(
            @PathVariable Long searchHistoryId
    ) {
        searchHistoryService.deleteSearchHistory(TEMP_MEMBER_ID, searchHistoryId);

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
            @RequestParam SearchType type
    ) {
        searchHistoryService.deleteAllSearchHistory(TEMP_MEMBER_ID, type);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.DELETE_SUCCESS,
                        "최근 검색어 전체 삭제 성공"
                )
        );
    }
}
