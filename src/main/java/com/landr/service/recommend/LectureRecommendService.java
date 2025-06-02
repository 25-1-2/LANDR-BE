package com.landr.service.recommend;

import com.landr.controller.user.dto.LectureRecommendRequest;
import com.landr.controller.user.dto.LectureRecommendResponse;
import com.landr.domain.lecture.Lecture;
import com.landr.repository.recommend.LectureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureRecommendService {

    private final LectureRepository lectureRepository;
    private final LectureMapper lectureMapper;
    private final GPTService gptService;
    private final EntityManager entityManager;

    public List<LectureRecommendResponse> recommend(LectureRecommendRequest request) {
        try {
            // 1. 기본 필터링 (기존 로직 개선)
            List<Lecture> candidateLectures = getFilteredLectures(request);
            log.info("필터링 후 강의 수: {}", candidateLectures.size());

            // 데이터가 없으면 빈 리스트 반환
            if (candidateLectures.isEmpty()) {
                log.warn("추천할 강의가 없습니다.");
                return new ArrayList<>();
            }

            // 2. GPT를 활용한 지능형 추천
            List<LectureRecommendResponse> aiRecommendations =
                    getAIRecommendations(candidateLectures, request);

            // 3. 과목별로 3개씩 추천 반환
            return getTopRecommendationsBySubject(aiRecommendations, 3);

        } catch (Exception e) {
            log.error("강의 추천 중 오류 발생", e);
            // 💡 GPT 실패 시 기존 방식으로 fallback
            return getFallbackRecommendations(request);
        }
    }

    private List<Lecture> getFilteredLectures(LectureRecommendRequest request) {
        // === 디버깅 시작 ===
        log.info("=== 데이터베이스 연결 디버깅 시작 ===");

        try {
            // 현재 연결 정보 확인
            Query urlQuery = entityManager.createNativeQuery("SELECT @@hostname, @@port");
            Object[] connectionInfo = (Object[]) urlQuery.getSingleResult();
            log.info("🔍 현재 연결된 서버: {}, 포트: {}", connectionInfo[0], connectionInfo[1]);

            Query dbQuery = entityManager.createNativeQuery("SELECT DATABASE()");
            Object currentDB = dbQuery.getSingleResult();
            log.info("🔍 현재 데이터베이스: {}", currentDB);

            Query userQuery = entityManager.createNativeQuery("SELECT USER()");
            Object currentUser = userQuery.getSingleResult();
            log.info("🔍 현재 사용자: {}", currentUser);

        } catch (Exception e) {
            log.error("연결 정보 확인 실패", e);
        }

        try {
            // 네이티브 쿼리로 먼저 확인
            Long nativeCount = lectureRepository.countAllNative();
            log.info("네이티브 쿼리 강의 수: {}", nativeCount);

            List<Object[]> rawData = lectureRepository.findRawLectures();
            log.info("네이티브 원시 데이터 수: {}", rawData.size());

            if (!rawData.isEmpty()) {
                Object[] first = rawData.get(0);
                log.info("첫 번째 원시 데이터: {}", Arrays.toString(first));
                log.info("첫 번째 데이터 길이: {}", first.length);
            }

            // 기본 컬럼들만 조회
            List<Object[]> basicData = lectureRepository.findBasicColumns();
            log.info("기본 컬럼 데이터 수: {}", basicData.size());
            if (!basicData.isEmpty()) {
                Object[] firstBasic = basicData.get(0);
                log.info("기본 컬럼 데이터: id={}, title={}, teacher={}, platform={}, subject={}, tag={}",
                        firstBasic[0], firstBasic[1], firstBasic[2], firstBasic[3], firstBasic[4], firstBasic[5]);
            }

        } catch (Exception e) {
            log.error("네이티브 쿼리 실행 오류", e);
        }

        // JPA로 조회
        List<Lecture> allLectures = lectureRepository.findAll();
        log.info("JPA 조회 강의 수: {}", allLectures.size());

        if (!allLectures.isEmpty()) {
            Lecture first = allLectures.get(0);
            log.info("JPA 첫 번째 강의: id={}, title={}, tag={}",
                    first.getId(), first.getTitle(), first.getTag());
        }

        log.info("=== 데이터베이스 연결 디버깅 종료 ===");
        // === 디버깅 종료 ===

        // 🆕 필터링 + 우선순위 정렬
        List<Lecture> filteredLectures = allLectures.stream()
                .filter(lecture -> {
                    boolean hasTag = lecture.getTag() != null;
                    log.debug("강의: {}, 태그: {}, 태그있음: {}", lecture.getTitle(), lecture.getTag(), hasTag);
                    return hasTag;
                })
                .filter(lecture -> {
                    boolean gradeMatch = matchGrade(lecture, request.getGrade());
                    log.debug("강의: {}, 학년매치: {}", lecture.getTitle(), gradeMatch);
                    return gradeMatch;
                })
                .filter(lecture -> {
                    boolean difficultyMatch = matchDifficultyLevel(lecture, request);
                    log.debug("강의: {}, 난이도매치: {}", lecture.getTitle(), difficultyMatch);
                    return difficultyMatch;
                })
                // 🆕 목표별 우선순위 정렬
                .sorted((a, b) -> {
                    int scoreA = calculatePriorityScore(a, request);
                    int scoreB = calculatePriorityScore(b, request);
                    return Integer.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());

        log.info("우선순위 정렬 후 강의 수: {}", filteredLectures.size());
        return filteredLectures;
    }

    // 🆕 우선순위 점수 계산
    private int calculatePriorityScore(Lecture lecture, LectureRecommendRequest request) {
        int score = 0;
        String tag = lecture.getTag() != null ? lecture.getTag().toLowerCase() : "";
        String title = lecture.getTitle() != null ? lecture.getTitle().toLowerCase() : "";

        // 목표 매칭 시 높은 점수
        if ("기출 분석".equals(request.getGoal())) {
            if (tag.contains("기출") || title.contains("기출")) score += 15;
            if (tag.contains("실전") || title.contains("실전")) score += 10;
            if (tag.contains("문제") || title.contains("문제")) score += 10;
        }

        if ("개념 정리".equals(request.getGoal())) {
            if (tag.contains("개념") || title.contains("개념")) score += 15;
            if (tag.contains("기초") || title.contains("기초")) score += 10;
        }

        // 학습 스타일 매칭
        if (request.getStyles() != null) {
            for (String style : request.getStyles()) {
                if ("문풀 위주".equals(style) && (tag.contains("문제") || tag.contains("실전"))) {
                    score += 12;
                }
                if ("차분한 설명".equals(style) && (tag.contains("개념") || tag.contains("완성"))) {
                    score += 8;
                }
            }
        }

        // 인기 플랫폼 우선순위
        if ("ETOOS".equals(lecture.getPlatform()) || "DAESANG".equals(lecture.getPlatform())) {
            score += 5;
        }

        return score;
    }

    private boolean matchGrade(Lecture lecture, String grade) {
        if (lecture.getTag() == null) return true; // tag가 없으면 일단 포함

        String tag = lecture.getTag().toLowerCase();

        // 실제 데이터 형태에 맞게 수정
        switch (grade) {
            case "고1":
                return tag.contains("고1") || tag.contains("1학년");
            case "고2":
                return tag.contains("고2") || tag.contains("2학년") || tag.contains("고3·2");
            case "고3":
                return tag.contains("고3") || tag.contains("3학년") || tag.contains("고3·2");
            case "N수생":
                return tag.contains("n수") || tag.contains("재수") || tag.contains("고3·2·n수");
            default:
                return true; // 학년 정보가 불분명하면 포함
        }
    }

    private boolean matchDifficultyLevel(Lecture lecture, LectureRecommendRequest request) {
        if (lecture.getTag() == null) return true; // tag가 없으면 일단 포함

        String tag = lecture.getTag().toLowerCase();

        // 실제 데이터의 키워드에 맞게 수정
        // 고성적자 (1-2등급)
        if (request.getSchoolRank() <= 2 && request.getMockRank() <= 2) {
            return tag.contains("심화") || tag.contains("고급") || tag.contains("실전") ||
                    tag.contains("완성"); // "완성" 키워드가 많이 보임
        }
        // 중위권 (3-4등급)
        else if (request.getSchoolRank() <= 4 && request.getMockRank() <= 4) {
            return tag.contains("개념") || tag.contains("완성") || tag.contains("내신") ||
                    !tag.contains("고급"); // 고급이 아닌 것들
        }
        // 하위권 (5등급 이하)
        else {
            return tag.contains("기초") || tag.contains("개념") || tag.contains("내신");
        }
    }

    private List<LectureRecommendResponse> getAIRecommendations(
            List<Lecture> lectures, LectureRecommendRequest request) {

        // 🆕 강의 수 제한 (토큰 한계 해결)
        List<Lecture> limitedLectures = lectures.stream()
                .limit(25) // ⬅️ 최대 25개만 GPT에게 보내기 (토큰 한계 고려)
                .collect(Collectors.toList());

        log.info("GPT에게 보낼 강의 수: {} (전체 필터링된 강의 수: {})", limitedLectures.size(), lectures.size());

        // GPT에게 보낼 프롬프트 생성
        String prompt = buildRecommendationPrompt(limitedLectures, request);

        // 🆕 프롬프트 길이 체크
        log.info("프롬프트 대략적 길이: {} 글자", prompt.length());

        // GPT API 호출
        String gptResponse = gptService.getRecommendation(prompt);

        // GPT 응답을 파싱하여 추천 점수와 이유 추출
        return parseGPTResponse(limitedLectures, gptResponse, request);
    }

    private String buildRecommendationPrompt(List<Lecture> lectures, LectureRecommendRequest request) {
        StringBuilder prompt = new StringBuilder();

        // 🆕 프롬프트 간소화
        prompt.append("학생: ").append(request.getGrade())
                .append(", 내신 ").append(request.getSchoolRank())
                .append("등급, 모의고사 ").append(request.getMockRank()).append("등급\n");
        prompt.append("목표: ").append(request.getGoal()).append("\n");
        prompt.append("방향: ").append(request.getFocus()).append("\n");

        if (request.getStyles() != null && !request.getStyles().isEmpty()) {
            prompt.append("스타일: ").append(String.join(", ", request.getStyles())).append("\n");
        }

        prompt.append("\n강의목록:\n");
        for (int i = 0; i < lectures.size(); i++) {
            Lecture lecture = lectures.get(i);
            // 🆕 프롬프트 대폭 간소화 (제목 길이 제한)
            String shortTitle = lecture.getTitle().length() > 40 ?
                    lecture.getTitle().substring(0, 40) + "..." : lecture.getTitle();
            String shortTag = lecture.getTag() != null && lecture.getTag().length() > 20 ?
                    lecture.getTag().substring(0, 20) + "..." : lecture.getTag();

            prompt.append(String.format("%d. %s (%s) [%s]\n",
                    i + 1, shortTitle, lecture.getTeacher(), shortTag));
        }

        prompt.append("\n각 강의 추천점수(0-100)와 이유:\n");
        prompt.append("형식: 번호|점수|이유\n");
        prompt.append("예: 1|85|목표와 성적에 적합|중급\n");

        return prompt.toString();
    }

    private List<LectureRecommendResponse> parseGPTResponse(
            List<Lecture> lectures, String gptResponse, LectureRecommendRequest request) {

        List<LectureRecommendResponse> recommendations = new ArrayList<>();
        String[] lines = gptResponse.split("\n");

        for (String line : lines) {
            try {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) { // 최소 3개 부분 (번호|점수|이유)
                        int lectureIndex = Integer.parseInt(parts[0].trim()) - 1;
                        double score = Double.parseDouble(parts[1].trim());
                        String reason = parts[2].trim();
                        String difficulty = parts.length >= 4 ? parts[3].trim() : "보통";

                        if (lectureIndex >= 0 && lectureIndex < lectures.size()) {
                            Lecture lecture = lectures.get(lectureIndex);
                            LectureRecommendResponse response = lectureMapper.toDto(lecture);

                            // 🆕 AI 추천 정보 추가
                            response = LectureRecommendResponse.builder()
                                    .id(response.getId())
                                    .platform(response.getPlatform())
                                    .title(response.getTitle())
                                    .teacher(response.getTeacher())
                                    .url(response.getUrl())
                                    .description(response.getDescription())
                                    .recommendScore(score)
                                    .recommendReason(reason)
                                    .difficulty(difficulty)
                                    .isPersonalized(true)
                                    .build();

                            recommendations.add(response);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("GPT 응답 파싱 실패: {}", line, e);
            }
        }

        log.info("GPT 파싱 완료: {}개 추천 생성", recommendations.size());
        return recommendations;
    }

    private List<LectureRecommendResponse> getFallbackRecommendations(LectureRecommendRequest request) {
        // GPT 실패 시 기존 방식으로 fallback
        log.info("AI 추천 실패, 기존 방식으로 fallback");

        List<Lecture> lectures = lectureRepository.findAll();
        List<LectureRecommendResponse> fallbackRecommendations = lectures.stream()
                .filter(l -> l.getTag() != null && matchTag(l.getTag(), request))
                .filter(l -> matchGrade(l, request.getGrade()))
                .limit(50) // 🆕 더 많은 후보군 확보
                .map(lecture -> {
                    LectureRecommendResponse base = lectureMapper.toDto(lecture);

                    // 🆕 fallback에서도 간단한 점수 계산
                    double score = calculateSimpleScore(lecture, request);

                    return LectureRecommendResponse.builder()
                            .id(base.getId())
                            .platform(base.getPlatform())
                            .title(base.getTitle())
                            .teacher(base.getTeacher())
                            .url(base.getUrl())
                            .description(base.getDescription())
                            .recommendScore(score)
                            .recommendReason("기본 매칭 (AI 추천 실패)")
                            .difficulty("보통")
                            .isPersonalized(false)
                            .build();
                })
                .collect(Collectors.toList());

        // 🆕 fallback에서도 과목별 3개씩 반환
        return getTopRecommendationsBySubject(fallbackRecommendations, 3);
    }

    // 🆕 간단한 점수 계산 (fallback용)
    private double calculateSimpleScore(Lecture lecture, LectureRecommendRequest request) {
        double score = 50.0; // 기본 점수

        if (lecture.getTag() != null) {
            String tag = lecture.getTag().toLowerCase();

            // 목표 매칭
            if ("기출 분석".equals(request.getGoal()) && tag.contains("기출")) {
                score += 20;
            }
            if ("개념 정리".equals(request.getGoal()) && tag.contains("개념")) {
                score += 20;
            }

            // 성적 매칭
            if (request.getSchoolRank() <= 2 && tag.contains("실전")) {
                score += 15;
            } else if (request.getSchoolRank() >= 4 && tag.contains("기초")) {
                score += 15;
            }
        }

        return Math.min(score, 100.0); // 최대 100점
    }

    // 🆕 과목별로 상위 N개씩 추천 반환
    private List<LectureRecommendResponse> getTopRecommendationsBySubject(
            List<LectureRecommendResponse> recommendations, int limitPerSubject) {

        return recommendations.stream()
                .sorted((a, b) -> Double.compare(b.getRecommendScore(), a.getRecommendScore()))
                .collect(Collectors.groupingBy(
                        lecture -> extractSubject(lecture.getTitle(), lecture.getDescription()),
                        Collectors.toList()
                ))
                .values()
                .stream()
                .flatMap(subjectLectures ->
                        subjectLectures.stream()
                                .limit(limitPerSubject) // 과목당 3개씩
                )
                .collect(Collectors.toList());
    }

    // 🆕 강의 제목/설명에서 과목 추출
    private String extractSubject(String title, String description) {
        String text = (title + " " + (description != null ? description : "")).toLowerCase();

        if (text.contains("국어") || text.contains("문학") || text.contains("독서") || text.contains("문법")) {
            return "국어";
        } else if (text.contains("수학") || text.contains("미적분") || text.contains("기하") || text.contains("확률")) {
            return "수학";
        } else if (text.contains("영어") || text.contains("english")) {
            return "영어";
        } else if (text.contains("물리") || text.contains("화학") || text.contains("생물") || text.contains("지구과학") || text.contains("통합과학")) {
            return "과학";
        } else if (text.contains("한국사") || text.contains("세계사") || text.contains("통합사회") || text.contains("정치") || text.contains("경제")) {
            return "사회";
        } else {
            return "기타";
        }
    }

    private boolean matchTag(String tag, LectureRecommendRequest req) {
        if (tag == null) return true;

        String lowerTag = tag.toLowerCase();

        // 실제 키워드에 맞게 매칭
        boolean goalMatch = false;
        switch (req.getGoal().toLowerCase()) {
            case "기출 분석":
                goalMatch = lowerTag.contains("기출") || lowerTag.contains("실전") ||
                        lowerTag.contains("문제") || lowerTag.contains("완성");
                break;
            case "개념 정리":
                goalMatch = lowerTag.contains("개념") || lowerTag.contains("기초") ||
                        lowerTag.contains("완성");
                break;
            default:
                goalMatch = true;
        }

        // focus 매칭
        boolean focusMatch = req.getFocus().contains("내신") ?
                lowerTag.contains("내신") || lowerTag.contains("내신집중") :
                true;

        return goalMatch || focusMatch;
    }
}