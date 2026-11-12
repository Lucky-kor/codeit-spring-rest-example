package com.codeit.codeitspringrestexample.global.advice;

import com.codeit.codeitspringrestexample.global.dto.ErrorResponse;
import com.codeit.codeitspringrestexample.global.exception.BusinessLogicException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 여러 Controller에서 발생한 예외를 공통 HTTP 오류 응답으로 바꾸는 곳입니다.
 * @RestControllerAdvice = 전역 예외 처리 + 반환 객체를 응답 본문으로 직렬화.
 * @ExceptionHandler의 대상 타입을 생략했으므로 메서드의 예외 매개변수 타입으로 대상을 정합니다.
 * 요청의 바인딩/검증에 실패하면 Controller 본문에 진입하기 전에 이곳으로 올 수도 있습니다.
 * 모든 예외를 처리하는 만능 처리기는 아닙니다. 잘못된 JSON 등은 현재 Spring 기본 처리에 맡깁니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Service에서 던진 업무 예외의 상태(현재 상품 조회에서는 404)와 메시지를 응답합니다.
    @ExceptionHandler
    public ResponseEntity handleBusinessLogicException(BusinessLogicException e) {
        return ResponseEntity
                .status(e.getExceptionCode().getStatus())
                .body(ErrorResponse.of(HttpStatus.valueOf(e.getExceptionCode().getStatus()),
                        e.getMessage()));
    }

    // 클래스의 @Validated를 통한 메서드 검증 실패. 예: 조회 경로의 음수 ID.
    // 캐시 조회에는 @Min이 없으므로 동일한 검증이 적용되지 않는 점도 비교하세요.
    @ExceptionHandler
    public ResponseEntity handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(e.getConstraintViolations()));
    }

    // @Valid @RequestBody DTO 검증 실패. 어떤 필드가 왜 거부되었는지 fieldErrors에 담습니다.
    @ExceptionHandler
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(e.getBindingResult()));
    }

    // 경로는 있지만 요청 HTTP 메서드가 지원되지 않을 때 405를 반환합니다.
    // 예: 현재 PUT /v2/products/1. 없는 경로의 404와 구분해서 관찰하세요.
    // 추가 실습: 이 사용자 정의 응답에도 Allow 헤더를 보존하도록 개선해 보세요.
    @ExceptionHandler
    public ResponseEntity handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED));
    }
}
