package com.umc.connext.domain.searchhistory.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryCreateRequest;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryResponse;
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

    @Operation(
            summary = "최근 검색어 가져오기",
            description = "최근 검색어를 최대 7개까지 가져옵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 검색어 가져오기 성공")
    })
    @GetMapping
    public ResponseEntity<Response<List<SearchHistoryResponse>>> getSearchHistory(
            @RequestParam SearchType type
    ) {
        Long memberId = getMemberId();

        List<SearchHistoryResponse> result =
                searchHistoryService.getSearchHistory(memberId, type);

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
    @ApiResponses({@ApiResponse(responseCode = "200", description = "검색어 저장하기 성공")})
    @PostMapping
    public ResponseEntity<Response<Void>> addSearchHistory(
            @Valid @RequestBody SearchHistoryCreateRequest request
    ) {
        Long memberId = getMemberId();
        searchHistoryService.addSearchHistory(memberId, request);
        return ResponseEntity.ok(
                Response.successVoid(
                        SuccessCode.OK,
                        "최근 검색어 저장 성공"
                )
        );
    }

    @Operation(
            summary = "검색어 한개 삭제하기",
            description = "최근 검색어 중 한개를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 검색어 한개 삭제 성공")
    })
    @DeleteMapping("/{searchHistoryId}")
    public ResponseEntity<Response<Void>> deleteSearchHistory(
            @PathVariable Long searchHistoryId
    ) {
        Long memberId = getMemberId();
        searchHistoryService.deleteSearchHistory(memberId, searchHistoryId);
        return ResponseEntity.ok(
                Response.successVoid(
                        SuccessCode.DELETE_SUCCESS,
                        "최근 검색어 한개 삭제 성공"
                )
        );
    }

    @Operation(
            summary = "검색어 전체 삭제하기",
            description = "최근 검색어에 있는 모든 검색어를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 검색어 전체 삭제 성공")
    })
    @DeleteMapping("/all")
    public ResponseEntity<Response<Void>> deleteAllSearchHistory(
            @RequestParam SearchType type
    ) {
        Long memberId = getMemberId();
        searchHistoryService.deleteAllSearchHistory(memberId, type);
        return ResponseEntity.ok(
                Response.successVoid(
                        SuccessCode.DELETE_SUCCESS,
                        "최근 검색어 전체 삭제 성공"
                )
        );
    }

    private Long getMemberId() {
        return 1L;
    }
}
