package com.landr.controller.mypage;

import com.landr.domain.user.User;
import com.landr.service.mypage.MyPageService;
import com.landr.service.mypage.dto.MyPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/mypage")
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageService myPageService;

    // 마이페이지 기본 정보 조회
    @GetMapping
    @Operation(summary = "마이페이지 기본 정보 조회")
    public MyPage getMyPageInfo(
        @AuthenticationPrincipal User user
    ) {
        return myPageService.getMyPageInfo(user);
    }
}
