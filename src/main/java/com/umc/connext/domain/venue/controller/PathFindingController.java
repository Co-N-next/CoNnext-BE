package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.PathFindingRequest;
import com.umc.connext.domain.venue.dto.PathFindingResponse;
import com.umc.connext.domain.venue.service.PathFindingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Path Finding", description = "공연장 내 경로 탐색 API")
@RestController
@RequestMapping("/venues/{venueId}/pathfinding")
@RequiredArgsConstructor
public class PathFindingController {

    private final PathFindingService pathFindingService;

    @Operation(
            summary = "경로 찾기 (POST)",
            description = "출발/도착 좌표 및 층 정보를 RequestBody로 받아 경로를 탐색합니다. 같은 층/다른 층 여부는 서버에서 자동 판단합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경로 탐색 성공",
                    content = @Content(schema = @Schema(implementation = PathFindingResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류(Validation 실패 등)"),
            @ApiResponse(responseCode = "404", description = "공연장 또는 경로 탐색 대상 리소스 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/path")
    public ResponseEntity<Response<PathFindingResponse>> findPath(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @RequestBody(
                    description = "경로 탐색 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PathFindingRequest.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PathFindingRequest request
    ) {
        PathFindingResponse pathResponse = pathFindingService.findPath(venueId, request);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, pathResponse));
    }

    @Operation(
            summary = "경로 찾기 (GET)",
            description = "출발/도착 좌표 및 층 정보를 Query Parameter로 받아 경로를 탐색합니다. 같은 층/다른 층 여부는 서버에서 자동 판단합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경로 탐색 성공",
                    content = @Content(schema = @Schema(implementation = PathFindingResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류(파라미터 누락/형식 오류 등)"),
            @ApiResponse(responseCode = "404", description = "공연장 또는 경로 탐색 대상 리소스 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/path")
    public ResponseEntity<Response<PathFindingResponse>> findPathByParams(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,

            @Parameter(description = "출발 X 좌표", example = "123.45", required = true)
            @RequestParam BigDecimal startX,
            @Parameter(description = "출발 Y 좌표", example = "678.90", required = true)
            @RequestParam BigDecimal startY,
            @Parameter(description = "출발 층", example = "1", required = true)
            @RequestParam Integer startFloor,

            @Parameter(description = "도착 X 좌표", example = "223.45", required = true)
            @RequestParam BigDecimal endX,
            @Parameter(description = "도착 Y 좌표", example = "778.90", required = true)
            @RequestParam BigDecimal endY,
            @Parameter(description = "도착 층", example = "2", required = true)
            @RequestParam Integer endFloor
    ) {
        PathFindingResponse pathResponse = pathFindingService.findPath(
                venueId, startX, startY, startFloor, endX, endY, endFloor
        );
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, pathResponse));
    }

    @Operation(
            summary = "좌표에서 특정 시설물까지 경로 찾기",
            description = "출발 좌표/층과 시설물 ID를 받아 해당 시설물까지의 경로를 탐색합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경로 탐색 성공",
                    content = @Content(schema = @Schema(implementation = PathFindingResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류(파라미터 누락/형식 오류 등)"),
            @ApiResponse(responseCode = "404", description = "공연장 또는 시설물 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/to-facility/{facilityId}")
    public ResponseEntity<Response<PathFindingResponse>> findPathToFacility(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,

            @Parameter(description = "시설물 ID", example = "10", required = true)
            @PathVariable Long facilityId,

            @Parameter(description = "출발 X 좌표", example = "123.45", required = true)
            @RequestParam BigDecimal startX,
            @Parameter(description = "출발 Y 좌표", example = "678.90", required = true)
            @RequestParam BigDecimal startY,
            @Parameter(description = "출발 층", example = "1", required = true)
            @RequestParam Integer startFloor
    ) {
        PathFindingResponse pathResponse = pathFindingService.findPathToFacility(
                venueId, startX, startY, startFloor, facilityId
        );
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, pathResponse));
    }
}
