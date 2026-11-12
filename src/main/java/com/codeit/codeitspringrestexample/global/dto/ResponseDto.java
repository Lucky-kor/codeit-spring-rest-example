package com.codeit.codeitspringrestexample.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 성공 응답의 공통 봉투(envelope): {"success":true,"data":...,"message":"성공"}.
 * T는 data의 타입 매개변수입니다. 예를 들어 ResponseDto<ProductResponseDto>로 사용합니다.
 * HTTP 상태 코드는 ResponseEntity가 정하고, 이 객체는 본문 구조를 정합니다.
 * 현재 HATEOAS 조회는 EntityModel을 직접 반환하므로 모든 API가 이 봉투를 쓰지는 않습니다.
 * Lombok이 모든 필드 생성자와 JSON 직렬화에 사용할 getter를 생성합니다.
 */
@AllArgsConstructor
@Getter
public class ResponseDto<T> {
    @Schema(example = "true")
    private boolean success;
    // 단일 DTO, 목록 등 응답마다 달라지는 실제 데이터입니다.
    private T data;
    @Schema(example = "성공 응답")
    private String message;
}
