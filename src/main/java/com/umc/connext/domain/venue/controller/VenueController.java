package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.PageInfo;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.service.VenueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Venue", description = "공연장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController implements VenueControllerDocs{

    private final VenueService venueService;

    // 공연장 검색
    @GetMapping("/search")
    public ResponseEntity<Response<VenueResDTO.VenueSearchDTO>> searchVenues(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) Integer page
    ) {
        Page<VenueResDTO.VenuePreviewDTO> result = venueService.searchVenues(query, page);

        PageInfo pageInfo = new PageInfo(
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        VenueResDTO.VenueSearchDTO searchData = new VenueResDTO.VenueSearchDTO(result.getContent(), pageInfo);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, searchData, "공연장 검색 성공"));
    }

    // 인기 검색 공연장 조회
    @GetMapping("/trend-search")
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearchVenues() {
        List<VenueResDTO.VenuePreviewDTO> result = venueService.trendSearchVenues();

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "인기 검색 공연장 조회 성공"));
    }

}
