package com.codeit.codeitspringrestexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 애플리케이션의 시작점. main()을 실행하면 내장 웹 서버와 Spring 컨테이너가 시작됩니다.
 * 이 클래스의 패키지 아래를 기본 탐색 범위로 삼아 Controller, Service, Repository,
 * Configuration 등의 Bean을 찾아 등록합니다. Bean은 Spring이 생성·관리하는 객체입니다.
 */
@SpringBootApplication
public class CodeitSpringRestExampleApplication {

    public static void main(String[] args) {
        // HTTP 요청을 받을 준비를 하고, 생성자 주입으로 각 계층의 의존 관계를 연결합니다.
        SpringApplication.run(CodeitSpringRestExampleApplication.class, args);
    }

}
