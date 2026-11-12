package com.codeit.codeitspringrestexample.product.repository;

import com.codeit.codeitspringrestexample.product.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 저장소 계층: 저장 방식에 관한 코드를 한곳에 모읍니다.
 * @Repository로 Spring Bean이 되어 ProductService에 주입됩니다.
 * Spring Data 인터페이스를 상속한 저장소가 아니라 직접 구현한 메모리 저장소입니다.
 */
@Repository
public class ProductRepository {
    // key = 상품 ID, value = 상품 객체. static이므로 같은 JVM/클래스 로더에서 공유됩니다.
    // final은 Map 참조의 재할당을 막을 뿐 put/remove를 막지 않습니다.
    // 서버를 재시작하면 데이터가 사라집니다. HashMap은 동시 요청에 안전하지 않고,
    // 여러 서버 사이에서 데이터도 공유되지 않으므로 수업용 저장소로 이해하세요.
    private static final Map<Long, Product> products = new HashMap<>();

    public Product save(Product product) {
        // 동일한 ID가 있으면 기존 값을 덮어씁니다. 현재 POST를 반복하면 ID 1이 교체됩니다.
        products.put(product.getId(), product);
        return product;
    }

    public Optional<Product> findById(Long id) {
        // 값이 없으면 Optional.empty(). 예외를 낼지는 비즈니스 계층에서 결정합니다.
        return Optional.ofNullable(products.get(id));
    }

    public List<Product> findAll() {
        // Map의 값들을 새 목록으로 복사합니다. 정렬 순서는 보장하지 않습니다.
        // 목록만 복사하며 각 Product 객체까지 복제하는 깊은 복사는 아닙니다.
        return new ArrayList<>(products.values());
    }

    public void delete(Long id) {
        // 없는 ID를 삭제해도 예외가 발생하지 않습니다. 삭제 API는 아직 연결되어 있지 않습니다.
        products.remove(id);
    }
}
