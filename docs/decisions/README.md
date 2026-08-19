# FleetLab 설계 결정 기록

FleetLab의 도메인 모델, 공개 계약, 영속성, 아키텍처, 의존성 또는 개발 방식에 지속적인 영향을 주는 결정을 기록한다.

아직 합의하지 않은 아이디어나 단순한 구현 세부사항은 기록하지 않는다. 기존 결정을 바꾸면 과거 문서를 삭제하지 않고, 새 결정 문서에서 대체된 문서를 명시한다.

## 작성 형식

파일명은 `NNN-kebab-case-title.md` 형식을 사용하고 다음 내용을 포함한다.

본문은 핵심만 간결하게 작성하고, 장단점이나 대안 비교에는 표를 우선 사용한다.

- 상태와 결정일
- 결정이 필요했던 맥락
- 최종 결정
- 선택의 이유와 장점
- 감수하는 단점과 후속 영향
- 검토한 대안
- 결정을 다시 검토할 조건

## 결정 목록

- [001. Map의 Edge를 방향성 있는 이동 관계로 모델링](001-directed-edge.md) — 채택
- [002. 생성된 Map을 immutable하게 유지](002-immutable-map.md) — 채택
- [003. Edge 거리를 명시적인 meter 값으로 표현](003-explicit-edge-distance.md) — 채택
- [004. NavigationMap이 Node와 Edge를 소유](004-navigation-map-aggregate.md) — 채택
- [005. Node와 Edge에 UUID 기반 전용 ID를 사용](005-node-edge-identifiers.md) — 채택
- [006. NavigationMap 생성 시 graph 불변조건을 검증](006-navigation-map-invariants.md) — 채택
- [007. Node 좌표를 meter 단위의 2D 값으로 표현](007-node-coordinate.md) — 채택
- [008. NavigationMap을 검증된 factory method로 생성](008-navigation-map-creation.md) — 채택
- [009. Path를 시작 Node와 Edge 목록으로 표현](009-path-representation.md) — 채택
- [010. PathPlanner가 명시적인 탐색 결과를 반환](010-path-planner-contract.md) — 채택
- [011. Dijkstra의 동일 비용 경로를 Edge ID 순서로 결정](011-dijkstra-deterministic-tie-breaking.md) — 채택
