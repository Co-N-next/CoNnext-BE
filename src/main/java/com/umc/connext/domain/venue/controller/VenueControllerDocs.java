package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/venues")
public interface VenueControllerDocs {

    @Operation(
            summary = "공연장 검색",
            description = "검색어를 포함하는 공연장을 조회하여 Pagination으로 제공합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색어를 다시 확인해주세요")
    })
    @GetMapping("/search")
    ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> searchVenues(
            @Parameter(description = "검색 키워드", example = "올림픽", required = true)
            @RequestParam String query,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page
    );

    @Operation(
            summary = "인기 검색 공연장 조회",
            description = "검색 횟수가 가장 많은 공연장 5개를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/trend-search")
    ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearchVenues();

    @Operation(
            summary = "공연장 전체 맵 데이터 조회",
            description = "공연장 지도 데이터를 조회합니다. (층별 구역 좌표 + 시설물 좌표) 일반적으로 최초 1회 호출 후 캐싱하여 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VenueResDTO.VenueMapResponse.class))),
            @ApiResponse(responseCode = "404", description = "공연장 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{venueId}/map")
    ResponseEntity<Response<VenueResDTO.VenueMapResponse>> getVenueMap(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    );

    @Operation(
            summary = "공연장 시설물(편의시설) 전체 조회",
            description = "특정 공연장의 모든 시설물(화장실, 엘리베이터, 계단, 매점 등) 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VenueResDTO.FacilityDto.class))),
            @ApiResponse(responseCode = "404", description = "공연장 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{venueId}/facilities")
    ResponseEntity<Response<List<VenueResDTO.FacilityDto>>> getVenueFacilities(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    );

    @Operation(
            summary = "즐겨찾기 공연장 등록",
            description = "해당 공연장을 즐겨찾기 목록에 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없습니다.")
    })
    @PostMapping("/favorites/{venueId}")
    ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> addFavoriteVenue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    );

    @Operation(
            summary = "즐겨찾기 공연장 삭제",
            description = "해당 공연장을 즐겨찾기 목록에서 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    @DeleteMapping("/favorites/{venueId}")
    ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> deleteFavoriteVenue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    );

    @Operation(
            summary = "즐겨찾기 공연장 목록 조회",
            description = "사용자가 즐겨찾기에 추가한 공연장 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/favorites")
    ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> favoriteVenues(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "근처 공연장 조회",
            description = "사용자의 위도와 경도를 이용하여 근처 공연장 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "근처에 공연장이 없습니다.")
    })
    @GetMapping("/nearby")
    ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> nearbyVenue(
            @Parameter(description = "위도", example = "37.5665", required = true)
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
            @Parameter(description = "경도", example = "126.9780", required = true)
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
            @Parameter(description = "검색 반경 (미터)", example = "500")
            @RequestParam(defaultValue = "500", required = false) @Min(1) @Max(5000) Integer radius
    );
}