package com.landr.repository.lecture;

import com.landr.controller.lecture.LectureSearchRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.QLecture;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LectureRepositoryImpl implements LectureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 최신순 정렬 (createdAt DESC, id DESC) 기준으로 커서 페이지네이션 (전체 조회)
     */
    @Override
    public List<Lecture> findLatestLecturesWithCursor(LectureSearchRequest req) {
        QLecture lecture = QLecture.lecture;

        BooleanBuilder cond = new BooleanBuilder();

        // 커서 조건 (이전 페이지 마지막 강의의 createdAt, id를 기준으로)
        if (req.getCursorLectureId() != null && req.getCursorCreatedAt() != null) {
            BooleanExpression cursorCondition =
                    lecture.createdAt.lt(req.getCursorCreatedAt())
                            .or(
                                    lecture.createdAt.eq(req.getCursorCreatedAt())
                                            .and(lecture.id.lt(req.getCursorLectureId()))
                            );
            cond.and(cursorCondition);
        }

        return queryFactory
                .selectFrom(lecture)
                .where(cond)
                .orderBy(lecture.createdAt.desc(), lecture.id.desc()) // 최신순 정렬
                .limit(req.getOffset() + 1)  // 페이지 크기 + 1 (hasNext 판별용)
                .fetch();
    }

    /**
     * 최신순 정렬 (createdAt DESC, id DESC) + 검색 조건 적용
     */
    @Override
    public List<Lecture> findLatestLecturesBySearch(LectureSearchRequest req) {
        QLecture lecture = QLecture.lecture;

        BooleanBuilder cond = new BooleanBuilder();

        // 검색어 조건 (강의명 또는 선생님 이름)
        if (req.getSearch() != null && !req.getSearch().isBlank()) {
            BooleanExpression searchCondition =
                    lecture.title.containsIgnoreCase(req.getSearch())
                            .or(lecture.teacher.containsIgnoreCase(req.getSearch()));
            cond.and(searchCondition);
        }

        // 커서 조건 (이전 페이지 마지막 강의의 createdAt, id를 기준으로)
        if (req.getCursorLectureId() != null && req.getCursorCreatedAt() != null) {
            BooleanExpression cursorCondition =
                    lecture.createdAt.lt(req.getCursorCreatedAt())
                            .or(
                                    lecture.createdAt.eq(req.getCursorCreatedAt())
                                            .and(lecture.id.lt(req.getCursorLectureId()))
                            );
            cond.and(cursorCondition);
        }

        return queryFactory
                .selectFrom(lecture)
                .where(cond)
                .orderBy(lecture.createdAt.desc(), lecture.id.desc())  // 최신순 정렬
                .limit(req.getOffset() + 1)  // 페이지 크기 + 1 (hasNext 판별용)
                .fetch();
    }
}
