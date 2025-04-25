//package com.landr.service.dto.lecture;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import java.util.List;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Getter
//@Schema(description = "강의 목록 응답")
//public class LectureListResponse {
//
//    @Schema(description = "강의 목록")
//    private List<LectureDto> lectures;
//
//    @Schema(description = "다음 페이지 존재 여부")
//    private boolean hasNext;
//
//    @Schema(description = "다음 페이지 요청 시 사용할 커서 값")
//    private Long nextCursor;
//}