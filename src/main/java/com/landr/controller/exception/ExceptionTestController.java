package com.landr.controller.exception;

import com.landr.controller.exception.dto.InvalidReq;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/exception")
@Tag(name = "Exception", description = "예외 테스트 API")
public class ExceptionTestController {

    @Operation(summary = "API 예외 발생")
    @GetMapping("/api")
    public void apiException() {
        throw new ApiException(ExceptionType.SCHEDULE_NOT_FOUND);
    }

    @Operation(summary = "Validation 예외 발생")
    @PostMapping("/validation")
    public InvalidReq validationException(@RequestBody @Valid InvalidReq req) {
        return req;
    }

    @Operation(summary = "기타 예외 발생")
    @GetMapping("/other")
    public void otherException() {
        throw new RuntimeException("예기치 못한 서버 에러 발생!");
    }
}
