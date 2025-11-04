package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.dto.ExceptionResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Hidden
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static ExceptionResponse methodNotAllowedExceptionResponse() {
        return new ExceptionResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "The method used on this endpoint is not allowed", List.of());
    }

    private static ExceptionResponse notFoundExceptionResponse() {
        return new ExceptionResponse(HttpStatus.NOT_FOUND.value(), "The requested endpoint was not found. Please check the URL.", List.of());
    }

    @RequestMapping("/error")
    public ExceptionResponse handleError(HttpServletRequest request) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus httpStatus = HttpStatus.valueOf(status != null ? Integer.parseInt(status.toString()) : HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (httpStatus == HttpStatus.NOT_FOUND) return notFoundExceptionResponse();
        if (httpStatus == HttpStatus.METHOD_NOT_ALLOWED) return methodNotAllowedExceptionResponse();

        return ExceptionResponse.defaultResponse("An unexpected internal error occurred.");
    }
}
