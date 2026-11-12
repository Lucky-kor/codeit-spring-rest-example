package com.codeit.codeitspringrestexample.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 페이징 응답에 사용할 메타데이터. 값을 계산해 주는 기능은 없는 단순 DTO입니다.
 * 현재 Repository.findAll()은 전체 목록만 반환하므로 실제 페이징은 추가 구현이 필요합니다.
 */
@AllArgsConstructor
@Getter
public class PageInfo {
    // 현재 페이지 번호. 0부터인지 1부터인지는 API 설계에서 정하고 문서화해야 합니다.
    private int page;
    // 한 페이지의 최대 항목 수. 0 이하 입력을 막는 검증은 아직 없습니다.
    private int size;
    // 현재 페이지 크기가 아닌 전체 조회 대상의 개수입니다.
    private long totalElements;
    // 전체 페이지 수. size > 0일 때 ceil(totalElements / size)로 계산할 수 있습니다.
    private int totalPages;
}
