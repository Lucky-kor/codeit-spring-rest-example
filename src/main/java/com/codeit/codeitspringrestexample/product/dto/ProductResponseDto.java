package com.codeit.codeitspringrestexample.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * 클라이언트에게 공개할 상품 표현입니다. 내부 모델의 전체 구조를 직접 노출하지 않도록
 * Controller가 Product를 이 DTO로 변환합니다. 향후 내부 필드가 늘어도 API 계약을 유지할 수 있습니다.
 * record 접근자를 JSON 변환기가 읽어 응답 본문을 만듭니다.
 * @Schema의 example은 문서용 예시이며 실제 반환 값을 설정하지 않습니다.
 */
public record ProductResponseDto(
        @Schema(description = "상품 식별자", example = "1")
        Long id,
        @Schema(description = "상품명", example = "고양이용 츄르")
        String name,
        @Schema(description = "가격", example = "3500")
        int price,
        @Schema(description = "수정 시간", example = "2026-11-12T09:03:23.093338Z")
        Instant updatedAt
) {
}
