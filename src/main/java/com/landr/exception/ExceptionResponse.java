package com.landr.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExceptionResponse {
    private int errorCode;
    private String errorMessage;
}
