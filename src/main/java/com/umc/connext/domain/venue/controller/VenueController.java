package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController implements VenueControllerDocs{

    private final VenueService venueService;
    // 인기 검색 공연장 조회
    @GetMapping("/trend-search")
    public ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearch() {
        List<VenueResDTO.VenuePreviewDTO> result = venueService.trendSearch();

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "인기 검색 공연장 조회 성공"));
    }

}
