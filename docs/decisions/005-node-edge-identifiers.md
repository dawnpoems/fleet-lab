# 005. Node와 Edge에 UUID 기반 전용 ID를 사용

- 상태: 채택
- 결정일: 2026-08-19

## 맥락

Node와 Edge는 이름이나 좌표가 아닌 변경되지 않는 값으로 식별해야 한다. 특히 동일한 두 Node 사이에 여러 Edge가 있거나 Run에서 특정 Edge만 차단할 때 각 Edge를 정확히 구분해야 한다.

## 결정

Node와 Edge는 각각 UUID 기반의 `NodeId`, `EdgeId` value class로 식별한다. ID는 생성 후 변경하지 않으며 사용자에게 보여줄 label과 분리한다.

ID는 DB 저장 전에 application 또는 입력 adapter에서 생성하여 domain에 전달한다. Edge는 출발지와 목적지를 `NodeId`로 참조하고, `NavigationMap`은 각 ID의 중복과 참조 유효성을 검사한다.

| 항목 | 결정 |
| --- | --- |
| Node 식별자 | UUID 기반 `NodeId` |
| Edge 식별자 | UUID 기반 `EdgeId` |
| 생성 시점 | DB 저장 전 |
| 생성 주체 | Application 또는 입력 adapter |
| 사용자 표시 | ID와 label을 분리 |
| Edge의 Node 참조 | `from: NodeId`, `to: NodeId` |

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 타입 안전성 | Node ID와 Edge ID를 바꿔 전달하면 컴파일 단계에서 발견할 수 있다. |
| Domain 독립성 | DB 없이도 완성된 domain 객체를 생성하고 테스트할 수 있다. |
| 충돌 방지 | Map을 import하거나 합칠 때 ID 충돌 가능성이 매우 낮다. |
| 안정적인 참조 | label이나 좌표가 달라져도 같은 대상을 식별할 수 있다. |
| 영속성 | PostgreSQL의 `uuid` 타입으로 자연스럽게 저장할 수 있다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| UUID는 사람이 읽기 어렵다. | 사용자 화면과 로그에는 별도 label이나 축약 표현을 사용한다. |
| 테스트 데이터가 장황할 수 있다. | 고정 UUID를 제공하는 test fixture helper를 사용한다. |
| 무작위 UUID 순서는 업무 의미가 없다. | ID 순서는 의미로 해석하지 않고, 필요할 때 안정적인 비교 기준으로만 사용한다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| DB 자동 증가 `Long` | 짧고 저장이 간단하다. | 저장 전 ID가 없고 domain 생성이 DB lifecycle에 종속된다. |
| 사람이 정한 문자열 | 읽고 테스트하기 쉽다. | 이름 규칙 관리와 Map 간 충돌 문제가 생긴다. |
| 모든 ID에 원시 `UUID` 사용 | 전용 타입이 필요 없다. | Node ID와 Edge ID를 바꿔 전달하는 실수를 컴파일러가 막지 못한다. |

## 다시 검토할 조건

- 외부 시스템이 지정한 식별자를 보존해야 하는 연동 요구가 생긴 경우
- UUID 저장이나 인덱스가 측정된 성능 병목이 된 경우
- Map revision 사이에서 Node와 Edge identity를 유지하는 규칙이 필요한 경우
