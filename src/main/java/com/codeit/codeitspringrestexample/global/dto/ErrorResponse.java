package com.codeit.codeitspringrestexample.global.dto;

import com.codeit.codeitspringrestexample.global.exception.ExceptionCode;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 오류 응답 본문. HTTP 상태는 예외 처리기의 ResponseEntity가 별도로 설정합니다.
 * 업무 오류는 status/message, 입력 오류는 fieldErrors 또는 violationErrors를 사용합니다.
 * 주의: 검증 오류용 생성자는 status/message를 설정하지 않습니다. 따라서 현재 본문에는
 * status=0, message=null이 나올 수 있지만 실제 HTTP 상태는 400입니다.
 * 이 불일치는 통일된 오류 응답 설계 실습에서 개선할 대상으로 남겨 둡니다.
 */
@Getter
public class ErrorResponse {
    private int status;
    private String message;
    // 요청 DTO의 필드별 검증 결과입니다. 사용하지 않는 오류 목록은 null입니다.
    private List<FieldError> fieldErrors;
    // 경로/메서드 인자 등 제약 위반 결과입니다.
    private List<ConstraintViolationError> violationErrors;

    private ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    private ErrorResponse(final List<FieldError> fieldErrors,
                          final List<ConstraintViolationError> violationErrors) {
        this.fieldErrors = fieldErrors;
        this.violationErrors = violationErrors;
    }

    // 오버로딩된 정적 팩터리: 입력 타입에 맞는 오류 응답 생성 방식을 선택합니다.
    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(FieldError.of(bindingResult), null);
    }

    public static ErrorResponse of(Set<ConstraintViolation<?>> violations) {
        return new ErrorResponse(null, ConstraintViolationError.of(violations));
    }

    public static ErrorResponse of(ExceptionCode exceptionCode) {
        return new ErrorResponse(exceptionCode.getStatus(), exceptionCode.getMessage());
    }

    public static ErrorResponse of(HttpStatus httpStatus) {
        return new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase());
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return new ErrorResponse(httpStatus.value(), message);
    }

    // Spring의 FieldError와 이름이 같아 아래에서는 Spring 타입을 전체 패키지명으로 적습니다.
    @Getter
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String reason;

        private FieldError(String field, Object rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<FieldError> of(BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors =
                    bindingResult.getFieldErrors();
            // Spring 검증 결과를 외부에 공개할 최소 정보(필드, 거부 값, 사유)로 변환합니다.
            // 여러 제약이 실패하면 목록이 되며, 오류 순서에 의존하지 않아야 합니다.
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            // null을 빈 문자열로 바꾸므로 원래 null/빈 문자열의 구분은 사라집니다.
                            error.getRejectedValue() == null ?
                                    "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }

    // Bean Validation 제약 위반을 API 응답용 구조로 변환하는 중첩 클래스입니다.
    @Getter
    public static class ConstraintViolationError {
        private String propertyPath;
        private Object rejectedValue;
        private String reason;

        private ConstraintViolationError(String propertyPath, Object rejectedValue,
                                         String reason) {
            this.propertyPath = propertyPath;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<ConstraintViolationError> of(
                Set<ConstraintViolation<?>> constraintViolations) {
            return constraintViolations.stream()
                    .map(constraintViolation -> new ConstraintViolationError(
                            constraintViolation.getPropertyPath().toString(),
                            // 현재는 거부 값이 null이면 NPE가 생길 수 있습니다.
                            // @NotNull 등으로 확장할 때 null 안전한 변환을 실습하세요.
                            constraintViolation.getInvalidValue().toString(),
                            constraintViolation.getMessage()
                    )).collect(Collectors.toList());
        }
    }
}