# 004. NavigationMap이 Node와 Edge를 소유

- 상태: 채택
- 결정일: 2026-08-19

## 맥락

Node와 Edge를 독립적으로 관리하면 존재하지 않는 Node를 참조하는 Edge처럼 불완전한 graph가 생길 수 있다. Map의 이름과 일관성을 책임질 경계도 필요하다.

## 결정

로봇의 정적 이동 공간을 `NavigationMap`으로 명명하고 aggregate root로 둔다. `NavigationMap`은 Node와 Edge를 소유하며 생성 시 graph 전체의 불변조건을 검사한다.

Node와 Edge는 `NavigationMap` 밖에서 독립적인 생명주기를 갖지 않는다. DB 테이블이나 API 표현은 이 aggregate 경계와 별개로 설계할 수 있다.

| 객체 | 책임 |
| --- | --- |
| `NavigationMap` | Node·Edge 소유, graph 불변조건 검사, Node와 outgoing Edge 조회 |
| `Node` | 위치와 식별 정보 표현 |
| `Edge` | 방향성 있는 이동 관계와 거리 표현 |

## 경계 밖의 책임

| 책임 | 담당 영역 |
| --- | --- |
| 최단 경로 계산 | `PathPlanner` |
| Edge 차단·점유 | SimulationRun별 runtime state |
| Map 편집 중 임시 상태 | 향후 편집 모델 |
| 저장과 조회 기술 | persistence adapter |
| HTTP·JSON 표현 | API adapter |

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 일관성 | Map 생성 시 Node와 Edge 관계를 한곳에서 검증한다. |
| 책임 | 정적 이동 공간과 알고리즘·실행 상태의 경계가 분명하다. |
| 테스트 | 완성되고 유효한 Map을 PathPlanner의 고정 입력으로 사용할 수 있다. |
| 명명 | Kotlin의 `Map<K, V>`와 구분되고 도메인 의미가 드러난다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| Node나 Edge 일부만 직접 수정하기 어렵다. | immutable Map 원칙에 따라 편집 결과로 새 Map을 만든다. |
| Map 생성 시 전체 graph 검증이 필요하다. | 완성된 graph의 안정성을 위한 비용으로 수용한다. |
| `NavigationMap` 이름이 다소 길다. | 코드에서 역할이 명확해지는 장점을 우선한다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| `Map` | 짧고 익숙하다. | Kotlin collection 타입과 겹치고 의미가 모호하다. |
| `Graph` | 자료구조 관점에서 정확하다. | 물류 이동 공간이라는 도메인 의미가 약하다. |
| Node·Edge 독립 관리 | 개별 저장과 수정이 쉽다. | 검증 책임이 흩어지고 불완전한 graph가 생기기 쉽다. |

## 아직 결정하지 않은 사항

- `NavigationMap`, Node, Edge의 ID 타입
- Node 좌표의 타입과 단위
- 세부 graph 불변조건
- 생성 factory와 조회 API의 구체적인 형태
- Map identity와 revision 정책

## 다시 검토할 조건

- Node나 Edge가 Map과 독립적인 생명주기를 가져야 하는 실제 요구가 생긴 경우
- 매우 큰 Map의 전체 검증이나 로딩이 측정된 병목이 된 경우
- 이동 graph와 시각화 Map을 별도 모델로 분리해야 하는 요구가 생긴 경우
