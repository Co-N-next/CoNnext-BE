package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.service.VenueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Venue", description = "공연장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController implements VenueControllerDocs{

    private final VenueService venueService;

    // 공연장 검색
    @GetMapping("/search")
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> searchVenues(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") Integer page
    ) {
        if (query.trim().isEmpty()) {
            throw new GeneralException(ErrorCode.INVALID_SEARCH_KEYWORD, "검색어는 공백일 수 없습니다.");
        }
        if (page < 0) {
            throw new GeneralException(ErrorCode.INVALID_PAGE_REQUEST, "Page는 0 이상이어야 합니다.");
        }

        Page<VenueResDTO.VenuePreviewDTO> result = venueService.searchVenues(query, page);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "공연장 검색 성공"));
    }

    // 인기 검색 공연장 조회
    @GetMapping("/trend-search")
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearchVenues() {
        List<VenueResDTO.VenuePreviewDTO> result = venueService.trendSearchVenues();

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "인기 검색 공연장 조회 성공"));
    }

    // 공연장 즐겨찾기 등록
    @PostMapping("/favorites/{venueId}")
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> addFavoriteVenue(
            @PathVariable Long venueId
    ){
        Long memberId = 1L; // 임시 회원 (추후 삭제)
        return ResponseEntity.ok().body(Response.success(SuccessCode.INSERT_SUCCESS,
                venueService.addFavoriteVenue(memberId, venueId),
                "즐겨찾기 공연장 등록 성공"));
    }

    // 공연장 즐겨찾기 삭제
    @DeleteMapping("/favorites/{venueId}")
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> deleteFavoriteVenue(
            @PathVariable Long venueId
    ){
        Long memberId = 1L; // 임시 회원 (추후 삭제)

        venueService.deleteFavoriteVenue(memberId, venueId);
        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "즐겨찾기 공연장 삭제 성공"));
    }

    // 공연장 즐겨찾기 목록 조회
    @GetMapping("/favorites")
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> favoriteVenues(){
        Long memberId = 1L; // 임시 회원

        List<VenueResDTO.VenuePreviewDTO> result = venueService.favoriteVenues(memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "즐겨찾기 공연장 조회 성공"));
    }

}
