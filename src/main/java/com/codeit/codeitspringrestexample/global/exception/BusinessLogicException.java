package com.codeit.codeitspringrestexample.global.exception;

import lombok.Getter;

/**
 * 상품이 없는 상황 등 예상 가능한 업무 실패를 전달하는 사용자 정의 예외입니다.
 * RuntimeException을 상속하므로 호출 메서드마다 throws를 선언할 필요는 없습니다.
 * 여기서는 예외 정보만 전달하고, HTTP 응답 생성은 전역 예외 처리기가 담당합니다.
 */
public class BusinessLogicException extends RuntimeException {
    @Getter
    private ExceptionCode exceptionCode;

    public BusinessLogicException(ExceptionCode exceptionCode) {
        // 부모 예외에 메시지를 넣어 e.getMessage()로도 같은 설명을 읽을 수 있게 합니다.
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
