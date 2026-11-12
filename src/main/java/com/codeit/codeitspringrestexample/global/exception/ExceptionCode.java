package com.codeit.codeitspringrestexample.global.exception;

import lombok.Getter;

/**
 * 업무 오류의 HTTP 상태와 기본 메시지를 한곳에 정의한 enum입니다.
 * 404 = 찾을 수 없음, 409 = 현재 리소스 상태와 충돌함.
 * 상수를 정의하는 것만으로 검사 로직이 생기지는 않습니다.
 * 현재 사용하는 값은 PRODUCT_NOT_FOUND이며 MEMBER 관련 기능/중복 검사는 구현되어 있지 않습니다.
 */
@Getter
public enum ExceptionCode {
    MEMBER_NOT_FOUND(404, "Member Not Found"),
    MEMBER_EXISTS(409, "Member Already Exists"),
    PRODUCT_NOT_FOUND(404, "Product Not Found"),
    PRODUCT_EXISTS(409, "Product Already Exists");

    private int status;
    private String message;

    // 각 enum 상수를 생성할 때 상태 코드와 메시지를 저장합니다.
    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
