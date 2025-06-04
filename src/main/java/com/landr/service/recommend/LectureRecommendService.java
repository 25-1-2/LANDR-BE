package com.landr.service.recommend;

import com.landr.controller.user.dto.LectureRecommendRequest;
import com.landr.controller.user.dto.LectureRecommendResponse;
import com.landr.domain.lecture.Lecture;
import com.landr.repository.recommend.LectureRecommendRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureRecommendService {

    private final LectureRecommendRepository lectureRepository;
    private final LectureMapper lectureMapper;
    private final GPTService gptService;

    public List<LectureRecommendResponse> recommend(LectureRecommendRequest request) {
        try {
            // 스타일 개수 체크
            if (request.getStyles() != null && request.getStyles().size() > 2) {
                throw new IllegalArgumentException("학습 스타일은 최대 2개까지 선택 가능합니다.");
            }

            // 과목별 직접 조회
            List<Lecture> candidateLectures = getLecturesBySubject(request);
            log.info("과목 '{}' 조회 결과: {}개", request.getSubject(), candidateLectures.size());

            if (candidateLectures.isEmpty()) {
                log.warn("과목 '{}'에 해당하는 강의가 없습니다.", request.getSubject());
                return new ArrayList<>();
            }

            // 추가 필터링 (메모리에서 최소한만)
            List<Lecture> filteredLectures = filterLectures(candidateLectures, request);
            log.info("필터링 후 강의 수: {}", filteredLectures.size());

            if (filteredLectures.isEmpty()) {
                return getFallbackRecommendations(request);
            }

            List<LectureRecommendResponse> aiRecommendations = getAIRecommendations(filteredLectures, request);
            List<LectureRecommendResponse> topRecommendations = getTopRecommendations(aiRecommendations, 10);

            // 최종 안전장치: 무조건 최소 3개 보장
            if (topRecommendations.size() < 3) {
                log.warn("최종 추천 부족 ({}개), 필터링된 강의로 추가 보충", topRecommendations.size());
                return ensureMinimumRecommendations(topRecommendations, filteredLectures, request, 3);
            }

            return topRecommendations;

        } catch (Exception e) {
            log.error("강의 추천 중 오류 발생", e);
            return getFallbackRecommendations(request);
        }
    }

    // 과목별 DB 조회
    private List<Lecture> getLecturesBySubject(LectureRecommendRequest request) {
        String subject = request.getSubject();

        // 먼저 과목별 개수 확인
        Long count = lectureRepository.countBySubjectKeyword(subject);
        log.info("DB에서 과목 '{}' 강의 수: {}개", subject, count);

        if (count == 0) {
            // 정확한 매칭이 안되면 대안 키워드로 재시도
            subject = getAlternativeSubjectKeyword(subject);
            log.info("대안 키워드 '{}' 로 재검색", subject);
            count = lectureRepository.countBySubjectKeyword(subject);
            log.info("대안 키워드 검색 결과: {}개", count);
        }

        //조회 (플랫폼 다양성은 필터링에서 처리)
        List<Lecture> allLectures = lectureRepository.findBySubjectAndGrade(subject, getGradeKeyword(request.getGrade()));

        log.info("총 조회된 강의 수: {}개", allLectures.size());
        return allLectures;
    }

    // 대안 키워드 생성
    private String getAlternativeSubjectKeyword(String subject) {
        switch (subject) {
            case "수학Ⅰ": case "수학Ⅱ": case "미적분": case "확률과통계": case "기하":
                return "수학";
            case "언어와매체": case "화법과작문":
                return "국어";
            case "물리학Ⅰ": case "물리학Ⅱ":
                return "물리";
            case "화학Ⅰ": case "화학Ⅱ":
                return "화학";
            case "생명과학Ⅰ": case "생명과학Ⅱ":
                return "생물";
            case "지구과학Ⅰ": case "지구과학Ⅱ":
                return "지구과학";
            default:
                return subject;
        }
    }

    // 학년 키워드 변환
    private String getGradeKeyword(String grade) {
        switch (grade) {
            case "고1": return "고1";
            case "고2": return "고2";
            case "고3": return "고3";
            case "N수생": return "수";
            default: return "";
        }
    }

    // 추가 필터링 (DB 조회 후 메모리에서 최소 필터링 + 플랫폼 다양성 보장)
    private List<Lecture> filterLectures(List<Lecture> lectures, LectureRecommendRequest request) {
        List<Lecture> filtered = lectures.stream()
                .filter(lecture -> matchDifficulty(lecture, request))
                .filter(lecture -> matchGoal(lecture, request))
                .sorted((a, b) -> Integer.compare(
                        calculatePriorityScore(b, request),
                        calculatePriorityScore(a, request)
                ))
                .collect(Collectors.toList());

        // 플랫폼 다양성 보장
        List<Lecture> diverseResult = ensurePlatformDiversity(filtered);

        return diverseResult.stream()
                .limit(50) // 최대 50개로 제한
                .collect(Collectors.toList());
    }

    // 플랫폼 다양성 보장 메서드
    private List<Lecture> ensurePlatformDiversity(List<Lecture> lectures) {
        if (lectures.size() <= 6) {
            return lectures;
        }

        List<Lecture> result = new ArrayList<>();
        Map<String, List<Lecture>> platformGroups = lectures.stream()
                .collect(Collectors.groupingBy(l -> l.getPlatform().toString())); // 🔧 Platform enum을 String으로 변환

        log.info("플랫폼별 강의 수: {}", platformGroups.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                )));

        // 각 플랫폼에서 최소 2개씩 선택
        List<String> platforms = Arrays.asList("ETOOS", "MEGA", "DAESANG", "EBSI");
        for (String platform : platforms) {
            List<Lecture> platformLectures = platformGroups.get(platform);
            if (platformLectures != null && !platformLectures.isEmpty()) {
                result.addAll(platformLectures.stream().limit(2).collect(Collectors.toList()));
            }
        }

        // 나머지 플랫폼도 포함
        for (Map.Entry<String, List<Lecture>> entry : platformGroups.entrySet()) {
            if (!platforms.contains(entry.getKey())) {
                result.addAll(entry.getValue().stream().limit(1).collect(Collectors.toList()));
            }
        }

        // 부족하면 상위 점수 강의로 추가
        Set<Long> addedIds = result.stream().map(Lecture::getId).collect(Collectors.toSet());
        lectures.stream()
                .filter(l -> !addedIds.contains(l.getId()))
                .limit(20 - result.size())
                .forEach(result::add);

        log.info("다양성 보장 후 강의 수: {}개 (플랫폼: {}개)",
                result.size(),
                result.stream().map(l -> l.getPlatform().toString()).collect(Collectors.toSet()).size());

        return result;
    }

    private boolean matchDifficulty(Lecture lecture, LectureRecommendRequest request) {
        if (lecture.getTag() == null) return true;
        String tag = lecture.getTag().toLowerCase();

        int avgRank = (request.getSchoolRank() + request.getMockRank()) / 2;

        // 필터링을 더 관대하게 수정
        if (avgRank <= 2) {
            // 상위권: 모든 레벨 허용 (심화 우선이지만 기초도 OK)
            return true;
        } else if (avgRank <= 4) {
            // 중위권: 고급만 제외
            return !tag.contains("최상위") && !tag.contains("특목고");
        } else {
            // 하위권: 너무 어려운 것만 제외
            return !tag.contains("최상위") && !tag.contains("특목고") && !tag.contains("영재");
        }
    }

    // 목표 매칭
    private boolean matchGoal(Lecture lecture, LectureRecommendRequest request) {
        if (lecture.getTag() == null) return true;
        String tag = lecture.getTag().toLowerCase();
        String title = lecture.getTitle().toLowerCase();

        switch (request.getGoal()) {
            case "기출 분석":
                // 기출이 없으면 문제풀이나 실전도 허용
                return tag.contains("기출") || tag.contains("실전") || tag.contains("문제") ||
                        title.contains("기출") || title.contains("실전") ||
                        tag.contains("내신") || tag.contains("완성"); // 내신도 허용
            case "개념 정리":
                return tag.contains("개념") || tag.contains("기초") || tag.contains("완성") ||
                        tag.contains("교과서") || title.contains("개념") || title.contains("기초");
            case "실전 문제풀이":
                return tag.contains("실전") || tag.contains("문제") || tag.contains("문풀") ||
                        tag.contains("기출") || title.contains("실전") || title.contains("문제");
            case "빠른 요약 정리":
                return tag.contains("요약") || tag.contains("정리") || tag.contains("핵심") ||
                        tag.contains("완성") || title.contains("요약") || title.contains("정리");
            default:
                return true;
        }
    }

    private int calculatePriorityScore(Lecture lecture, LectureRecommendRequest request) {
        int score = 0;
        String text = (lecture.getTitle() + " " + lecture.getTag()).toLowerCase();

        // 목표 매칭
        if ("기출 분석".equals(request.getGoal()) && text.contains("기출")) score += 25;
        if ("개념 정리".equals(request.getGoal()) && text.contains("개념")) score += 25;
        if ("실전 문제풀이".equals(request.getGoal()) && text.contains("실전")) score += 25;

        // 스타일 매칭
        if (request.getStyles() != null) {
            for (String style : request.getStyles()) {
                switch (style) {
                    case "문풀 위주":
                        if (text.contains("문제") || text.contains("실전")) score += 15;
                        break;
                    case "차분한 설명":
                        if (text.contains("개념") || text.contains("기초")) score += 12;
                        break;
                    case "기출 분석":
                        if (text.contains("기출")) score += 15;
                        break;
                    case "개념 위주":
                        if (text.contains("개념")) score += 15;
                        break;
                    case "실전 위주":
                        if (text.contains("실전")) score += 15;
                        break;
                    case "심화 위주":
                        if (text.contains("심화")) score += 12;
                        break;
                    case "기초부터":
                        if (text.contains("기초")) score += 12;
                        break;
                }
            }
        }

        // 플랫폼 우선순위
        String platform = lecture.getPlatform().toString();
        if ("ETOOS".equals(platform) || "MEGA".equals(platform) ||
                "DAESANG".equals(platform) || "EBSI".equals(platform)) {
            score += 3;
        }

        return score;
    }

    private List<LectureRecommendResponse> getAIRecommendations(List<Lecture> lectures, LectureRecommendRequest request) {
        List<Lecture> limitedLectures = lectures.stream().limit(20).collect(Collectors.toList());
        log.info("GPT에게 보낼 강의 수: {}", limitedLectures.size());

        String prompt = buildPrompt(limitedLectures, request);
        String gptResponse = gptService.getRecommendation(prompt);

        List<LectureRecommendResponse> aiRecommendations = processGPTResponse(limitedLectures, gptResponse, request);

        // 최소 3개 보장 로직
        if (aiRecommendations.size() < 3) {
            log.warn("GPT 추천 부족 ({}개), 추가 강의로 최소 3개 보장", aiRecommendations.size());
            return ensureMinimumRecommendations(aiRecommendations, limitedLectures, request, 3);
        }

        return aiRecommendations;
    }

    private String buildPrompt(List<Lecture> lectures, LectureRecommendRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("학생: ").append(request.getGrade())
                .append(", 내신 ").append(request.getSchoolRank())
                .append("등급, 모의고사 ").append(request.getMockRank()).append("등급\n");
        prompt.append("목표: ").append(request.getGoal()).append("\n");
        prompt.append("초점: ").append(request.getFocus()).append("\n");

        if (request.getStyles() != null && !request.getStyles().isEmpty()) {
            prompt.append("스타일: ").append(String.join(", ", request.getStyles())).append("\n");
        }

        prompt.append("관심과목: ").append(request.getSubject()).append("\n");

        prompt.append("\n강의목록:\n");
        for (int i = 0; i < lectures.size(); i++) {
            Lecture lecture = lectures.get(i);
            String shortTitle = lecture.getTitle().length() > 50 ?
                    lecture.getTitle().substring(0, 50) + "..." : lecture.getTitle();

            prompt.append(String.format("%d. %s (%s)\n",
                    i + 1, shortTitle, lecture.getTeacher()));
        }

        // 최소 3개 명시적 요청
        prompt.append("\n각 강의를 학생에게 추천할 점수(0-100)와 이유를 제공하세요.\n");
        prompt.append("⚠️ 반드시 최소 3개 이상 추천해주세요!\n");
        prompt.append("형식: 번호|점수|이유\n");

        return prompt.toString();
    }

    // 최소 개수 보장 메서드
    private List<LectureRecommendResponse> ensureMinimumRecommendations(
            List<LectureRecommendResponse> aiRecommendations,
            List<Lecture> allLectures,
            LectureRecommendRequest request,
            int minCount) {

        List<LectureRecommendResponse> result = new ArrayList<>(aiRecommendations);

        // 이미 추천된 강의 ID 수집
        Set<Long> recommendedIds = aiRecommendations.stream()
                .map(LectureRecommendResponse::getId)
                .collect(Collectors.toSet());

        // 부족한 개수만큼 추가 강의 선택
        List<Lecture> remainingLectures = allLectures.stream()
                .filter(lecture -> !recommendedIds.contains(lecture.getId()))
                .sorted((a, b) -> Integer.compare(
                        calculatePriorityScore(b, request),
                        calculatePriorityScore(a, request)
                ))
                .collect(Collectors.toList());

        int needed = minCount - result.size();
        for (int i = 0; i < needed && i < remainingLectures.size(); i++) {
            Lecture lecture = remainingLectures.get(i);
            LectureRecommendResponse response = lectureMapper.toDto(lecture);

            response = LectureRecommendResponse.builder()
                    .id(response.getId())
                    .platform(response.getPlatform())
                    .title(response.getTitle())
                    .teacher(response.getTeacher())
                    .url(response.getUrl())
                    .description(response.getDescription())
                    .tag(lecture.getTag())
                    .totalLessons(lecture.getTotalLessons())
                    .recommendScore(75.0 - (i * 5)) // 점수 차별화
                    .recommendReason("학습 목표와 스타일에 적합한 강의")
                    .difficulty("보통")
                    .isPersonalized(true)
                    .subject(request.getSubject()) // 🔧 직접 설정
                    .build();

            result.add(response);
        }

        log.info("최소 개수 보장 완료: {}개 → {}개", aiRecommendations.size(), result.size());
        return result;
    }

    private List<LectureRecommendResponse> processGPTResponse(List<Lecture> lectures, String gptResponse, LectureRecommendRequest request) {
        List<LectureRecommendResponse> recommendations = new ArrayList<>();
        String[] lines = gptResponse.split("\n");

        for (String line : lines) {
            try {
                if (line.contains("|")) {
                    // "1. 1|90|이유" 또는 "1|90|이유" 형식 모두 처리
                    String cleanLine = line.trim();

                    // "1. " 부분 제거
                    if (cleanLine.matches("^\\d+\\.\\s+\\d+\\|.*")) {
                        cleanLine = cleanLine.replaceFirst("^\\d+\\.\\s+", "");
                    }

                    String[] parts = cleanLine.split("\\|");
                    if (parts.length >= 3) {
                        int index = Integer.parseInt(parts[0].trim()) - 1;
                        double score = Double.parseDouble(parts[1].trim());
                        String reason = parts[2].trim();

                        if (index >= 0 && index < lectures.size()) {
                            Lecture lecture = lectures.get(index);
                            LectureRecommendResponse response = lectureMapper.toDto(lecture);

                            response = LectureRecommendResponse.builder()
                                    .id(response.getId())
                                    .platform(response.getPlatform())
                                    .title(response.getTitle())
                                    .teacher(response.getTeacher())
                                    .url(response.getUrl())
                                    .description(response.getDescription())
                                    .tag(lecture.getTag())
                                    .totalLessons(lecture.getTotalLessons())
                                    .recommendScore(score)
                                    .recommendReason(reason)
                                    .difficulty("보통")
                                    .isPersonalized(true)
                                    .subject(request.getSubject()) // 🔧 extractSubject 대신 직접 설정
                                    .build();

                            recommendations.add(response);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("GPT 응답 파싱 실패: {} (오류: {})", line, e.getMessage());
            }
        }

        log.info("GPT 파싱 완료: {}개 추천 생성", recommendations.size());
        return recommendations;
    }

    private String extractSubject(String text) {
        if (text == null) return "기타";
        text = text.toLowerCase();

        if (text.contains("수학ⅰ") || text.contains("수학1")) return "수학Ⅰ";
        if (text.contains("수학ⅱ") || text.contains("수학2")) return "수학Ⅱ";
        if (text.contains("미적분")) return "미적분";
        if (text.contains("확률과통계")) return "확률과통계";
        if (text.contains("기하")) return "기하";
        if (text.contains("수학")) return "수학";
        if (text.contains("국어")) return "국어";
        if (text.contains("영어")) return "영어";

        return "기타";
    }

    private List<LectureRecommendResponse> getTopRecommendations(
            List<LectureRecommendResponse> recommendations, int limit) {

        List<LectureRecommendResponse> sorted = recommendations.stream()
                .sorted((a, b) -> Double.compare(b.getRecommendScore(), a.getRecommendScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // 최소 3개 보장
        if (sorted.size() < 3) {
            log.warn("최종 추천 부족 ({}개), 최소 3개 필요", sorted.size());
        }

        return sorted;
    }

    private List<LectureRecommendResponse> getFallbackRecommendations(LectureRecommendRequest request) {
        log.info("AI 추천 실패, 기본 방식으로 fallback. 과목: {}", request.getSubject());

        // fallback도 과목별 조회 사용
        List<Lecture> lectures = lectureRepository.findBySubjectKeyword(
                getAlternativeSubjectKeyword(request.getSubject())
        );

        List<LectureRecommendResponse> fallbackList = lectures.stream()
                .filter(l -> l.getTag() != null)
                .limit(10) // 더 많이 가져와서 선택권 확보
                .map(lecture -> {
                    LectureRecommendResponse base = lectureMapper.toDto(lecture);
                    return LectureRecommendResponse.builder()
                            .id(base.getId())
                            .platform(base.getPlatform())
                            .title(base.getTitle())
                            .teacher(base.getTeacher())
                            .url(base.getUrl())
                            .description(base.getDescription())
                            .tag(lecture.getTag())
                            .totalLessons(lecture.getTotalLessons())
                            .recommendScore(60.0)
                            .recommendReason("기본 매칭")
                            .difficulty("보통")
                            .isPersonalized(false)
                            .subject(request.getSubject()) // 🔧 직접 설정
                            .build();
                })
                .collect(Collectors.toList());

        // fallback도 최소 3개 보장
        if (fallbackList.size() < 3 && lectures.size() >= 3) {
            log.warn("Fallback 부족 ({}개), 추가 보충", fallbackList.size());
            fallbackList = lectures.stream()
                    .limit(3)
                    .map(lecture -> {
                        LectureRecommendResponse base = lectureMapper.toDto(lecture);
                        return LectureRecommendResponse.builder()
                                .id(base.getId())
                                .platform(base.getPlatform())
                                .title(base.getTitle())
                                .teacher(base.getTeacher())
                                .url(base.getUrl())
                                .description(base.getDescription())
                                .tag(lecture.getTag())
                                .totalLessons(lecture.getTotalLessons())
                                .recommendScore(60.0)
                                .recommendReason("기본 매칭")
                                .difficulty("보통")
                                .isPersonalized(false)
                                .subject(request.getSubject()) // 🔧 직접 설정
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        log.info("Fallback 추천 완료: {}개", fallbackList.size());
        return fallbackList;
    }
}