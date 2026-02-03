package com.umc.connext.domain.announcement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.umc.connext.domain.announcement.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import com.umc.connext.common.response.Response;
import com.umc.connext.common.code.SuccessCode;

import com.umc.connext.domain.announcement.dto.response.AnnouncementPageResponse;

@Tag(name = "Notifications-NOTICES", description = "공지사항 알림 관련 API")
@RestController
@RequestMapping("/notifications/notices")
@RequiredArgsConstructor

public class AnnouncementController {
    private final AnnouncementService announcementService;

    @Operation(
            summary = "공지사항 알림 조회",
            description = "공지사항 알림을 페이징해서 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지사항 알림 조회 성공")
    })
    @GetMapping
    public ResponseEntity<Response<AnnouncementPageResponse>> getAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AnnouncementPageResponse result =
                announcementService.getAnnouncements(page, size);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "공지사항 조회 성공"
                )
        );
    }
}
