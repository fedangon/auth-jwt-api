package com.fedangon.authjwtapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        // Padroniza erros de negocio/validacao em formato ProblemDetail
        if (ex.getStatus().is5xxServerError()) {
            log.error("Erro da API: status={} code={} message={}", ex.getStatus().value(), ex.getCode(), ex.getMessage(), ex);
        } else if (ex.getStatus().is4xxClientError()) {
            log.info("Erro da API: status={} code={} message={}", ex.getStatus().value(), ex.getCode(), ex.getMessage());
        } else {
            log.warn("Erro da API: status={} code={} message={}", ex.getStatus().value(), ex.getCode(), ex.getMessage());
        }
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problemDetail.setTitle(ex.getStatus().getReasonPhrase());
        problemDetail.setProperty("code", ex.getCode());
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // Retorna os erros de validacao por campo
        log.info("Falha de validacao: errors={}", ex.getBindingResult().getErrorCount());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail("One or more fields are invalid.");
        problemDetail.setProperty("code", "validation_error");
        problemDetail.setProperty("timestamp", Instant.now().toString());

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        log.info("Corpo da requisicao invalido: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Malformed request");
        problemDetail.setDetail("Request body is missing or invalid.");
        problemDetail.setProperty("code", "malformed_request");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // Evita vazar detalhes internos em excecoes nao previstas
        log.error("Erro inesperado", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("Unexpected error.");
        problemDetail.setProperty("code", "internal_error");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }
}
