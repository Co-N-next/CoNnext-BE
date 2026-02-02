package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.entity.VenueFloorConfig;
import com.umc.connext.domain.venue.repository.VenueFloorConfigRepository;
import com.umc.connext.domain.venue.repository.VenueRepository;
import com.umc.connext.domain.venue.service.FloorMappingService;
import com.umc.connext.domain.venue.service.SvgParserService;
import com.umc.connext.domain.venue.service.VenueDataInitService;
import com.umc.connext.domain.venue.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin Venue", description = "공연장 관리자 API (공연장 CRUD, 층 설정, SVG 업로드/초기화)")
@RestController
@RequestMapping("/api/admin/venues")
@RequiredArgsConstructor
public class VenueAdminController {

    private final VenueRepository venueRepository;
    private final VenueFloorConfigRepository floorConfigRepository;
    private final FloorMappingService floorMappingService;
    private final VenueDataInitService venueDataInitService;
    private final SvgParserService svgParserService;

    @Operation(summary = "공연장 목록 조회", description = "등록된 공연장 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Venue.class)))
    })
    @GetMapping
    public ResponseEntity<Response<List<Venue>>> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, venues));
    }

    @Operation(summary = "공연장 등록", description = "공연장을 신규 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = Venue.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    @PostMapping
    public ResponseEntity<Response<Venue>> createVenue(
            @RequestBody(
                    description = "공연장 생성 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VenueRequest.VenueCreateRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody VenueRequest.VenueCreateRequest request
    ) {
        Venue venue = Venue.builder()
                .name(request.name())
                .address(request.address())
                .totalFloors(request.totalFloors() != null ? request.totalFloors() : 1)
                .build();

        Venue savedVenue = venueRepository.save(venue);
        return ResponseEntity.ok(Response.success(SuccessCode.INSERT_SUCCESS, savedVenue));
    }

    @Operation(summary = "공연장 단건 조회", description = "venueId로 공연장 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Venue.class))),
            @ApiResponse(responseCode = "404", description = "공연장 없음")
    })
    @GetMapping("/{venueId}")
    public ResponseEntity<Response<Venue>> getVenue(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    ) {
        return venueRepository.findById(venueId)
                .map(venue -> ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, venue)))
                .orElseGet(() -> ResponseEntity.ok(Response.fail(ErrorCode.NOT_FOUND)));
    }

    @Operation(summary = "공연장 수정", description = "공연장 정보를 부분 수정합니다(null 필드는 변경하지 않음).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = Venue.class))),
            @ApiResponse(responseCode = "404", description = "공연장 없음"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    @PutMapping("/{venueId}")
    public ResponseEntity<Response<Venue>> updateVenue(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @RequestBody(
                    description = "공연장 수정 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VenueRequest.VenueUpdateRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody VenueRequest.VenueUpdateRequest request
    ) {
        return venueRepository.findById(venueId)
                .map(venue -> {
                    if (request.name() != null) venue.setName(request.name());
                    if (request.address() != null) venue.setAddress(request.address());
                    if (request.totalFloors() != null) venue.setTotalFloors(request.totalFloors());
                    if (request.isActive() != null) venue.setIsActive(request.isActive());
                    return ResponseEntity.ok(Response.success(SuccessCode.UPDATE_SUCCESS, venueRepository.save(venue)));
                })
                .orElseGet(() -> ResponseEntity.ok(Response.fail(ErrorCode.NOT_FOUND)));
    }

    @Operation(
            summary = "SVG 업로드로 공연장 데이터 초기화",
            description = "SVG를 업로드하여 섹션/시설물/층 설정 데이터를 초기화합니다. clearExisting=true이면 기존 층 설정을 먼저 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초기화 성공",
                    content = @Content(schema = @Schema(implementation = VenueDataInitService.InitResult.class))),
            @ApiResponse(responseCode = "400", description = "초기화 실패(파싱 실패/유효성 오류 등)",
                    content = @Content(schema = @Schema(implementation = VenueDataInitService.InitResult.class)))
    })
    @PostMapping(value = "/{venueId}/upload-svg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<VenueDataInitService.InitResult>> uploadSvg(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @Parameter(description = "SVG 파일", required = true)
            @RequestParam("file") MultipartFile svgFile,
            @Parameter(description = "기존 데이터 삭제 여부", example = "false")
            @RequestParam(value = "clearExisting", defaultValue = "false") boolean clearExisting
    ) {
        if (clearExisting) {
            floorMappingService.removeAllFloorConfigs(venueId);
        }

        VenueDataInitService.InitResult result = venueDataInitService.initializeFromSvg(venueId, svgFile);
        if (result.isSuccess()) {
            return ResponseEntity.ok(Response.success(SuccessCode.INSERT_SUCCESS, result));
        } else {
            return ResponseEntity.ok(Response.fail(ErrorCode.BAD_REQUEST));
        }
    }

    @Operation(summary = "SVG 미리보기(저장 없음)", description = "SVG를 파싱만 수행하고 DB 저장 없이 파싱 결과를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파싱 성공",
                    content = @Content(schema = @Schema(implementation = SvgParserService.ParseResult.class))),
            @ApiResponse(responseCode = "400", description = "파싱 실패")
    })
    @PostMapping(value = "/{venueId}/preview-svg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<SvgParserService.ParseResult>> previewSvg(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @Parameter(description = "SVG 파일", required = true)
            @RequestParam("file") MultipartFile svgFile
    ) {
        try {
            SvgParserService.ParseResult result = svgParserService.parseAll(svgFile.getInputStream(), venueId);
            return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, result));
        } catch (Exception e) {
            return ResponseEntity.ok(Response.fail(ErrorCode.BAD_REQUEST));
        }
    }

    @Operation(summary = "층 설정 목록 조회", description = "venueId에 해당하는 층 설정 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VenueFloorConfig.class)))
    })
    @GetMapping("/{venueId}/floor-config")
    public ResponseEntity<Response<List<VenueFloorConfig>>> getFloorConfigs(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    ) {
        List<VenueFloorConfig> configs = floorConfigRepository.findAllByVenueId(venueId);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, configs));
    }

    @Operation(summary = "층 설정 단건 추가/수정", description = "섹션(sectionId)에 대해 층(floor) 및 설명(description)을 저장/갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = VenueFloorConfig.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    @PostMapping("/{venueId}/floor-config")
    public ResponseEntity<Response<VenueFloorConfig>> setFloorConfig(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @RequestBody(
                    description = "층 설정 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VenueRequest.FloorConfigRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody VenueRequest.FloorConfigRequest request
    ) {
        VenueFloorConfig config = floorMappingService.setFloor(
                venueId, request.sectionId(), request.floor(), request.description()
        );
        return ResponseEntity.ok(Response.success(SuccessCode.INSERT_SUCCESS, config));
    }

    @Operation(summary = "층 설정 일괄 추가/수정", description = "여러 섹션의 층 설정을 한 번에 저장/갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = VenueFloorConfig.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    @PostMapping("/{venueId}/floor-config/batch")
    public ResponseEntity<Response<List<VenueFloorConfig>>> setFloorConfigsBatch(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @RequestBody(
                    description = "층 설정 요청 리스트",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VenueRequest.FloorConfigRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody List<VenueRequest.FloorConfigRequest> requests
    ) {
        Map<String, Integer> sectionFloorMap = new java.util.HashMap<>();
        for (VenueRequest.FloorConfigRequest req : requests) {
            sectionFloorMap.put(req.sectionId(), req.floor());
        }
        List<VenueFloorConfig> configs = floorMappingService.setFloorsBatch(venueId, sectionFloorMap);
        return ResponseEntity.ok(Response.success(SuccessCode.INSERT_SUCCESS, configs));
    }

    @Operation(summary = "층 설정 단건 삭제", description = "특정 sectionId의 층 설정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "대상 없음")
    })
    @DeleteMapping("/{venueId}/floor-config/{sectionId}")
    public ResponseEntity<Response<Void>> removeFloorConfig(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @Parameter(description = "섹션 ID", example = "A", required = true)
            @PathVariable String sectionId
    ) {
        floorMappingService.removeFloorConfig(venueId, sectionId);
        return ResponseEntity.ok(Response.<Void>success(SuccessCode.DELETE_SUCCESS));
    }

    @Operation(summary = "층 설정 전체 삭제", description = "venueId의 모든 층 설정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공")
    })
    @DeleteMapping("/{venueId}")
    public ResponseEntity<Response<Void>> deleteVenue(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    ) {
        return venueRepository.findById(venueId)
                .map(venue -> {
                    venueRepository.delete(venue);
                    return ResponseEntity.ok(Response.<Void>success(SuccessCode.DELETE_SUCCESS));
                })
                .orElseGet(() -> ResponseEntity.ok(Response.<Void>fail(ErrorCode.NOT_FOUND)));
    }

    @Operation(summary = "특정 층의 섹션 목록 조회", description = "venueId와 floor에 해당하는 층 설정 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VenueFloorConfig.class)))
    })
    @GetMapping("/{venueId}/floor-config/by-floor/{floor}")
    public ResponseEntity<Response<List<VenueFloorConfig>>> getFloorConfigsByFloor(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId,
            @Parameter(description = "층 번호", example = "1", required = true)
            @PathVariable Integer floor
    ) {
        List<VenueFloorConfig> configs = floorConfigRepository.findAllByVenueIdAndFloor(venueId, floor);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, configs));
    }

    @Operation(summary = "공연장 층 목록 조회", description = "해당 공연장에서 사용 중인 모든 층 번호 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{venueId}/floors")
    public ResponseEntity<Response<List<Integer>>> getFloors(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    ) {
        List<Integer> floors = floorMappingService.getFloors(venueId);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, floors));
    }

}
