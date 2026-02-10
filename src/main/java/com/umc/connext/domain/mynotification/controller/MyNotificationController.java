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

@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Notifications-NEWS", description = "내소식 알림 관련 API")
@RestController
@RequestMapping("/notifications/news")
@RequiredArgsConstructor
public class MyNotificationController {
    private final MyNotificationService myNotificationService;

    @Operation(
            summary = "내소식 알림 조회",
            description = "내소식 알림을 페이징해서 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내소식 알림 조회 성공")
    })
    @GetMapping
    public ResponseEntity<Response<MyNotificationPageResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MyNotificationPageResponse result =
                myNotificationService.getMyNotifications(
                        user.getMemberId(),
                        page,
                        size);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "내소식 알림 조회 성공"
                )
        );
    }

    @Operation(
            summary = "위치 공유 요청 알림 수락",
            description = "위치 공유 요청 알림을 수락합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "위치 공유 요청 알림 수락을 성공")
    })
    @PostMapping("/share-locations")
    public ResponseEntity<Response<Void>> shareLocations(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ShareLocationRequestDTO request
    ) {
        myNotificationService.acceptShareLocation(
                user.getMemberId(),
                request
        );

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.UPDATE_SUCCESS,
                        "위치 공유 요청 수락 성공"
                )
        );
    }

    @Operation(
            summary = "메이트 요청 수락",
            description = "메이트 요청을 수락합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메이트 요청 수락 성공")
    })
    @PostMapping("/share-mates")
    public ResponseEntity<Response<Void>> shareMates(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ShareMateRequestDTO request
    ) {
        myNotificationService.acceptShareMate(
                user.getMemberId(),
                request
        );

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.UPDATE_SUCCESS,
                        "메이트 요청 수락 성공"
                )
        );
    }

    @Operation(
            summary = "알림 읽음 표시",
            description = "알림을 읽었을 경우 알림 읽음으로 표시됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    })
    @GetMapping("/{notificationId}/read")
    public ResponseEntity<Response<Void>> readMyNotification(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId
    ) {
        myNotificationService.markAsRead(
                user.getMemberId(),
                notificationId
        );

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.UPDATE_SUCCESS,
                        "알림 읽음 처리 성공"
                )
        );
    }
}
