package com.codeit.codeitspringrestexample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring 컨텍스트를 실제로 구성할 수 있는지 확인하는 기본 실행 점검입니다.
 * Bean 등록/주입과 설정 오류를 발견할 수 있지만 HTTP API 동작을 검증하지는 않습니다.
 * README의 단계별 실습에서 상태 코드, 응답 본문, ETag를 검증하는 테스트로 확장해 보세요.
 */
@SpringBootTest
class CodeitSpringRestExampleApplicationTests {

    @Test
    // 본문이 비어 있어도 컨텍스트 구성 중 예외가 나면 테스트는 실패합니다.
    void contextLoads() {
    }

}
