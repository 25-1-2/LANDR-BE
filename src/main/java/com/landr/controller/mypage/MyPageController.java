package com.landr.controller.mypage;

import com.landr.domain.user.User;
import com.landr.service.mypage.MyPageService;
import com.landr.service.mypage.dto.MyPage;
import com.landr.service.mypage.dto.MyPageStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/mypage")
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageService myPageService;

    // 마이페이지 기본 정보 조회
    @GetMapping
    @Operation(summary = "마이페이지 기본 정보 조회", description = "유저 이름, 오늘 수강할 강의 수 및 완료한 강의 수, 완강수, 공부 연속 일수, 목표 날짜, 완료한 강의 리스트, 과목별 성취도 리스트를 조회합니다.")
    public ResponseEntity<MyPage> getMyPageInfo(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(myPageService.getMyPageInfo(user));
    }

    // 월별 공부 시간, 학습 통계 조회
    @GetMapping("/statistics")
    @Operation(summary = "월별 공부 기록 통계 조회", description = "특정 월의 과목별 공부 시간과 주차별 공부 시간을 조회합니다. 날짜 형식은 yyyy-MM (예: 2025-05)")
    public ResponseEntity<MyPageStatistics> getMonthlyStatistics(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth date,
        @AuthenticationPrincipal User user
    ) {
        MyPageStatistics statistics = myPageService.getMonthlyStatistics(user.getId(), date);
        return ResponseEntity.ok(statistics);
    }
}
