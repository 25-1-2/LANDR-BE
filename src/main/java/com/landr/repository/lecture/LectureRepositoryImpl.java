package com.landr.repository.lecture;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.landr.domain.lecture.QLecture;
import com.landr.domain.plan.QPlan;
import com.landr.controller.lecture.dto.LectureSearchRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.repository.lecture.dto.LectureWithPlanCount;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LectureRepositoryImpl implements LectureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Lecture> findBySearchWithCursor(LectureSearchRequest req) {
        QLecture lecture = QLecture.lecture;

        BooleanBuilder cond = new BooleanBuilder();

        if (req.getSearch() != null) {
            cond.and(lecture.title.contains(req.getSearch())
                    .or(lecture.teacher.contains(req.getSearch())));
        }
        if (req.getCursor() != null) {
            cond.and(lecture.id.lt(req.getCursor()));
        }

        return queryFactory.selectFrom(lecture)
                .where(cond)
                .orderBy(lecture.createdAt.desc(), lecture.id.desc())
                .limit(req.getOffset() + 1)
                .fetch();
    }

    @Override
    public List<LectureWithPlanCount> findOrderByPlanCount(LectureSearchRequest req) {
        QLecture lecture = QLecture.lecture;
        QPlan plan = QPlan.plan;

        BooleanBuilder cond = new BooleanBuilder();

        if (req.getCursor() != null) {
            cond.and(lecture.id.lt(req.getCursor()));
        }

        return queryFactory
                .select(Projections.constructor(LectureWithPlanCount.class,
                        lecture,
                        plan.count()
                ))
                .from(lecture)
                .leftJoin(plan).on(plan.lecture.eq(lecture))
                .where(cond)
                .groupBy(lecture.id)
                .orderBy(plan.count().desc(), lecture.id.desc())
                .limit(req.getOffset() + 1)
                .fetch();
    }
}

