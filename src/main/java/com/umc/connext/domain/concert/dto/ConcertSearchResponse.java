package com.umc.connext.domain.concert.dto;

import com.umc.connext.common.response.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@Schema(description = "공연 검색 응답")
public class ConcertSearchResponse {

    @Schema(description = "공연 목록")
    private List<ConcertResponse> concerts;

    @Schema(description = "페이지 정보")
    private PageInfo pageInfo;

    public static ConcertSearchResponse from(Page<ConcertResponse> page) {
        return ConcertSearchResponse.builder()
                .concerts(page.getContent())
                .pageInfo(new PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.hasNext(),
                        page.getTotalElements(),
                        page.getTotalPages()
                ))
                .build();
    }
}