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

    // 공지 조회
    @GetMapping
    public ResponseEntity<Response<AnnouncementPageResponse>> getAnnouncements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Response.fail(
                            com.umc.connext.common.code.ErrorCode.UNAUTHORIZED
                    ));
        }

        Long memberId = userDetails.getMemberId();

        AnnouncementPageResponse result =
                announcementService.getAnnouncements(memberId, page, size);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "공지사항 조회 성공"
                )
        );
    }

    // 공지 생성
    @PostMapping
    public ResponseEntity<Response<Void>> createAnnouncement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnnouncementCreateRequestDTO request
    ) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Response.fail(
                            com.umc.connext.common.code.ErrorCode.UNAUTHORIZED
                    ));
        }

        Long adminId = userDetails.getMemberId();

        announcementService.createAnnouncement(adminId, request);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        "공지사항 전송 성공"
                )
        );
    }
}
