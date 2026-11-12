package com.codeit.codeitspringrestexample.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 여러 건을 반환할 때 쓸 수 있는 목록 + 페이지 정보 구조입니다.
 * 예: MultiResponseDto<ProductResponseDto>. 현재 실행되는 API에는 연결되지 않았습니다.
 * 페이징 실습 때 Service/Repository의 조회 범위 계산과 함께 연결하세요.
 * Lombok이 전체/기본 생성자와 getter/setter를 생성합니다.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MultiResponseDto<T> {
    // 현재 페이지에 포함되는 데이터 목록입니다.
    private List<T> data;
    // 전체 데이터 개수 등 페이지 탐색에 필요한 메타데이터입니다.
    private PageInfo pageInfo;
}
