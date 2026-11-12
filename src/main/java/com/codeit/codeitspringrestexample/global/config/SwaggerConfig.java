package com.codeit.codeitspringrestexample.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 명세 및 Swagger UI의 그룹을 구성합니다.
 * @Configuration 클래스의 @Bean 메서드 반환값도 Spring 관리 객체가 됩니다.
 * OpenAPI는 API를 기술하는 명세, Swagger UI는 이를 읽어 보여 주고 호출하는 화면입니다.
 * 아래 메타데이터의 ${...}는 application.yml의 springdoc.info 값에서 가져옵니다.
 * 문서 애너테이션/그룹 설정 자체가 새로운 API나 접근 권한을 만들지는 않습니다.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "${springdoc.info.title}",
                description = "${springdoc.info.description}",
                version = "${springdoc.info.version}"
        )
)
public class SwaggerConfig {
    // product 그룹에는 현재 구현된 /v2/products 하위 API가 표시됩니다.
    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("product")
                .pathsToMatch("/v2/products/**")
                .build();
    }

    // 분류 예시용 그룹. /v2/user/** Controller가 없어 현재 표시할 기능은 없습니다.
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .pathsToMatch("/v2/user/**")
                .build();
    }

    // 분류 예시용 그룹이며 인증/인가 기능은 아닙니다. 관리자 API는 구현되어 있지 않습니다.
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/v2/admin/**")
                .build();
    }
}
