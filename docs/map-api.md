# Map API 사용 예제

README의 Docker/PostGIS 및 backend 시작 절차를 실행한 뒤 사용한다.
기본 주소는 http://localhost:8080 이다.

## 1. Map 생성

```bash
curl -i http://localhost:8080/api/maps \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Warehouse",
    "nodes": [
      {"key": "A", "xMeters": 0, "yMeters": 0},
      {"key": "B", "xMeters": 1, "yMeters": 0},
      {"key": "C", "xMeters": 2, "yMeters": 0}
    ],
    "edges": [
      {"from": "A", "to": "B", "distanceMeters": 1.25},
      {"from": "B", "to": "C", "distanceMeters": 2.75}
    ]
  }'
```

기대 결과: 201 Created와 Location: /api/maps/{id}.
본문은 id, name, nodes, edges를 포함한다. 서버가 모든 UUID를 생성한다.
생성 응답의 nodes[0], nodes[1], nodes[2]는 각각 요청의 A, B, C에 대응한다.
Edge의 from/to는 실제 Node UUID다. key는 저장하지 않는다.
동일 요청을 다시 보내면 별도 snapshot이 만들어진다.

## 2. 저장된 Map 조회

아래 값은 생성 응답의 실제 UUID로 바꾼다.

```bash
map_id='생성 응답의 id'
start_id='생성 응답 nodes[0].id'
destination_id='생성 응답 nodes[2].id'

curl -i "http://localhost:8080/api/maps/$map_id"
```

기대 결과: 200 OK와 저장된 graph. 조회 배열의 순서는 보장하지 않으므로 UUID로 Node와 Edge를 연결한다.

## 3. 경로 탐색

```bash
curl -i "http://localhost:8080/api/maps/$map_id/paths" \
  -H 'Content-Type: application/json' \
  -d "{\"start\":\"$start_id\",\"destination\":\"$destination_id\"}"
```

기대 결과는 200 OK, status=Found 및 path.totalDistanceMeters=4.0이다.
path에는 start, destination, 이동 순서의 nodeIds와 edgeIds가 포함된다.
출발과 목적지를 바꾸면 반대 방향 Edge가 없으므로 200 OK와 다음 응답을 받는다.

```json
{"status":"Unreachable"}
```

같은 유효 Node를 출발·목적지로 지정하면 Found, 빈 edgeIds, 거리 0을 반환한다.

## 오류

| 상황 | 응답 |
| --- | --- |
| 잘못된 JSON/UUID, 이름, 중복 key, 존재하지 않는 key 참조, 잘못된 graph 또는 탐색 Node | 400 |
| 존재하지 않는 Map | 404 |
| 연결되었으나 모든 경로의 거리 합이 overflow | 422 |

오류 형식은 application/problem+json이며 status, title, detail을 포함한다.
프런트엔드는 detail 문자열 대신 HTTP status와 정상 응답의 status 필드로 분기한다.
목록 조회, 편집, 삭제 및 알고리즘 선택 API는 아직 제공하지 않는다.

## 자동 검증

```bash
cd backend
./gradlew test --tests '*NavigationMapApiTest' --tests '*NavigationPathApiTest'
```

Docker가 실행되어 있어야 한다. Testcontainers PostGIS를 사용한다.
경로 테스트는 임시 포트에서 실제 HTTP 서버를 띄워 생성 → 조회 → 경로 탐색과 오류 응답을 검증하고 종료한다.
