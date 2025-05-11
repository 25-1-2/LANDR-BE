package com.landr.service.mypage;

import com.landr.domain.user.User;
import com.landr.service.mypage.dto.MyPage;
import com.landr.service.mypage.dto.MyPageStatistics;
import java.time.YearMonth;

public interface MyPageService {

    /**
     * 기본 마이페이지 정보 조회
     */
    MyPage getMyPageInfo(User user);

    /**
     * 월별 과목별 공부 시간, 주간 공부 시간 통계 조회
     */
    MyPageStatistics getMonthlyStatistics(Long userId, YearMonth date);
}
