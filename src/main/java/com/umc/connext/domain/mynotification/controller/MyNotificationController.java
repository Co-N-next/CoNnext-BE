package com.umc.connext.domain.mynotification.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.mynotification.service.MyNotificationService;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationPageResponse;
import com.umc.connext.domain.notification.type.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MyNotificationPageResponse result =
                myNotificationService.getMyNotifications(page, size);

        return ResponseEntity.ok(
                Response.success(
                        SuccessCode.GET_SUCCESS,
                        result,
                        "내소식 알림 전체 조회 성공"
                )
        );
    }
}
