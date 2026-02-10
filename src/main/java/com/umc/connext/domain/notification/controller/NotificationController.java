package com.umc.connext.domain.notification.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.umc.connext.domain.notification.service.NotificationService;
import com.umc.connext.domain.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import com.umc.connext.global.jwt.principal.CustomUserDetails;

@Tag(name = "Notifications", description = "알림 관련 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "내소식/공지사항 알림 존재 여부",
            description = "내소식/공지사항의 안 읽은 알림 존재 여부를 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 존재 여부 확인 성공")
    })
    @GetMapping
    public ResponseEntity<Response<Boolean>> hasUnreadNotification(
            @RequestParam NotificationType type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();

        boolean result =
                notificationService.hasUnreadNotification(memberId, type);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        result ? "안 읽은 알림 존재" : "안 읽은 알림 없음"
                )
        );
    }

}
