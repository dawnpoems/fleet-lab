# 015. Map HTTP API와 서버 발급 식별자

- 상태: 채택
- 결정일: 2026-11-08

## 맥락

브라우저에서 Map을 생성하고 저장된 graph의 최단 경로를 요청할 HTTP 경계가 필요하다. 클라이언트는 생성 전에 영구 UUID를 관리하지 않는다.

## 결정

| 요청 | 성공 응답 |
| --- | --- |
| POST /api/maps | 201, Location 헤더와 Map 본문 |
| GET /api/maps/{mapId} | 200, Map 본문 |
| POST /api/maps/{mapId}/paths | 200, Found 또는 Unreachable |

생성 입력은 name, nodes[{key,xMeters,yMeters}], edges[{from,to,distanceMeters}]다.
key는 요청 안에서만 유효한 비어 있지 않은 문자열이며 정확히 일치하는 값으로 비교한다. 중복 key와 알 수 없는 참조를 거부한다.
서버가 Map, Node, Edge UUID를 발급한다. 동일 요청을 반복하면 별도 snapshot이 만들어진다.

Map 응답은 id, name, nodes[{id,xMeters,yMeters}], edges[{id,from,to,distanceMeters}]다.
생성 응답의 Node/Edge 배열은 요청 순서를 유지하므로 클라이언트는 임시 key와 UUID를 대응시킬 수 있다. 조회 배열 순서는 보장하지 않는다.

경로 요청은 start와 destination UUID다. Found 응답은 status="Found" 및 path{start,destination,nodeIds,edgeIds,totalDistanceMeters}를 포함한다.
Unreachable 응답은 status="Unreachable"만 포함한다. 경로의 Edge ID와 Node ID 순서는 이동 순서다.

| 실패 | HTTP 응답 |
| --- | --- |
| 잘못된 JSON, UUID, 이름, graph, Node key 또는 탐색 Node | 400 ProblemDetail |
| 존재하지 않는 Map | 404 ProblemDetail |
| 연결되어 있지만 모든 경로 거리 합이 overflow | 422 ProblemDetail |

상세 오류에는 SQL과 내부 저장소 예외를 노출하지 않는다. 도달 불가는 정상 결과다.
application은 plain Kotlin으로 UUID 발급, graph 생성, 저장 및 탐색을 조율한다. Spring 설정에서 repository와 PathPlanner를 주입한다. Domain과 application은 HTTP/JPA/Spring에 의존하지 않는다.

## 장점과 비용

서버 UUID 발급으로 snapshot 정책을 중앙에서 지킬 수 있으나 임시 key 변환이 필요하다.
명시적인 응답 DTO는 Domain을 직렬화 형식으로부터 분리하지만 변환 코드가 필요하다.
생성 재시도는 새 Map을 만들므로 멱등성을 보장하지 않는다.

## 대안

클라이언트 UUID 발급은 변환이 적으나 클라이언트의 식별자 관리 책임이 커진다.
Unreachable을 404로 표현하면 Map 부재와 경로 부재를 혼동한다.
알고리즘 이름 요청 인자는 아직 선택 UI나 복수 구현이 없어 추가하지 않는다.

## 다시 검토할 조건

목록 조회, 멱등성, 인증, algorithm 선택 또는 runtime 통행 상태가 필요해질 때 재검토한다.
