# RESTful API 수업 예제 학습 가이드

상품 API를 만들며 **HTTP 요청이 Java 코드로 들어와 저장소를 거친 뒤 JSON 응답으로 나가는 과정**을 익히는 프로젝트입니다. 이후 입력 검증, 예외 처리, HATEOAS, HTTP 캐시, 버전 관리, API 문서화로 확장합니다.

기준 학습자료는 [RESTful APIs 설계와 구현](https://calm-individual-12a.notion.site/RESTful-APIs-40dc6b7098288099a21bdab86ff529f2)입니다. 아래 순서는 교안의 다섯 단원에 현재 예제의 파일과 실습을 연결한 것입니다. 교안 전체 기능이 이 프로젝트에 모두 구현된 것은 아닙니다. 먼저 **현재 동작을 관찰**하고, 각 단계의 **추가 실습**을 직접 구현하세요. 이 학습용 주석 작업은 기존 동작을 유지합니다.

## 1. 실행 준비와 첫 요청

Java의 클래스·인터페이스·생성자·예외·제네릭·컬렉션을 알고 있으면 따라가기 쉽습니다. `record`, `Optional`, 람다는 코드 주석과 함께 확인하세요.

| 항목 | 프로젝트 설정 / 확인 사항 |
| --- | --- |
| Spring Boot | `build.gradle`: 4.1.2 |
| Java 컴파일 도구 체인 | `build.gradle`: Java 17 |
| Gradle | Wrapper 설정: 9.8.2. 별도 Gradle 설치 대신 제공된 Wrapper 사용 |
| 실행 환경 확인 | 이번 검증에서는 Gradle 실행 JVM으로 JDK 21, 프로젝트 도구 체인으로 Java 17 사용 |
| API 문서 | springdoc-openapi 3.2.0, Swagger UI |
| 저장소 | `HashMap` 기반 메모리 저장소. DB 설치 불필요 |

Gradle을 실행하는 JVM과 소스를 컴파일하는 Java 도구 체인은 다른 개념입니다. 로컬에서 Java 17 도구 체인을 찾지 못한다는 오류가 나면 JDK 17 설치 여부와 IDE의 Gradle JVM 설정을 확인하세요. 처음 실행할 때는 의존성 다운로드를 위한 인터넷 연결이 필요합니다.

프로젝트 루트에서 실행합니다.

```bash
java -version
./gradlew --version
./gradlew test
./gradlew bootRun
```

Windows에서는 `./gradlew` 대신 `gradlew.bat`를 사용합니다. macOS/Linux에서 실행 권한 오류가 나면 `chmod +x gradlew`로 권한을 부여합니다. 서버 종료는 실행 터미널에서 `Ctrl+C`입니다.

기본 주소는 `http://localhost:8080`입니다. 포트가 사용 중이면 다음처럼 바꿀 수 있습니다.

```bash
./gradlew bootRun --args='--server.port=18080'
```

아래 명령은 macOS/Linux의 bash/zsh 기준입니다. 별도 터미널을 열고 서버 포트에 맞게 주소를 설정하세요. Windows PowerShell에서는 셸 문법과 JSON 인용 방식을 조정하거나 Swagger UI를 사용하세요.

```bash
BASE_URL=http://localhost:8080

curl -i -X POST "$BASE_URL/v2/products" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{"name":"고양이용 츄르","price":3500}'

curl -i "$BASE_URL/v2/products/1" \
  -H 'Accept: application/hal+json'
```

첫 요청은 `200 OK`와 `success`, `data`, `message`를 반환합니다. `data.id`는 `1`이고 `updatedAt`은 실행 시점마다 달라집니다. 두 번째 요청은 상품 필드와 `_links`가 있는 HATEOAS 응답입니다. `-i`는 HTTP 상태와 헤더도 출력하게 합니다.

**첫 요청은 꼭 POST부터 보내세요.** 서버 시작 시 미리 저장된 상품은 없습니다. 재시작하면 저장했던 상품도 사라집니다. 현재 POST는 ID를 항상 `1`로 지정하므로 반복하면 같은 상품이 덮어써집니다.

## 2. 전체 구조와 코드 읽는 법

모든 Java 파일은 `src/main/java/com/codeit/codeitspringrestexample` 아래에 있습니다.

```text
CodeitSpringRestExampleApplication   서버 시작, 컴포넌트 탐색
product/
  controller/ProductController      URI·메서드·헤더·본문·응답 처리
  dto/ProductRequestDto             요청 JSON 구조와 입력 제약
  dto/ProductResponseDto            클라이언트에 공개할 상품 필드
  entity/Product                    서버 내부 상품 모델
  service/ProductService            업무 흐름, 조회 실패 판단
  repository/ProductRepository      Map에 상품 저장·조회·삭제
global/
  exception/ExceptionCode           업무 오류 종류와 상태/메시지
  exception/BusinessLogicException  업무 실패를 전달하는 예외
  advice/GlobalExceptionHandler     예외 → HTTP 오류 응답
  dto/ResponseDto                  성공 응답 봉투
  dto/ErrorResponse                오류 응답 구조
  dto/MultiResponseDto, PageInfo    목록·페이징 확장용 DTO
  config/SwaggerConfig             문서 메타데이터·API 그룹
```

`global/`도 `product/`와 같은 수준의 패키지입니다. 설정은 `src/main/resources/application.yml`, 의존성과 Java 설정은 `build.gradle`, 기본 실행 점검 테스트는 `src/test/java/.../CodeitSpringRestExampleApplicationTests.java`에 있습니다.

파일 바로가기:

- 시작·설정: [시작 클래스](src/main/java/com/codeit/codeitspringrestexample/CodeitSpringRestExampleApplication.java), [build.gradle](build.gradle), [application.yml](src/main/resources/application.yml)
- 상품 흐름: [Controller](src/main/java/com/codeit/codeitspringrestexample/product/controller/ProductController.java) → [Service](src/main/java/com/codeit/codeitspringrestexample/product/service/ProductService.java) → [Repository](src/main/java/com/codeit/codeitspringrestexample/product/repository/ProductRepository.java)
- 데이터 구조: [요청 DTO](src/main/java/com/codeit/codeitspringrestexample/product/dto/ProductRequestDto.java), [내부 Product](src/main/java/com/codeit/codeitspringrestexample/product/entity/Product.java), [응답 DTO](src/main/java/com/codeit/codeitspringrestexample/product/dto/ProductResponseDto.java), [성공 봉투](src/main/java/com/codeit/codeitspringrestexample/global/dto/ResponseDto.java)
- 오류 흐름: [ExceptionCode](src/main/java/com/codeit/codeitspringrestexample/global/exception/ExceptionCode.java), [업무 예외](src/main/java/com/codeit/codeitspringrestexample/global/exception/BusinessLogicException.java), [전역 처리기](src/main/java/com/codeit/codeitspringrestexample/global/advice/GlobalExceptionHandler.java), [오류 응답](src/main/java/com/codeit/codeitspringrestexample/global/dto/ErrorResponse.java)
- 확장·검증: [목록 DTO](src/main/java/com/codeit/codeitspringrestexample/global/dto/MultiResponseDto.java), [페이지 정보](src/main/java/com/codeit/codeitspringrestexample/global/dto/PageInfo.java), [Swagger 설정](src/main/java/com/codeit/codeitspringrestexample/global/config/SwaggerConfig.java), [기본 테스트](src/test/java/com/codeit/codeitspringrestexample/CodeitSpringRestExampleApplicationTests.java)

정상 요청을 다음 순서로 추적해 보세요.

```text
클라이언트의 HTTP 요청
  → Spring MVC: 경로/메서드/Accept에 맞는 핸들러 선택
  → 요청 바인딩·JSON 역직렬화·검증
  → ProductController: HTTP 입력을 내부 호출로 변환
  → ProductService: 업무 처리
  → ProductRepository: Map 접근
  → ProductController: 응답 DTO/링크/상태/헤더 구성
  → 메시지 변환기: Java 객체를 응답 JSON으로 직렬화
```

예외가 발생하면 정상 응답을 만드는 다음 줄로 진행하지 않습니다. 처리 대상 예외라면 `GlobalExceptionHandler`가 대신 응답합니다. 이 코드의 전역 처리기에 등록되지 않은 오류는 Spring의 기본 처리 경로를 따를 수 있습니다.

## 3. 교안과 연결한 권장 학습 순서

각 단계에서 **교안 읽기 → 지정 코드 읽기 → 요청 실행 → 결과 설명 → 추가 구현** 순서로 진행하세요. 기본 흐름을 먼저 이해하고 심화 기능을 한 가지씩 붙이는 순서입니다.

| 단계 | 교안에서 볼 시점 | 지금 읽을 코드 | 단계 완료 기준 |
| --- | --- | --- | --- |
| 0 | 실행 전 | `build.gradle`, 시작 클래스, `application.yml` | 서버를 켜고 POST/GET 한 번씩 성공 |
| 1 | ① REST의 이해와 실제 활용 | Controller의 매핑, Repository | 리소스·표현·무상태성을 예제와 연결 |
| 2 | ② RESTful API 설계 원칙 | `postProduct`, `ResponseDto`, 응답 DTO | 메서드·상태·헤더·본문 역할 구분 |
| 3 | ③ 기본: @RestController, 요청/응답 처리 | 요청 DTO → `postProduct` → Service → Repository | 등록 한 건을 줄 단위로 추적 |
| 4 | ③ 기본: Spring 예외 처리, 예외 처리 실습 | `findProduct`, 예외 2종, 전역 처리기, `ErrorResponse` | 400/404/405의 발생 위치와 응답 설명 |
| 5 | ② API 응답 설계 복습 | 목록 메서드, `MultiResponseDto`, `PageInfo` | 목록·삭제·페이징을 직접 연결 |
| 6 | ④ 심화: HATEOAS 기본 적용 | `getProduct`, `EntityModel`, 링크 빌더 | 링크의 관계와 주소를 설명하고 중복 개선 |
| 7 | ② HTTP 프로토콜: Cache-Control / ETag | `getProductByCache`, `updatedAt` | 200 → 304 → 변경 후 200 재현 |
| 8 | ④ 심화: API 버전 관리 | 버전 조회 메서드 2개, `produces` | Accept로 v1/v2를 명시적으로 선택 |
| 9 | ⑤ API 문서화: Swagger UI 활용 | `SwaggerConfig`, `@Schema`, 문서 애너테이션 | 명세와 실제 응답을 대조 |
| 10 | ⑤ Spring REST Docs, 문서화 전략 | 기존 테스트와 직접 추가할 API 테스트 | 실행 가능한 검증과 문서 갱신 연결 |

교안의 **리소스 간 관계 처리**는 이 예제에 별도 연관 모델이 없어 확장 과제로 진행합니다. **Spring REST Docs**도 현재 의존성·문서 생성 설정이 없습니다. Swagger UI 구현을 먼저 익힌 뒤 마지막 단계에서 비교하세요.

### 단계 1. REST 관점에서 URI와 상태 읽기

먼저 [REST의 이해와 실제 활용](https://calm-individual-12a.notion.site/REST-257c6b709828824a80db81a5e9b26f1d)과 [RESTful API 설계 원칙](https://calm-individual-12a.notion.site/RESTful-API-0eac6b70982883b2855d81656c0e84d2)의 리소스 중심 설계를 읽습니다.

`ProductController`의 클래스 수준 `@RequestMapping("/v2")`와 메서드 수준 매핑을 합쳐 최종 경로를 적어 보세요. `/products`는 상품 컬렉션, `/products/1`은 식별자가 1인 상품입니다. URI의 동사 대신 HTTP 메서드로 동작을 표현하는 이유를 설명해 보세요.

`ProductRepository`에는 상품 상태가 있지만, 요청 사이의 클라이언트 대화 상태를 세션으로 보관하는 코드는 없습니다. REST의 무상태성은 **서버가 상품 데이터를 저장하면 안 된다는 뜻이 아닙니다.** 클라이언트의 각 요청을 이해하는 데 필요한 정보를 해당 요청이 제공하는지를 생각해야 합니다. 이 예제에는 인증·세션 구현이 없습니다.

추가 실습:

1. 생성·단건 조회·전체 조회·수정·삭제의 메서드와 URI를 표로 설계합니다.
2. 코드에서 실제 `@...Mapping`을 찾아 구현 여부를 표시합니다. Service에 메서드가 있다고 HTTP API가 자동으로 생기지 않습니다.
3. `/products/{id}/cache`가 같은 상품의 캐시 실험용 경로라는 점을 설명합니다. 확장 시 동일한 상품 URI에 조건부 조회를 합치는 설계도 비교해 보세요.

완료 기준: “URI는 무엇을 가리키고, 메서드는 어떤 의미를 갖는가?”에 실제 경로로 답할 수 있습니다. 링크나 JSON을 쓴다는 사실만으로 REST의 모든 제약을 충족한다고 단정하지 마세요.

### 단계 2. HTTP 상태·헤더·응답 본문 구분하기

[HTTP 프로토콜의 효과적인 활용](https://calm-individual-12a.notion.site/HTTP-97ac6b709828820aa6ae0124e1f4ff88)의 메서드·상태 코드·헤더를 읽고 `postProduct`의 반환부와 `ResponseDto`를 봅니다.

- `ResponseEntity`: HTTP 상태와 헤더, 본문을 함께 설정합니다.
- `ResponseDto<T>`: JSON 본문의 `success`, `data`, `message` 구조입니다.
- `ProductResponseDto`: `data` 안에 넣을 상품 표현입니다.
- `Content-Type`: 지금 보내는 본문의 형식입니다. POST JSON에는 `application/json`을 보냅니다.
- `Accept`: 클라이언트가 받고 싶은 응답 형식입니다. 같은 URI의 버전 선택에도 사용됩니다.

현재 POST 성공 예시는 다음과 같습니다. 키 출력 순서는 의미가 없고 시간은 예시입니다.

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "고양이용 츄르",
    "price": 3500,
    "updatedAt": "2026-11-12T10:00:00Z"
  },
  "message": "성공"
}
```

추가 실습: 먼저 현재 `200`을 확인한 뒤, `postProduct`를 **201 Created + Location: /v2/products/{id}**로 바꿔 보세요. `ResponseEntity.created(생성된_URI).body(...)`를 출발점으로 삼고, API 문서의 응답 코드도 201로 맞춥니다. Location을 따라 GET했을 때 생성된 상품이 조회되어야 합니다. 고정 ID 문제는 다음 단계에서 함께 개선합니다.

완료 기준: 본문에 `success=true`를 적는 것과 HTTP 상태를 201로 정하는 것이 서로 다른 일임을 설명할 수 있습니다.

### 단계 3. 상품 등록을 계층별로 추적하기

[RESTful API 구현: 기본](https://calm-individual-12a.notion.site/RESTful-API-764c6b7098288385b847812f18d591d0)의 `@RestController`, 요청 처리, 응답 처리를 읽습니다. 코드 읽기 순서는 다음과 같습니다.

1. `ProductRequestDto`: record와 `@NotBlank`, `@Min(0)`.
2. `ProductController.postProduct`: `@RequestBody`, `@Valid`, DTO → Product 변환.
3. `ProductService.create`: 저장소 호출.
4. `ProductRepository.save`: `Map.put`으로 저장.
5. `Product`: 생성자·필드·Lombok 애너테이션.
6. Controller로 돌아와 `ProductResponseDto`, `ResponseDto`로 응답 생성.

가능하면 디버거의 중단점을 위 메서드들에 두고 POST를 한 번 보내세요. JSON의 `name`이 `requestDto.name()`이 되고, `new Product(...)`에 전달되고, Map에 저장된 뒤 다시 응답 DTO로 옮겨지는 것을 관찰합니다. Lombok 생성 코드는 직접 작성되어 있지 않아도 컴파일 시 생성됩니다. Spring이 객체를 주입하는 생성자와, 개발자가 `new Product(...)`로 호출하는 데이터 객체 생성자도 구분하세요.

추가 실습:

1. 이름과 가격을 바꿔 POST를 두 번 보낸 뒤 ID 1을 조회합니다. 상품이 두 개 쌓이지 않는 이유를 `1L`과 `Map.put`에서 찾습니다.
2. ID 발급을 저장소 책임으로 옮깁니다. Controller가 ID를 고정하지 않게 하고 메모리 예제에서는 증가하는 ID를 발급해 봅니다. 동시성을 고려한다면 ID 발급뿐 아니라 Map 접근도 함께 검토해야 합니다.
3. 두 번 등록한 결과의 ID가 다르고 각 ID로 원래 상품이 조회되는지 확인합니다.
4. 응답 매핑을 공통 메서드로 추출해 세 곳의 `new ProductResponseDto(...)` 중복을 줄여 봅니다.

완료 기준: Controller·Service·Repository가 각각 무엇을 책임지는지 설명하고, 서로 다른 상품 2건을 저장할 수 있습니다. 서버 재시작 후 데이터가 사라지는 현상은 메모리 저장소의 특성입니다.

### 단계 4. 입력 검증과 업무 예외 비교하기

기본 교안의 Spring 예외 처리 및 심화 예외 처리 실습을 읽은 뒤, **먼저 성공 경로를 이해한 상태에서** 다음 요청을 보냅니다. 아래는 추가 실습으로 코드를 바꾸기 전의 동작입니다.

```bash
# DTO 검증 실패: 이름 공백, 가격 음수
curl -i -X POST "$BASE_URL/v2/products" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{"name":" ","price":-1}'

# 경로 인자 검증 실패
curl -i "$BASE_URL/v2/products/-1" -H 'Accept: application/hal+json'

# 존재하지 않는 상품: 현재 고정 ID 예제에서는 999가 없음
curl -i "$BASE_URL/v2/products/999" -H 'Accept: application/hal+json'

# 경로는 있지만 PUT은 미구현
curl -i -X PUT "$BASE_URL/v2/products/1" -H 'Accept: application/json'
```

| 상황 | 발생 위치와 예외 | HTTP 상태 | 현재 본문 특징 |
| --- | --- | --- | --- |
| 이름 공백 / 음수 가격 | `@Valid` DTO 검사 → `MethodArgumentNotValidException` | 400 | `fieldErrors`에 필드·거부 값·사유 |
| 일반 조회의 음수 ID | `@Validated` + `@Min` → `ConstraintViolationException` | 400 | `violationErrors`에 인자 경로·거부 값·사유 |
| 없는 상품 | Service의 `orElseThrow` → `BusinessLogicException` | 404 | `status=404`, `message="Product Not Found"` |
| 지원하지 않는 PUT | MVC 매핑 → `HttpRequestMethodNotSupportedException` | 405 | `status=405`, `message="Method Not Allowed"` |

검증 실패 본문은 다음과 같은 구조입니다. 검증 메시지 언어와 오류 목록 순서는 환경에 따라 달라질 수 있습니다.

```json
{
  "status": 0,
  "message": null,
  "fieldErrors": [
    {"field": "name", "rejectedValue": " ", "reason": "공백일 수 없습니다"},
    {"field": "price", "rejectedValue": "-1", "reason": "0 이상이어야 합니다"}
  ],
  "violationErrors": null
}
```

여기서 **HTTP 상태는 400인데 본문의 status는 0**입니다. `ErrorResponse`의 검증 오류용 생성자가 status/message를 초기화하지 않아 생긴 현재 예제의 불일치입니다. `int` 필드는 기본값 0, 참조형 필드는 null이 됩니다. 이를 올바른 API 설계로 외우지 말고 개선 대상을 찾는 연습으로 삼으세요.

코드는 `findProduct` → `ExceptionCode` → `BusinessLogicException` → `GlobalExceptionHandler` → `ErrorResponse` 순서로 읽습니다. DTO 검증은 Controller 본문 진입 전 실패할 수 있고, 상품 존재 여부는 Service가 판단한다는 차이를 설명합니다.

추가 실습:

1. 정상 입력 `price=0`과 조회 `id=0`을 비교합니다. 현재 `@Min(0)`은 0을 허용합니다. ID 0은 검증을 통과해도 저장된 상품이 없으면 404입니다.
2. `/products/-1/cache`도 호출합니다. 캐시 메서드에는 `@Min`이 없어 현재는 400 대신 조회 실패 404가 됩니다. 모든 조회 경로의 ID 검증 정책을 맞춰 봅니다.
3. 검증 오류 본문에도 `status=400`, 의미 있는 message가 채워지도록 팩터리/생성자를 개선합니다.
4. JSON 문법 오류와 숫자가 아닌 ID를 보내 봅니다. 현재 전역 처리기에 해당 핸들러가 없음을 확인하고, 원하는 오류 계약으로 통일합니다. 사용하지 않는 `HttpMessageNotReadableException` import만으로 처리 기능이 생기지는 않습니다.
5. `ConstraintViolationError` 변환에서 거부 값이 null인 경우를 안전하게 처리합니다. 가격을 필수로 하려면 `int` 대신 `Integer`와 `@NotNull`을 검토하고, 누락/null 입력도 확인합니다.
6. 405 응답에 지원 메서드를 알려 주는 `Allow` 헤더가 있는지 확인하고 보완합니다.

완료 기준: 잘못된 입력과 존재하지 않는 상품을 서로 다른 책임으로 설명하고, HTTP 상태와 본문 상태를 일치시킬 수 있습니다.

### 단계 5. 목록·삭제·페이징 API 직접 연결하기

이 단계는 교안의 리소스 설계·API 응답 설계를 실제 구현으로 옮기는 **추가 실습**입니다. 현재 동작하는 API로 착각하지 않도록 주의하세요.

먼저 `ProductService.findProducts`, `delete`, `ProductRepository.findAll`, `delete`를 읽습니다. 이어 `MultiResponseDto`, `PageInfo`를 읽습니다. 현재 목록·삭제 Controller 매핑은 없고, 페이징 계산도 없습니다.

1. `GET /v2/products`를 만들고 Service의 전체 조회 결과를 응답 DTO 목록으로 변환합니다. 빈 저장소에서는 빈 목록을 반환하게 합니다.
2. `DELETE /v2/products/{id}`를 연결하고 성공 시 `204 No Content`로 본문 없이 응답하게 합니다. 없는 ID를 삭제할 때 404로 할지, 동일하게 204로 할지 정책을 정해 문서화합니다. 반복 DELETE의 멱등성은 응답이 매번 동일해야 한다는 뜻이 아니라 최종 리소스 상태와 관련됩니다.
3. `page`, `size` 쿼리 인자를 추가합니다. 예를 들어 1부터 시작하는 page와 양수 size를 정하고 검증합니다.
4. ID 순 정렬 등 안정된 순서를 정한 뒤 페이지 범위만 잘라 `MultiResponseDto<ProductResponseDto>`에 담습니다. HashMap 순서에 의존하면 안 됩니다.
5. `PageInfo`의 `totalElements`는 전체 개수, `totalPages`는 전체 페이지 수로 계산합니다. 빈 목록과 마지막 페이지, 범위를 벗어난 페이지의 정책도 정합니다.

완료 기준: 상품 3건·size 2 조건에서 첫 페이지 2건, 다음 페이지 1건, 전체 개수 3, 전체 페이지 2가 일치합니다. 삭제 후 다시 조회하면 정한 정책대로 404를 받습니다. 이 단계는 ID 발급 실습을 마친 뒤 진행해야 여러 상품으로 검증할 수 있습니다.

### 단계 6. HATEOAS 응답과 링크 이해하기

[HATEOAS 기본 적용](https://calm-individual-12a.notion.site/HATEOAS-587c6b709828836fa73f01a26992853a)을 읽고 `ProductController.getProduct`의 반환부를 봅니다.

```bash
curl -i "$BASE_URL/v2/products/1" -H 'Accept: application/hal+json'
```

현재 응답은 공통 `ResponseDto` 봉투 없이 상품 필드와 `_links`를 반환합니다. 원본은 같은 `self` 링크를 두 번 추가하여 HAL 응답의 `self`가 동일한 링크 2개를 담은 배열로 나타납니다.

```json
{
  "id": 1,
  "name": "고양이용 츄르",
  "price": 3500,
  "updatedAt": "2026-11-12T10:00:00Z",
  "_links": {
    "self": [
      {"href": "http://localhost:8080/v2/products/1"},
      {"href": "http://localhost:8080/v2/products/1"}
    ]
  }
}
```

`EntityModel.of`는 데이터에 링크를 붙입니다. `methodOn`은 링크를 만들 메서드 호출 정보를 기록하는 프록시이고, 해당 메서드의 실제 업무 로직을 재귀 실행하지 않습니다. `linkTo`는 매핑 정보를 이용해 주소를 만들며 `withSelfRel()`은 관계 이름을 `self`로 정합니다.

추가 실습:

1. 중복 `self` 하나를 제거하고, 캐시 조회를 가리키는 `cache` 관계 링크로 교체합니다. 응답의 주소를 직접 따라가 200을 확인합니다.
2. 단계 5의 목록 API를 구현했다면 목록으로 이동하는 `products` 관계를 추가합니다. 없는 API를 가리키는 링크를 먼저 만들지 마세요.
3. 목록 표현에는 `CollectionModel`을 적용해 보고, 공통 봉투 응답과 HAL의 차이를 클라이언트 관점에서 비교합니다.

완료 기준: 응답을 읽은 클라이언트가 링크의 rel/href로 다음 요청을 만들 수 있고, 링크가 실제 구현된 경로로 연결됩니다.

### 단계 7. ETag와 조건부 GET 실험하기

HTTP 교안의 **Cache-Control / ETag**를 읽고 `getProductByCache`를 위에서 아래로 봅니다. 상품 조회 → updatedAt으로 ETag 생성 → 요청 헤더 비교 → 304 또는 200 순서입니다.

```bash
# 1) 첫 응답의 ETag 값을 확인합니다.
curl -i "$BASE_URL/v2/products/1/cache" -H 'Accept: application/json'

# 2) 아래 값을 방금 받은 ETag로 교체합니다. 큰따옴표도 포함합니다.
ETAG='"여기에_응답의_숫자_값"'
curl -i "$BASE_URL/v2/products/1/cache" \
  -H 'Accept: application/json' \
  -H "If-None-Match: $ETAG"
```

첫 요청은 `200` + 본문 + `ETag` + `Cache-Control: max-age=60, must-revalidate`입니다. 두 번째 요청의 태그가 정확히 같으면 `304 Not Modified`와 ETag만 있고 본문은 없습니다. 클라이언트는 이미 보관한 본문을 사용합니다. 위 ETAG의 예시 문자를 그대로 보내면 일치하지 않아 200이 나오므로 반드시 실제 값을 복사하세요.

`max-age=60`은 캐시가 신선한 60초 동안 재검증 없이 재사용할 수 있다는 뜻입니다. `must-revalidate`는 신선도가 만료된 뒤 재검증 없이 오래된 응답을 재사용하지 못하게 합니다. 매번 서버에 확인하라는 뜻은 아닙니다. `curl`은 브라우저처럼 이 정책에 따라 응답을 자동 캐싱하지 않으므로 위 실습은 조건부 요청을 직접 보내는 방식입니다.

이 코드는 서버 내부 결과 캐시(`@Cacheable`)가 아닙니다. **304일 때도 Repository 조회가 실행됩니다.** 절약하는 것은 주로 응답 본문 전송입니다.

추가 실습:

1. **원본의 고정 ID를 유지 중이라면** 이름/가격을 바꿔 POST하여 상품 1을 덮어쓴 뒤 옛 ETag로 조회합니다. 새 ETag와 200이 나오는지 봅니다.
2. **단계 3에서 ID 발급을 개선했다면** POST는 다른 상품을 생성합니다. 기존 상품 변경을 관찰하려면 먼저 수정 API를 추가하고, 수정 시 `updatedAt`도 갱신합니다. 그 후 같은 ID에 대해 옛 ETag로 200이 나오는지 확인하세요.
3. 304 응답에는 현재 Cache-Control이 빠져 있습니다. 200/304에서 정책을 일관되게 전달하도록 보완합니다.
4. 수정했는데 updatedAt 갱신을 빼먹으면 어떤 문제가 생기는지 확인합니다. 밀리초 기반 태그는 같은 밀리초의 변경을 놓칠 수 있으므로 내용 해시나 변경 버전을 비교합니다.
5. 원본은 문자열 완전 일치만 검사합니다. 여러 ETag, 약한 태그(`W/`), `*`까지 고려하는 조건부 요청 처리로 확장해 봅니다. Spring의 조건부 요청 지원을 조사하고 현재 구현과 비교하세요.

완료 기준: 태그 없음 → 200, 같은 태그 → 304·빈 본문, 기존 상품 변경 후 옛 태그 → 200을 재현하고 각각의 이유를 설명할 수 있습니다.

### 단계 8. Accept로 API 버전 선택하기

[API 버전 관리](https://calm-individual-12a.notion.site/API-800c6b70982883f0871101163234ef14)를 읽고 `findProductVersion`, `findProductVersion2`와 `produces`를 비교합니다.

```bash
curl -i "$BASE_URL/v2/products/1" \
  -H 'Accept: application/vdn.codeit.v1+json'

curl -i "$BASE_URL/v2/products/1" \
  -H 'Accept: application/vdn.codeit.v2+json'
```

현재 예제는 **`vdn`**을 사용합니다. 일반적인 vendor 접두사 `vnd`와 철자가 다르지만, 원본 매핑과 일치시키기 위해 실습 요청도 `vdn`으로 작성했습니다. 코드와 요청을 함께 고치기 전에는 임의로 바꾸지 마세요.

동일 URI에서 `Accept`와 `produces`의 매칭으로 메서드가 달라집니다. 현재 v1/v2는 상품 필드는 같고 message만 각각 `v1 응답입니다.`, `v2 응답입니다.`로 다릅니다. 이 메서드들은 `ProductResponseDto` 대신 내부 `Product`를 직접 반환하므로, 앞의 일반 조회와 구조/책임도 비교하세요.

클래스 경로 `/v2`는 URI 기반 버전 예시이고, 여기의 Accept 버전은 별도 조건입니다. `SwaggerConfig`의 문서 정보 버전 `V1`도 또 다른 메타데이터입니다. **URI /v2, Accept의 v1/v2, 문서 V1이 자동으로 동기화되지는 않습니다.**

추가 실습:

1. 세 조회 메서드에 중단점을 두고 HAL/v1/v2 Accept를 바꿔 선택된 메서드를 확인합니다. Accept 생략이나 `*/*`에서는 특정 메서드를 선택한다고 가정하지 마세요.
2. v1은 그대로 두고 v2에만 새로운 응답 필드를 추가합니다. 버전별 응답 DTO를 사용해 내부 모델을 직접 공개하지 않도록 개선합니다.
3. 기존 클라이언트의 v1 필드와 동작이 유지되는지 검증합니다. 필드 추가·이름 변경·타입 변경의 호환성 차이를 토론합니다.
4. `vdn`을 `vnd`로 정리한다면 매핑, README 요청, 테스트와 문서를 함께 바꾸고 기존 클라이언트 전환 정책을 정합니다.

완료 기준: 같은 URI에 서로 다른 Accept를 보내 의도한 버전의 응답을 받고, 변경 시 어떤 클라이언트가 영향을 받는지 설명할 수 있습니다.

### 단계 9. Swagger UI와 실제 응답 대조하기

[Swagger UI 활용](https://calm-individual-12a.notion.site/Swagger-UI-127c6b70982883faa4b9813f79d86fc1)을 읽고 다음 순서로 코드를 봅니다.

1. `build.gradle`의 springdoc 의존성: OpenAPI 생성 및 UI 제공.
2. `application.yml`: `/docs` 경로와 문서 메타데이터.
3. `SwaggerConfig`: `@OpenAPIDefinition`, `GroupedOpenApi`, 경로별 그룹.
4. Controller의 `@Tag`, `@Operation`, `@ApiResponse`.
5. DTO의 `@Schema`: 설명·예시.

서버 실행 후 다음 주소를 엽니다. 포트를 바꿨다면 주소도 바꿉니다.

- [Swagger UI](http://localhost:8080/docs): API를 읽고 직접 요청하는 화면.
- [전체 OpenAPI JSON](http://localhost:8080/v3/api-docs): 도구가 읽는 명세.
- [product 그룹 JSON](http://localhost:8080/v3/api-docs/product): 상품 API 그룹 명세.

UI에서 `product` 그룹을 선택하고 POST의 **Try it out**으로 상품을 등록합니다. 그다음 조회를 호출하며 **Response Code, Response Headers, Response Body**를 비교하세요. 같은 경로의 여러 produces 매핑은 OpenAPI에서 하나의 GET 작업으로 합쳐질 수 있으므로 버전 선택 실험은 앞의 명시적인 curl 요청도 병행합니다.

현재 `user`, `admin` 그룹은 분류 예시뿐이고 대응 Controller가 없습니다. 그룹을 만들었다고 사용자·관리자 API나 권한 제어가 생기는 것은 아닙니다.

추가 실습:

1. POST 위의 큰 주석 블록은 비활성 문서 예시입니다. 활성 `@ApiResponse`와 중복되지 않게 정리한 뒤 상세 설명/예시를 적용합니다.
2. 문서 예시의 `message="성공 응답"`과 실제 `message="성공"`을 일치시킵니다. 단계 2에서 201로 바꿨다면 문서도 201로 바꿉니다.
3. 검증 400, 상품 없음 404, 캐시 조회의 200/304를 실제 형식에 맞게 문서화합니다. 캐시가 있다는 이유만으로 304가 나오는 것이 아니라 태그 일치가 조건임을 설명합니다.
4. raw `ResponseEntity`/`EntityModel`과 raw `new ResponseDto(...)`를 적절한 제네릭 타입으로 보완하여 타입 안전성과 자동 문서의 스키마를 개선합니다.
5. `@Schema(example=...)` 값을 바꿔도 실제 데이터나 검증 제약은 바뀌지 않는지 확인합니다. 문서 애너테이션은 실행 로직을 대신하지 않습니다.

완료 기준: 실제 요청 결과와 문서의 상태 코드·헤더·본문 예시가 일치하고, 문서 설정이 어디에서 나오는지 찾을 수 있습니다.

### 단계 10. 테스트와 문서화 전략으로 마무리하기

`CodeitSpringRestExampleApplicationTests.contextLoads`는 Spring 컨텍스트 구성 여부를 확인하는 테스트입니다. 본문이 비어 있어도 Bean 생성/주입 실패는 발견할 수 있지만, 상품 API가 올바르게 동작한다는 보장은 아닙니다.

추가 실습으로 `MockMvc` 등을 사용해 다음 동작을 자동 검증하세요. 테스트는 직접 데이터를 준비하고 다른 테스트의 실행 순서에 의존하지 않도록 합니다. 현재 저장소의 static Map은 같은 JVM에서 테스트 간 공유될 수 있으므로 테스트 데이터 초기화/격리 방법도 설계합니다.

| 테스트 시나리오 | 확인할 내용 |
| --- | --- |
| 정상 등록 → 단건 조회 | 저장된 필드와 반환 ID, 상태 코드 일치 |
| 서로 다른 상품 2건 등록 | ID 충돌 없이 각각 조회됨(ID 발급 개선 후) |
| 잘못된 DTO | HTTP 400과 오류 필드. 오류 목록 순서에 의존하지 않음 |
| 없는 상품 | HTTP 404와 업무 메시지 |
| 조건부 조회 | 첫 200의 ETag → 같은 태그 304·빈 본문 → 변경 후 옛 태그 200 |
| 버전 선택 | v1/v2 Accept마다 의도한 계약 유지 |
| 목록·삭제·페이징 | 빈 목록/마지막 페이지/삭제 후 조회 등 경계 조건(구현 후) |

교안의 Spring REST Docs는 이 프로젝트에 아직 없습니다. 위 테스트를 만든 다음 의존성과 문서 생성 구성을 추가하고, 검증한 요청/응답을 문서 스니펫으로 생성하는 방식으로 확장하세요. Swagger UI의 탐색·호출 편의와 테스트에서 생성한 문서의 동작 검증 연결을 비교하면 문서화 전략을 선택하기 쉽습니다.

검증은 프로젝트 루트에서 실행합니다.

```bash
./gradlew test
# 주석 변경 후에도 컴파일/테스트를 새로 실행하고 싶을 때
./gradlew test --rerun-tasks
```

## 4. 현재 구현된 API 빠른 참조

아래 표는 **추가 실습 전 원본 동작** 기준입니다. 단건 조회가 겹치므로 Accept를 명시했습니다.

| 메서드 | 경로 | Accept / 입력 | 정상 결과 |
| --- | --- | --- | --- |
| POST | `/v2/products` | JSON 본문 + `application/json` | 200, 공통 봉투 안의 상품 DTO, ID 1 저장 |
| GET | `/v2/products/{product-id}` | `application/hal+json` | 200, 상품 DTO + 중복 self 링크 |
| GET | `/v2/products/{product-id}/cache` | `application/json`, 선택적 `If-None-Match` | 200 + 본문/ETag 또는 태그 일치 시 304·빈 본문 |
| GET | `/v2/products/{product-id}` | `application/vdn.codeit.v1+json` | 200, 공통 봉투 + 내부 Product, v1 메시지 |
| GET | `/v2/products/{product-id}` | `application/vdn.codeit.v2+json` | 200, 공통 봉투 + 내부 Product, v2 메시지 |

`GET /v2/products`, DELETE, PUT/PATCH, 실제 페이징, 회원 API, 인증/인가는 현재 구현되어 있지 않습니다. 존재하지 않는 상품은 404이며 요청 바인딩·검증 등에서 먼저 실패하면 다른 상태가 나올 수 있습니다.

## 5. 복습과 확장 과제 안내

수업을 마친 뒤 다음을 자신의 말로 설명해 보세요.

- 요청 DTO, 내부 Product, 응답 DTO를 구분하는 이유와 변환 위치.
- 생성자 주입과 Spring Bean의 관계, Lombok이 생성하는 코드.
- `@Valid`와 `@Validated`, DTO 검증과 업무 존재 검사, `@Schema`의 차이.
- HTTP 상태 코드와 본문 필드의 차이, 400/404/405의 발생 경로.
- `EntityModel`, `rel`, `href`가 클라이언트의 다음 행동에 주는 정보.
- 304에 본문이 없을 때 클라이언트가 사용할 데이터와 ETag 갱신 조건.
- URI 버전, 미디어 타입 버전, 문서 버전의 독립성.
- Map 저장소의 휘발성·객체 참조 공유·동시성 한계.

확장 과제로 교안의 **리소스 간 관계 처리**를 연결하세요. 예를 들어 Category를 추가하고 상품과의 관계, `/categories/{id}/products` 조회, 존재하지 않는 카테고리 처리, 관련 HATEOAS 링크를 설계합니다. 그다음 메모리 저장소를 DB 저장소로 바꾸며 Service/Controller의 변경 범위를 비교할 수 있습니다. 이 과정에서 기능 추가 → 요청 검증 → 응답/예외 계약 → 문서 → 테스트가 함께 완성되는지 확인하세요.
