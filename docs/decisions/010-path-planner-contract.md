# 010. PathPlanner가 명시적인 탐색 결과를 반환

- 상태: 채택
- 결정일: 2026-08-19

## 맥락

SimulationEngine이 Dijkstra 같은 구체 알고리즘을 직접 선택하지 않으려면 교체 가능한 경로 탐색 계약이 필요하다. 단절된 graph에서 도달 불가능은 오류가 아니라 정상적으로 발생할 수 있는 결과다.

## 결정

`PathPlanner`를 stateless domain policy interface로 정의한다. 정적 `NavigationMap`, 출발 `NodeId`, 목적지 `NodeId`를 입력받고 명시적인 `PathPlanningResult`를 반환한다.

```kotlin
interface PathPlanner {
    fun findPath(
        map: NavigationMap,
        start: NodeId,
        destination: NodeId,
    ): PathPlanningResult
}

sealed interface PathPlanningResult {
    data class Found(val path: Path) : PathPlanningResult
    data object Unreachable : PathPlanningResult
}
```

| 상황 | 처리 |
| --- | --- |
| 유효한 경로 발견 | `Found(Path)` |
| 출발지와 목적지가 같음 | 거리 0의 `Found(Path)` |
| 목적지에 도달할 수 없음 | `Unreachable` |
| 출발 Node가 Map에 없음 | `IllegalArgumentException` |
| 목적지 Node가 Map에 없음 | `IllegalArgumentException` |

구체 알고리즘 구현체는 `PathPlanner`를 구현하며 외부에서 선택해 주입한다. 초기 계약은 정적 Map 탐색만 다루고 SimulationRun의 Edge 차단·점유 상태를 포함하지 않는다.

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 교체 가능성 | SimulationEngine이 구체 path finding 알고리즘에 의존하지 않는다. |
| 결과 의미 | 경로 발견과 도달 불가능을 타입으로 구분한다. |
| 안전성 | 호출부가 모든 정상 결과를 처리하도록 컴파일러가 돕는다. |
| 재현성 | Planner가 mutable state를 가지지 않아 같은 입력을 독립적으로 처리한다. |
| 단순성 | 실제 모델인 `NavigationMap`을 사용하고 일반 Graph 추상화를 추가하지 않는다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| 결과 타입이 추가된다. | 정상적인 두 결과를 명확히 구분하는 domain 타입으로 사용한다. |
| Run 중 통행 상태를 아직 반영할 수 없다. | Runtime state를 설계할 때 실제 요구에 맞는 탐색 입력을 추가한다. |
| 없는 Node는 결과 타입이 아닌 예외로 처리된다. | 도달 불가능과 잘못된 호출을 의도적으로 구분한다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| Nullable `Path` | 별도 결과 타입이 없다. | `null`의 의미가 타입에 드러나지 않고 처리를 빠뜨리기 쉽다. |
| 경로가 없으면 예외 | 반환 타입이 단순하다. | 도달 불가능은 정상적인 탐색 결과다. |
| 알고리즘 이름을 인자로 전달 | 하나의 함수에서 알고리즘을 선택할 수 있다. | 구현체 선택 분기가 Planner 내부에 쌓인다. |
| 일반 Graph interface 입력 | 여러 graph 구현을 받을 수 있다. | 현재 존재하지 않는 변형을 위한 추상화다. |

## 다시 검토할 조건

- SimulationRun의 차단·점유·비용 상태를 path finding에 반영할 때
- 탐색 통계나 진단 정보를 결과로 제공해야 할 때
- 비동기 또는 시간 제한 탐색이 실제로 필요해질 때
