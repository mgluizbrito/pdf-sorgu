package io.github.mgluizbrito.PdfSorgu.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ExceptionResponse(int status, String msg, List<ExceptionsField> list) {

    public static ExceptionResponse defaultResponse(String msg) {
        return new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), msg, List.of());
    }

    public static ExceptionResponse conflict(String msg) {
        return new ExceptionResponse(HttpStatus.CONFLICT.value(), msg, List.of());
    }

    public static ExceptionResponse notAllowed(String msg) {
        return new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), msg, List.of());
    }

    public static ExceptionResponse requiredSize(String msg) {
        return new ExceptionResponse(HttpStatus.LENGTH_REQUIRED.value(), msg, List.of());
    }

}
