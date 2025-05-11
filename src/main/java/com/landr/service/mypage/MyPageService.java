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

    MyPageStatistics getMonthlyStatistics(Long id, YearMonth date);
}
