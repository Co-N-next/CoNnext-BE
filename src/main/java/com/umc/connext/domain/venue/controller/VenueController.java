package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.converter.VenueConverter;
import com.umc.connext.domain.venue.dto.VenueLayoutResponse;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.projection.SimpleVenue;
import com.umc.connext.domain.venue.service.VenueService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Venue", description = "공연장 관련 API")
@Validated
@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController implements VenueControllerDocs {

    private final VenueService venueService;

    @Override
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
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result.getContent(), "공연장 검색 성공"));
    }

    @Override
    @GetMapping("/trend-search")
    @Override
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearchVenues() {
        List<VenueResDTO.VenuePreviewDTO> result = venueService.trendSearchVenues();
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "인기 검색 공연장 조회 성공"));
    }

    @Override
    @GetMapping("/")
    public ResponseEntity<Response<List<VenueResDTO.VenueHomeDTO>>> getPopularVenues() {
        List<VenueResDTO.VenueHomeDTO> result = venueService.getPopularVenues();
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "홈 화면 공연장 조회 성공"));
    }

    @Override
    @GetMapping("/{venueId}/map")
    public ResponseEntity<Response<VenueResDTO.VenueMapResponse>> getVenueMap(@PathVariable Long venueId) {
        VenueResDTO.VenueMapResponse mapData = venueService.getVenueMap(venueId);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, mapData));
    }

    @Override
    @GetMapping("/{venueId}/facilities")
    public ResponseEntity<Response<List<VenueResDTO.FacilityDto>>> getVenueFacilities(@PathVariable Long venueId) {
        List<VenueResDTO.FacilityDto> facilities = venueService.getVenueFacilities(venueId);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, facilities));
    }

    @Override
    @PostMapping("/favorites/{venueId}")
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> addFavoriteVenue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long venueId
    ) {
        Long memberId = userDetails.getMemberId();
        return ResponseEntity.ok().body(Response.success(SuccessCode.INSERT_SUCCESS,
                venueService.addFavoriteVenue(memberId, venueId),
                "즐겨찾기 공연장 등록 성공"));
    }

    @Override
    @DeleteMapping("/favorites/{venueId}")
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> deleteFavoriteVenue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long venueId
    ) {
        Long memberId = userDetails.getMemberId();
        venueService.deleteFavoriteVenue(memberId, venueId);
        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "즐겨찾기 공연장 삭제 성공"));
    }

    @Override
    @GetMapping("/favorites")
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> favoriteVenues(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<VenueResDTO.VenuePreviewDTO> result = venueService.favoriteVenues(memberId);
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "즐겨찾기 공연장 조회 성공"));
    }

    @Override
    @GetMapping("/nearby")
    public ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> nearbyVenue(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "500", required = false) Integer radius
    ) {
        Optional<SimpleVenue> result = venueService.nearbyVenue(lat, lng, radius);

        if (result.isEmpty()) {
            return ResponseEntity.ok().body(Response.success(SuccessCode.NO_CONTENT));
        }

        VenueResDTO.VenueSimpleDTO dto = VenueConverter.toVenueSimpleDTO(result.get());
        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, dto, "근처 공연장 조회 성공"));
    }

    @Operation(
            summary = "공연장 레이아웃 조회",
            description = "공연장의 SVG 레이아웃 정보를 조회합니다. 섹션, 시설물 좌표를 포함하여 프론트엔드에서 SVG 렌더링 및 경로 오버레이에 사용됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VenueLayoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없음")
    })
    @GetMapping("/{venueId}/layout")
    public ResponseEntity<Response<VenueLayoutResponse>> getVenueLayout(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,

            @Parameter(description = "특정 층만 조회 (미지정 시 전체)", example = "1")
            @RequestParam(required = false) Integer floor
    ) {
        VenueLayoutResponse layout = venueService.getVenueLayout(venueId, floor);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, layout));
    }

    @Operation(
            summary = "공연장 기본 정보 조회",
            description = "공연장의 이름, 주소, SVG 크기 등 기본 정보만 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없음")
    })
    @GetMapping("/{venueId}")
    public ResponseEntity<Response<VenueLayoutResponse.VenueInfo>> getVenueInfo(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    ) {
        VenueLayoutResponse.VenueInfo info = venueService.getVenueInfo(venueId);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, info));
    }
}