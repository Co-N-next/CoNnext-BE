package com.umc.connext.domain.mynotification.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.mynotification.service.MyNotificationService;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationPageResponse;
import com.umc.connext.domain.mynotification.dto.response.ShareLocationRequestDTO;
import com.umc.connext.domain.mynotification.dto.response.ShareMateRequestDTO;
import com.umc.connext.domain.notification.type.NotificationType;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryCreateRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/notifications/news")
@RequiredArgsConstructor
@Tag(name = "Notifications-NEWS", description = "내소식 알림 관련 API")
public class MyNotificationController {

    private final MyNotificationService myNotificationService;

    // 내소식 조회
    @GetMapping
    public ResponseEntity<Response<MyNotificationPageResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        Long memberId = user.getMemberId();
        MyNotificationPageResponse result =
                myNotificationService.getMyNotifications(memberId, page, size);

        return ResponseEntity.ok(
                Response.success(SuccessCode.GET_SUCCESS, result, "내소식 알림 조회 성공")
        );
    }

    // 위치 공유 수락
    @PostMapping("/share-locations")
    public ResponseEntity<Response<Void>> shareLocations(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ShareLocationRequestDTO request
    ) {
        Long memberId = user.getMemberId();
        myNotificationService.acceptShareLocation(memberId, request);

        return ResponseEntity.ok(
                Response.success(SuccessCode.UPDATE_SUCCESS, "위치 공유 요청 수락 성공")
        );
    }

    // 메이트 수락
    @PostMapping("/share-mates")
    public ResponseEntity<Response<Void>> shareMates(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ShareMateRequestDTO request
    ) {
        Long memberId = user.getMemberId();
        myNotificationService.acceptShareMate(memberId, request);

        return ResponseEntity.ok(
                Response.success(SuccessCode.UPDATE_SUCCESS, "메이트 요청 수락 성공")
        );
    }

    // 읽음 처리
    @GetMapping("/{notificationId}/read")
    public ResponseEntity<Response<Void>> readMyNotification(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId
    ) {
        Long memberId = user.getMemberId();
        myNotificationService.markAsRead(memberId, notificationId);

        return ResponseEntity.ok(
                Response.success(SuccessCode.UPDATE_SUCCESS, "알림 읽음 처리 성공")
        );
    }
}

