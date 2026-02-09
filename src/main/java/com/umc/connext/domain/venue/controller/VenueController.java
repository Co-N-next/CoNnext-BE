package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.converter.VenueConverter;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.projection.SimpleVenue;
import com.umc.connext.domain.venue.service.VenueService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
<<<<<<< feat/favorite-venues
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
=======
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
>>>>>>> dev

import java.util.List;
import java.util.Optional;

@Tag(name = "Venue", description = "공연장 관련 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController implements VenueControllerDocs {

    private final VenueService venueService;

    // 공연장 검색
    @GetMapping("/search")
    @Override
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
    @Override
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearchVenues() {
        List<VenueResDTO.VenuePreviewDTO> result = venueService.trendSearchVenues();

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "인기 검색 공연장 조회 성공"));
    }

<<<<<<< feat/favorite-venues
    // 공연장 즐겨찾기 등록
    @PostMapping("/favorites/{venueId}")
    @Override
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> addFavoriteVenue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long venueId
    ){
        Long memberId = userDetails.getMemberId();
        return ResponseEntity.ok().body(Response.success(SuccessCode.INSERT_SUCCESS,
                venueService.addFavoriteVenue(memberId, venueId),
                "즐겨찾기 공연장 등록 성공"));
    }

    // 공연장 즐겨찾기 삭제
    @DeleteMapping("/favorites/{venueId}")
    @Override
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> deleteFavoriteVenue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long venueId
    ){
        Long memberId = userDetails.getMemberId();

        venueService.deleteFavoriteVenue(memberId, venueId);
        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "즐겨찾기 공연장 삭제 성공"));
    }

    // 공연장 즐겨찾기 목록 조회
    @GetMapping("/favorites")
    @Override
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> favoriteVenues(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long memberId = userDetails.getMemberId();

        List<VenueResDTO.VenuePreviewDTO> result = venueService.favoriteVenues(memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "즐겨찾기 공연장 조회 성공"));
=======
    // 근처 공연장 조회
    @GetMapping("/nearby")
    @Override
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> nearbyVenue(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
            @RequestParam(defaultValue = "500", required = false) @Min(1) @Max(5000) Integer radius
    ) {
        // 근처 공연장 조회
        Optional<SimpleVenue> result = venueService.nearbyVenue(lat, lng, radius);

        // 근처에 공연장이 없을 경우
        if (result.isEmpty()) {
            return ResponseEntity.ok().body(Response.success(SuccessCode.NO_CONTENT));
        }

        // 근처에 공연장이 있는 경우
        VenueResDTO.VenueSimpleDTO dto = VenueConverter.toVenueSimpleDTO(result.get());
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, dto, "근처 공연장 조회 성공"));

>>>>>>> dev
    }

}
