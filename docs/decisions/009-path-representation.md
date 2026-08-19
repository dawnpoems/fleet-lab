# 009. Path를 시작 Node와 Edge 목록으로 표현

- 상태: 채택
- 결정일: 2026-08-19

## 맥락

Path는 PathPlanner가 선택한 정확한 이동 통로와 방향, 총거리를 표현해야 한다. Parallel edge를 허용하므로 Node 목록만으로는 사용한 Edge를 구분할 수 없다.

## 결정

`Path`를 시작 `NodeId`와 순서가 있는 immutable `List<Edge>`로 구성한다. Path는 내용 자체가 중요한 value object이며 별도 `PathId`를 갖지 않는다.

| 정보 | 표현 |
| --- | --- |
| 시작점 | `start: NodeId` |
| 이동 통로 | `edges: List<Edge>` |
| 목적지 | 마지막 Edge의 `to`, 빈 목록이면 `start` |
| Node 목록 | `start`와 각 Edge의 `to`로 계산 |
| 총거리 | Edge의 `distanceMeters` 합으로 계산 |

Edge가 없는 Path는 출발지와 목적지가 같은 거리 0의 경로를 나타낸다. 입력 Edge 목록은 방어적으로 복사하고 다음 불변조건을 검사한다.

- 첫 Edge의 `from`이 `start`와 같다.
- 앞 Edge의 `to`와 다음 Edge의 `from`이 같다.
- 계산된 총거리가 유한하다.

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 정확성 | Parallel edge 중 실제로 사용한 Edge를 보존한다. |
| 일관성 | Node 목록과 총거리를 중복 저장하지 않는다. |
| 자기 완결성 | Path만으로 방향과 이동 거리를 확인할 수 있다. |
| 빈 경로 | 시작 Node를 별도로 보관해 거리 0의 경로를 표현한다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| Path가 Edge 객체를 직접 참조한다. | Edge와 NavigationMap이 immutable이므로 안전하게 공유한다. |
| Node 목록 조회 시 계산이 필요하다. | start와 Edge 목적지를 순서대로 결합하는 단순 계산으로 제공한다. |
| 연속성 검증이 필요하다. | Path 생성 시 한 번 검사해 이후 사용 코드를 단순화한다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| Node 목록만 저장 | 사람이 읽기 쉽다. | Parallel edge와 사용한 통로를 구분할 수 없다. |
| Edge ID 목록만 저장 | Path 데이터가 가볍다. | 방향과 거리를 알기 위해 매번 Map 조회가 필요하다. |
| Node와 Edge 목록 모두 저장 | 조회가 편하다. | 두 목록이 서로 불일치할 수 있다. |
| 총거리 직접 입력 | 계산이 필요 없다. | Edge 거리 합과 다른 값이 저장될 수 있다. |

## 다시 검토할 조건

- 매우 긴 Path에서 Edge 객체 보관이나 파생값 계산이 측정된 병목이 된 경우
- Map revision 사이에서 Path를 장기간 보존해야 하는 경우
- Path 자체를 영속화하고 독립적으로 식별해야 하는 요구가 생긴 경우
