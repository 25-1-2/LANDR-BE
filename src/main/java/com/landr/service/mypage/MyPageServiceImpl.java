package com.landr.service.mypage;

import com.landr.domain.user.User;
import com.landr.service.mypage.dto.MyPage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MyPageServiceImpl implements MyPageService{


    @Override
    public MyPage getMyPageInfo(User user) {
        // 완료한 강의 수

        // 공부 연속일 수

        // 목표 날짜

        // 완료한 계획 리스트

        // 과목별 성취도 리스트(과목별 시작일, 종료일, 총 강의 수, 완료한 강의 수)
        // 시작일은 과목별 가장 빠른 시작일, 종료일은 과목별 가장 늦은 종료일로 설정
        // 총 강의 수는 과목에 대한 계획들의 총 강의 수
        return MyPage.builder()
            .userName(user.getName())
            .completedLectureCount(0)
            .studyStreak(0)
            .goalDate(null)
            .completedPlanList(null)
            .subjectAchievementList(null)
            .build();
    }
}
