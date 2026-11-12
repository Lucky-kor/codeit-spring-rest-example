package com.codeit.codeitspringrestexample.product.service;

import com.codeit.codeitspringrestexample.global.exception.BusinessLogicException;
import com.codeit.codeitspringrestexample.global.exception.ExceptionCode;
import com.codeit.codeitspringrestexample.product.entity.Product;
import com.codeit.codeitspringrestexample.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 비즈니스 계층: Controller의 HTTP 처리와 Repository의 저장 구현 사이를 연결합니다.
 * @Service는 Bean 등록, @RequiredArgsConstructor는 final 필드를 받는 생성자 생성을 담당합니다.
 * Spring은 그 생성자에 ProductRepository Bean을 주입합니다(생성자 기반 의존성 주입).
 * 이 계층은 HTTP ResponseEntity 대신 도메인 객체를 다룹니다.
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product create(Product product) {
        // 현재는 저장만 위임합니다. 중복 검사나 생성 정책은 추가 실습에서 확장할 지점입니다.
        return productRepository.save(product);
    }

    public Product findProduct(Long id) {
        // Optional에 값이 있으면 꺼내고, 없으면 업무 예외를 발생시킵니다.
        // 예외는 Controller로 전파된 뒤 GlobalExceptionHandler에서 HTTP 404로 변환됩니다.
        return productRepository.findById(id).
                orElseThrow(() -> new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND));
    }

    // 목록 조회를 위한 준비 코드. 현재 Controller에는 이를 호출하는 GET /products가 없습니다.
    public List<Product> findProducts() {
        return productRepository.findAll();
    }

    // 삭제를 위한 준비 코드. 존재 확인 정책과 DELETE 매핑은 후속 실습에서 정합니다.
    public void delete(Long id) {
        productRepository.delete(id);
    }
}
