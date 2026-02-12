package com.umc.connext.domain.announcement.controller;

import jakarta.validation.Valid;
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
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.umc.connext.domain.announcement.dto.response.AnnouncementPageResponse;
import com.umc.connext.domain.announcement.dto.response.AnnouncementCreateRequestDTO;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@Tag(name = "Notifications-NOTICES", description = "공지사항 알림 관련 API")
@RestController
@RequestMapping("/notifications/notices")
@RequiredArgsConstructor
@Validated
public class AnnouncementController {

    private final AnnouncementService announcementService;

    private static final Long TEMP_MEMBER_ID = 1L;

    @GetMapping
    public ResponseEntity<Response<AnnouncementPageResponse>> getAnnouncements(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {

        AnnouncementPageResponse result =
                announcementService.getAnnouncements(
                        TEMP_MEMBER_ID,
                        page,
                        size
                );

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "공지사항 조회 성공"
                )
        );
    }

    @PostMapping
    public ResponseEntity<Response<Void>> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequestDTO request
    ) {

        announcementService.createAnnouncement(
                TEMP_MEMBER_ID,
                request
        );

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        "공지사항 전송 성공"
                )
        );
    }
}
