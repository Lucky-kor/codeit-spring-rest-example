package com.codeit.codeitspringrestexample.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 서버 내부에서 사용하는 상품 모델입니다. DTO와 달리 저장·조회할 상품의 상태를 표현합니다.
 * 패키지 이름은 entity이지만 @Entity가 없는 일반 Java 객체이며 JPA/DB를 사용하지 않습니다.
 * Lombok이 기본 생성자, 모든 필드를 받는 생성자, getter/setter를 컴파일 시 생성합니다.
 * setter로 값을 바꾸면 Map에 저장된 같은 객체에도 변경이 반영됩니다(객체 참조 공유).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
    // 상품을 구분하는 식별자. 자동 생성되지 않으며 현재 Controller가 1L을 전달합니다.
    private Long id;
    // 상품명. 이 모델 자체에는 검증 제약이 없고 요청 DTO에서 입력을 검증합니다.
    private String name;
    // 가격. 예제에서는 정수 단위를 사용하며 통화·소수점 처리는 다루지 않습니다.
    private int price;
    // 시간대와 독립적인 시점(UTC 기준 표현). 캐시 API의 ETag를 만드는 재료입니다.
    // 수정 기능을 추가할 때는 데이터와 함께 갱신해야 캐시가 변경을 감지할 수 있습니다.
    private Instant updatedAt;
}
