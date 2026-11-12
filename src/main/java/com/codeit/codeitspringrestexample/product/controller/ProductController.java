package com.codeit.codeitspringrestexample.product.controller;

import com.codeit.codeitspringrestexample.global.dto.ResponseDto;
import com.codeit.codeitspringrestexample.product.dto.ProductRequestDto;
import com.codeit.codeitspringrestexample.product.dto.ProductResponseDto;
import com.codeit.codeitspringrestexample.product.entity.Product;
import com.codeit.codeitspringrestexample.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * HTTP 진입점. 학습자료의 요청/응답 처리, HATEOAS, 캐싱, 버전 관리를 비교하는 Controller입니다.
 *
 * 요청 흐름: HTTP JSON → 요청 DTO/검증 → Service → Repository → Product
 * 응답 흐름: Product → 응답 DTO(또는 버전 예제의 Product) → JSON + HTTP 상태/헤더
 *
 * @RestController는 반환 객체를 응답 본문으로 쓰게 합니다(뷰 이름으로 해석하지 않음).
 * @RequiredArgsConstructor는 final 필드 생성자를 만들어 Service를 주입받게 합니다.
 * @RequestMapping("/v2")와 메서드 경로를 합쳐 최종 URI를 정합니다.
 * @Validated는 메서드 인자의 @Min 같은 제약을 검사하기 위한 클래스 수준 설정입니다.
 * @Tag는 Swagger 문서 분류이며 HTTP 처리나 검증을 변경하지 않습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2")
@Validated
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {
    private final ProductService productService;

    // 아래 블록은 비활성화된 문서화 확장 예시입니다. Java 코드로 실행되지 않습니다.
    // 실제 활성화된 @ApiResponse는 아래 POST 매핑의 200 응답 설명입니다.
    // 상세 예시의 message="성공 응답"과 실제 message="성공"도 비교해 보세요.
    /*
    @Operation(summary = "상품 등록", description = "한개의 상품을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 응답 처리",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 1,
                                                    "name": "고양이용 츄르",
                                                    "price": 3500,
                                                    "updatedAt": "2026-11-12T09:03:23.093338Z"
                                                },
                                                "message": "성공 응답"
                                            }
                                            """
                            )
                    )
            )
    })
     */
    /**
     * POST /v2/products: JSON 입력을 받아 상품을 저장하고 200 OK를 반환합니다.
     * Content-Type은 요청 본문의 형식, Accept는 받고 싶은 응답 형식을 나타냅니다.
     * @RequestBody가 JSON을 ProductRequestDto로 역직렬화하고 @Valid가 제약을 검사합니다.
     * 검증 실패 시 아래 본문은 실행되지 않고 전역 예외 처리기가 400을 반환합니다.
     *
     * 학습 포인트: 생성 API는 보통 201 Created + Location 헤더로 설계할 수 있습니다.
     * 현재 예제는 200이며, 상태/헤더/문서 설명을 함께 바꾸는 것은 README의 후속 실습입니다.
     */
    @PostMapping("/products")
    @ApiResponse(responseCode = "200", description = "정상 응답")
    public ResponseEntity<ResponseDto<ProductResponseDto>> postProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        // 요청 DTO를 내부 모델로 변환합니다. 시간은 클라이언트 입력 대신 서버가 정합니다.
        // 주의: ID가 1L로 고정되어 두 번째 POST는 새 상품 추가가 아니라 ID 1의 값을 덮어씁니다.
        // 현재 코드의 생성 동작을 관찰한 뒤 저장소에서 ID를 발급하는 방식으로 확장해 보세요.
        Product createdProduct = productService.create(
                new Product(
                        1L, requestDto.name(),requestDto.price(), Instant.now()
                )
        );

        // 내부 모델을 외부 응답 DTO로 변환합니다. 지금은 필드가 같아 보여도 책임은 다릅니다.
        // API가 늘어나면 반복되는 매핑을 별도 메서드/매퍼로 추출할 수 있습니다.
        ProductResponseDto resultDto = new ProductResponseDto(
                createdProduct.getId(),
                createdProduct.getName(),
                createdProduct.getPrice(),
                createdProduct.getUpdatedAt()
        );

        // ResponseEntity는 HTTP 상태/헤더/본문을, ResponseDto는 본문의 공통 구조를 담당합니다.
        // @RestController의 메시지 변환기가 이 Java 객체를 JSON으로 직렬화합니다.
        return ResponseEntity.ok(new ResponseDto(true, resultDto, "성공"));
    }

    @Operation(summary = "단일 상품 조회", description = "한개의 상품을 식별자를 사용하여 조회합니다.")
    // 일반 조회 예제: 아래 캐시 API와 달리 ETag 비교/Cache-Control을 직접 구현하지 않습니다.
    // @PathVariable은 URI의 {product-id}를 Long으로 변환해 id에 넣습니다.
    // @Min(0)은 0도 허용합니다. 양수만 허용하려면 @Positive 또는 @Min(1)을 검토하세요.
    // 음수는 400이지만 숫자가 아닌 경로는 타입 변환 오류이며 이 예제의 사용자 정의 핸들러 대상이 아닙니다.
    // 같은 경로의 버전별 메서드도 있어 실습 요청에는 Accept를 명시해 구분합니다.
    @GetMapping("/products/{product-id}")
    public EntityModel getProduct(@PathVariable("product-id") @Min(0) Long id) {
        Product findProduct = productService.findProduct(id);

        ProductResponseDto resultDto = new ProductResponseDto(
                findProduct.getId(),
                findProduct.getName(),
                findProduct.getPrice(),
                findProduct.getUpdatedAt()
        );

        // HATEOAS: 상품 정보와 함께 다음 요청에 사용할 링크를 응답에 포함합니다.
        // EntityModel이 데이터를 감싸고 JSON의 _links에 관계(rel)와 주소(href)를 표현합니다.
        // methodOn은 링크 생성을 위한 메서드 호출 정보를 기록하는 프록시입니다.
        // 여기서 getProduct(id)를 적어도 실제 조회 비즈니스 로직을 재귀 실행하지 않습니다.
        // linkTo는 매핑 정보를 바탕으로 URI를 만들고 withSelfRel은 관계 이름을 self로 정합니다.
        // 주의: 현재 두 링크는 같은 주소/같은 self 관계입니다. 중복이므로 추가 탐색 의미가 없습니다.
        // 후속 실습에서는 하나를 cache나 목록 조회처럼 다른 관계의 링크로 바꾸세요.
        return EntityModel.of(resultDto,
                linkTo(methodOn(
                        ProductController.class)
                        .getProduct(id)
                ).withSelfRel(),
                linkTo(methodOn(
                        ProductController.class)
                        .getProduct(id)
                ).withSelfRel()
        );

//        // 이전 공통 봉투 응답과 HATEOAS 표현을 비교하기 위해 남겨 둔 비활성 코드입니다.
//        return ResponseEntity.ok(new ResponseDto(true, resultDto, "성공"));
    }

    /**
     * GET /v2/products/{product-id}/cache: HTTP 조건부 요청 예제입니다.
     * 1) 첫 요청: 서버가 200 + 본문 + ETag + Cache-Control을 보냅니다.
     * 2) 재검증 요청: 클라이언트가 보관한 ETag를 If-None-Match에 넣어 보냅니다.
     * 3) 태그 일치: 304 + 본문 없음. 클라이언트는 이전에 보관한 본문을 사용합니다.
     * 4) 태그 불일치: 200 + 현재 본문 + 새 태그를 보냅니다.
     *
     * HTTP 캐시 재사용을 돕는 코드이며 서버 내부 조회 결과를 저장하는 @Cacheable은 아닙니다.
     * 현재 구현은 304일 때도 먼저 Service/Repository 조회를 수행합니다.
     * @RequestHeader(required=false)이므로 첫 요청처럼 헤더가 없어도 호출할 수 있습니다.
     * 이 메서드에는 @Min 제약이 없어 음수 ID도 조회 단계까지 갑니다.
     */
    @Operation(summary = "단일 상품 조회(캐싱)", description = "한개의 상품을 식별자를 사용하여 조회합니다. 캐싱된 데이터가 있을경우 반환하지 않아요.")
    @GetMapping("/products/{product-id}/cache")
    public ResponseEntity getProductByCache(@PathVariable("product-id") Long id,
                                            @RequestHeader(value = "If-None-Match", required = false) String inm) {
        // 태그를 판단할 최신 상품을 가져옵니다. 없는 ID이면 캐시 비교 전에 404입니다.
        Product findProduct = productService.findProduct(id);

        // ETag는 현재 표현의 식별값입니다. 여기서는 updatedAt의 epoch 밀리초에 큰따옴표를 붙입니다.
        // 예: "1790000000000". 요청에 복사할 때도 큰따옴표를 포함해야 합니다.
        // 같은 밀리초에 내용이 바뀌면 태그 충돌 가능성이 있어 완전한 변경 감지는 아닙니다.
        // 모든 변경에서 updatedAt을 갱신하고, 더 엄밀하게는 내용 해시/버전 번호를 검토하세요.
        String eTag = "\"" + findProduct.getUpdatedAt().toEpochMilli() + "\"";

        // 학습용으로 헤더 전체 문자열의 완전 일치만 확인합니다.
        // 실제 If-None-Match는 여러 태그, 약한 태그(W/), * 등도 처리해야 합니다.
        // 따라서 이 비교는 HTTP 조건부 요청의 모든 규칙을 구현한 것은 아닙니다.
        // 이 코드에서 eTag는 문자열 결합으로 만들어져 null이 아니므로 null 검사는 중복입니다.
        if(eTag != null && eTag.equals(inm)) {
            return ResponseEntity
                    .status(304)
                    .eTag(eTag)
                    .build();
        }
        // 태그가 없거나 다르면 현재 표현을 200으로 반환합니다.
        // 불일치만으로 서버 데이터가 반드시 바뀌었다고 단정할 수는 없습니다(첫 요청도 여기에 해당).
        // 304 분기는 본문을 만들지 않고, 여기서는 응답 DTO/공통 봉투를 구성합니다.
        ProductResponseDto resultDto = new ProductResponseDto(
                findProduct.getId(),
                findProduct.getName(),
                findProduct.getPrice(),
                findProduct.getUpdatedAt()
        );
        return ResponseEntity
                .status(200)
                .eTag(eTag)
                // max-age=60: 캐시가 신선한 60초 동안은 재검증 없이 재사용할 수 있습니다.
                // must-revalidate: 신선도가 만료된 뒤에는 재검증 없이 오래된 응답을 재사용하지 않습니다.
                // 매 요청 재검증을 뜻하지 않으며 curl 자체는 브라우저 같은 캐시를 자동 운용하지 않습니다.
                // 추가 실습: 현재 304 응답에도 캐시 정책 헤더가 일관되게 나가는지 살펴보세요.
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).mustRevalidate())
                .body(new ResponseDto(true, resultDto, "성공") );

    }

    /**
     * 미디어 타입을 이용한 버전 관리: 같은 URI라도 Accept에 따라 핸들러를 선택합니다.
     * 현재 /v2는 URI 버전 예시이면서 아래 미디어 타입 버전과 함께 사용되어 있습니다.
     * 두 전략을 비교하는 수업 코드이며, 실무에서는 버전의 의미와 정책을 명확히 정해야 합니다.
     *
     * 예: Accept: application/vdn.codeit.v1+json → findProductVersion
     * 예: Accept: application/vdn.codeit.v2+json → findProductVersion2
     * 일반 조회 예: Accept: application/hal+json → getProduct
     * Accept 생략 또는 와일드카드 요청은 여러 후보가 생길 수 있으므로 특정 메서드 선택을 가정하지 마세요.
     * 주의: 원본의 vdn은 관례적인 vendor 접두사 vnd와 철자가 다릅니다.
     * 여기서는 기존 매핑을 유지하므로 요청도 vdn을 그대로 사용해야 합니다.
     */
    @GetMapping(value = "/products/{product-id}", produces = "application/vdn.codeit.v1+json")
    public ResponseEntity findProductVersion(@PathVariable("product-id") @Min(0) Long id) {
        // 버전 예제는 응답 DTO 변환을 생략하고 Product를 직접 노출합니다.
        // 일반/HATEOAS 조회와 응답 구조가 다르므로 클라이언트 계약을 비교해 보세요.
        Product findProduct = productService.findProduct(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(true, findProduct, "v1 응답입니다."));
    }

    // v2도 현재는 같은 필드를 반환하며 message만 다릅니다. 필드 구조 차이는 추가 실습 대상입니다.
    @GetMapping(value = "/products/{product-id}", produces = "application/vdn.codeit.v2+json")
    public ResponseEntity findProductVersion2(@PathVariable("product-id") @Min(0) Long id) {
        Product findProduct = productService.findProduct(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(true, findProduct, "v2 응답입니다."));
    }
}
