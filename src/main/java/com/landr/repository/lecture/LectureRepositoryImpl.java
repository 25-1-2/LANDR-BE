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
    private final QLecture lecture = QLecture.lecture;

    @Override
    public List<Lecture> findLatestLecturesWithCursor(LectureSearchRequest req) {
        BooleanBuilder cond = new BooleanBuilder();

        if (req.getCursorLectureId() != null && req.getCursorCreatedAt() != null) {
            BooleanExpression cursorCondition =
                    lecture.createdAt.lt(req.getCursorCreatedAt())
                            .or(lecture.createdAt.eq(req.getCursorCreatedAt())
                                    .and(lecture.id.lt(req.getCursorLectureId())));
            cond.and(cursorCondition);
        }

        if (req.getPlatform() != null) {
            cond.and(lecture.platform.eq(req.getPlatform()));
        }

        if (req.getSubject() != null) {
            cond.and(lecture.subject.eq(req.getSubject()));
        }

        return queryFactory
                .selectFrom(lecture)
                .where(cond)
                .orderBy(lecture.createdAt.desc(), lecture.id.desc())
                .limit(req.getOffset() + 1)
                .fetch();
    }

    @Override
    public List<Lecture> findLatestLecturesBySearch(LectureSearchRequest req) {
        BooleanBuilder cond = new BooleanBuilder();

        if (req.getSearch() != null && !req.getSearch().isBlank()) {
            BooleanExpression searchCondition =
                    lecture.title.containsIgnoreCase(req.getSearch())
                            .or(lecture.teacher.containsIgnoreCase(req.getSearch()));
            cond.and(searchCondition);
        }

        if (req.getPlatform() != null) {
            cond.and(lecture.platform.eq(req.getPlatform()));
        }

        if (req.getSubject() != null) {
            cond.and(lecture.subject.eq(req.getSubject()));
        }

        if (req.getCursorLectureId() != null && req.getCursorCreatedAt() != null) {
            BooleanExpression cursorCondition =
                    lecture.createdAt.lt(req.getCursorCreatedAt())
                            .or(lecture.createdAt.eq(req.getCursorCreatedAt())
                                    .and(lecture.id.lt(req.getCursorLectureId())));
            cond.and(cursorCondition);
        }

        return queryFactory
                .selectFrom(lecture)
                .where(cond)
                .orderBy(lecture.createdAt.desc(), lecture.id.desc())
                .limit(req.getOffset() + 1)
                .fetch();
    }
}
