# 014. NavigationMap snapshot을 정규화하여 원자적으로 저장

- 상태: 채택
- 결정일: 2026-11-08

## 맥락

Map을 저장하고 복원해도 Scenario가 참조할 snapshot의 내용과 경로 탐색 결과가 유지되어야 한다. ADR 012의 독립 snapshot 정책을 DB 경계에도 적용한다.

## 결정

- `NavigationMapRepository` port는 `save(map): Unit`, `findById(id): NavigationMap?`를 제공한다.
- 저장은 신규 insert만 허용한다. 같은 Map ID는 내용이 같아도 거부하며 기존 데이터를 유지한다. DB primary key가 동시 저장에도 이를 보장한다.
- `navigation_map`, `navigation_node`, `navigation_edge`를 UUID primary key로 저장한다. Node와 Edge ID도 snapshot 사이에서 재사용하지 않는다.
- Node와 Edge에 `map_id`를 두며 Edge의 `(map_id, from_node_id)`와 `(map_id, to_node_id)`는 같은 Map의 Node를 참조하는 복합 foreign key다.
- Map 이름, meter 좌표와 거리는 일반 컬럼으로 저장한다. PostGIS geometry는 아직 사용하지 않는다.
- 저장은 하나의 transaction으로 수행하며 중간 실패 시 전체를 rollback한다. JPA `persist`로 신규 저장하고 `merge`를 사용하지 않는다.
- Domain과 JPA entity를 분리한다. 조회는 `NavigationMap.create()`로 복원하여 불변조건을 재검증한다.
- Node/Edge collection의 입력 순서는 영속 계약에 포함하지 않는다. ID와 내용, 방향, 경로 탐색 결과를 보존한다.
- 스키마는 Liquibase로 관리하고 Hibernate는 validate만 한다. 통합 테스트는 Testcontainers PostGIS에서 실행한다.
- 중복 저장은 adapter의 데이터 무결성 예외로 노출한다. HTTP 오류 변환은 API use case를 설계할 때 정한다.

## 장점과 비용

| 선택 | 장점 | 비용 |
| --- | --- | --- |
| 정규화된 세 테이블 | FK로 snapshot 간 잘못된 참조 방지 | aggregate 복원에 여러 조회 필요 |
| 신규 저장만 허용 | 기존 실행 조건을 보호 | 동일 요청 재시도도 중복 오류 |
| 별도 JPA 모델 | 도메인 독립성 유지 | 변환 코드 필요 |
| 원자적 저장 | 부분 snapshot을 남기지 않음 | 대형 Map의 transaction 비용 |

## 대안

JSON 단일 컬럼은 graph 참조 제약을 DB에서 보장하기 어렵다. 같은 ID의 upsert는 snapshot 불변성을 훼손한다. 동일 내용 재저장 성공 처리는 내용 비교와 동시성 처리가 추가되어 채택하지 않는다. Domain에 JPA를 붙이는 방식은 core를 persistence 기술에 결합한다.

## 다시 검토할 조건

대형 Map 저장·조회가 병목이 되거나 재시도 멱등성, 검색, 삭제 또는 공간 질의가 실제 요구가 될 때 재검토한다.
