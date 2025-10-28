package io.github.mgluizbrito.PdfSorgu.controller.common;

import io.github.mgluizbrito.PdfSorgu.dto.ExceptionResponse;
import io.github.mgluizbrito.PdfSorgu.dto.ExceptionsField;
import io.github.mgluizbrito.PdfSorgu.exceptions.InvalidFieldException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ExceptionResponse handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        List<ExceptionsField> ErrosList = fieldErrors
                .stream()
                .map(fe -> new ExceptionsField(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ExceptionResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de Validação",
                ErrosList);
    }

    @ExceptionHandler(InvalidFieldException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ExceptionResponse handlerInvalidFieldException(InvalidFieldException e) {
        return new ExceptionResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(), "Invalid Field", List.of(new ExceptionsField(e.getField(), e.getMessage()))
        );
    }

//    @ExceptionHandler(AccessDeniedException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ExceptionResponse handlerAcessoNegadoException(AccessDeniedException e){
//        return new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "ACESSO NEGADO", List.of());
//    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleGenericException(RuntimeException e) {
        return ExceptionResponse.defaultResponse("An unexpected error occurred: " + e.getMessage());
    }
}
