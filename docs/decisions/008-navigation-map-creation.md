# 008. NavigationMap을 검증된 factory method로 생성

- 상태: 채택
- 결정일: 2026-08-19

## 맥락

`NavigationMap`은 Node와 Edge 전체의 불변조건을 검사하고 생성 이후 immutable해야 한다. 외부 collection 변경으로 내부 상태가 바뀌거나 검증을 우회한 객체가 만들어져서는 안 된다.

## 결정

`NavigationMap` 생성자는 private으로 두고 companion object의 `create(nodes, edges)`를 유일한 생성 진입점으로 사용한다. 완성된 Node와 Edge collection을 한 번에 받아 다음 작업을 수행한다.

1. 입력 collection을 방어적으로 복사한다.
2. Graph 전체의 불변조건을 검증한다.
3. Node ID와 outgoing Edge 조회 index를 만든다.
4. 검증된 immutable `NavigationMap`을 반환한다.

별도 Factory class, Builder 또는 MapDraft는 현재 도입하지 않는다.

불변조건 위반은 Kotlin `require`를 사용해 fail-fast `IllegalArgumentException`으로 처리한다. 첫 번째 오류에서 생성을 중단하며 같은 입력이 같은 오류부터 실패하도록 검증 순서를 고정한다.

```text
1. 빈 Map
2. 중복 Node ID
3. 중복 Edge ID
4. 존재하지 않는 Node 참조
5. Isolated Node
```

예외 메시지는 원인을 이해할 수 있게 작성하지만 외부 API가 파싱할 public contract로 사용하지 않는다. 유효한 Map에서 목적지에 도달할 수 없는 상태는 생성 오류가 아니라 PathPlanner의 정상적인 탐색 결과로 다룬다.

## 검증 책임

| 검증 대상 | 책임 객체 |
| --- | --- |
| 좌표가 유한한가 | `Coordinate` |
| Edge 거리와 self-loop가 유효한가 | `Edge` |
| ID 중복, Node 참조, isolated Node, 빈 Map | `NavigationMap` |

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 유효성 | 검증을 우회한 `NavigationMap` 생성을 막는다. |
| Immutable | 외부 mutable collection의 변경이 Map에 영향을 주지 않는다. |
| 조회 | 반복 탐색에 필요한 index를 생성 시 한 번 준비한다. |
| 단순성 | 별도 Factory class나 Builder 없이 생성 책임을 한곳에 둔다. |
| 실패 계약 | 별도 결과 타입 없이 불변조건 위반을 즉시 알린다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| Node와 Edge를 하나씩 추가할 수 없다. | 실제 편집 요구가 생기면 별도 draft 모델을 설계한다. |
| 단순 constructor보다 생성 코드가 늘어난다. | Aggregate 전체 검증과 내부 index 생성을 명시하는 비용으로 수용한다. |
| 여러 오류 중 첫 번째 오류만 확인할 수 있다. | 모든 오류를 보여줘야 할 때 별도 validation report를 추가한다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| Public constructor | 가장 직접적이고 코드가 적다. | 검증과 내부 표현 준비가 단순 값 생성처럼 보이고 생성 진입점 통제가 약하다. |
| Mutable Builder | Map을 단계적으로 작성하기 쉽다. | 아직 편집 lifecycle 요구가 없고 mutable 상태가 추가된다. |
| 별도 Factory class | 생성 책임을 독립 객체로 분리한다. | 외부 의존성이나 여러 생성 전략이 없어 현재는 불필요하다. |
| `null` 반환 | 실패 표현이 단순하다. | 실패 원인을 알 수 없고 호출부마다 null 처리가 필요하다. |
| 명시적인 validation 결과 | 여러 오류를 타입으로 전달할 수 있다. | 현재는 결과와 위반 타입 및 호출부 분기가 추가되는 비용이 더 크다. |

## 다시 검토할 조건

- 실제 Map 편집 lifecycle과 임시 저장 요구가 생긴 경우
- 편집 화면에서 여러 검증 오류를 한 번에 제공해야 하는 경우
- 외부 API에 안정적인 domain 오류 코드가 필요한 경우
- 여러 형식의 Map import가 서로 다른 생성 절차를 필요로 하는 경우
- 조회 index 생성 비용이나 전체 Map 로딩이 측정된 병목이 된 경우
