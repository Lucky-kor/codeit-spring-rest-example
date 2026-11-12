package com.codeit.codeitspringrestexample.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * POST 요청 JSON을 받는 DTO(Data Transfer Object).
 * 예: {"name":"고양이용 츄르","price":3500}
 * record는 필드에 대응하는 생성자와 name(), price() 접근자 등을 자동 제공합니다.
 * 필드 참조는 불변이며 setter가 없습니다. 요청에서 id/updatedAt은 받지 않습니다.
 * @Schema는 문서 설명, 검증 애너테이션은 실제 입력 제약을 담당합니다.
 * Controller의 @Valid가 이 DTO에 선언된 제약 검사를 시작합니다.
 */
public record ProductRequestDto(
        // null, 빈 문자열, 공백만 있는 이름을 거부합니다. 길이 제한은 없습니다.
        @NotBlank
        @Schema(description = "이름", example = "고양이용 츄르")
        String name,
        // 0원은 허용하고 음수는 거부합니다.
        // int는 null을 표현할 수 없으므로 필수 입력 여부를 엄밀히 다루려면
        // Integer + @NotNull 사용과 JSON 역직렬화 설정을 함께 검토해 보세요.
        @Min(0)
        @Schema(description = "가격", example = "3500")
        int price
) {
}
