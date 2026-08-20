# 013. NavigationMap만 필수 사용자 표시 이름을 가짐

- 상태: 채택
- 결정일: 2026-08-20

## 맥락

UUID는 시스템 식별에는 적합하지만 사용자가 Map을 구분하기 어렵다. 반면 모든 routing Node와 Edge에 이름을 강제하면 의미 없는 label과 관리 부담이 생긴다.

## 결정

사용자가 관리하고 선택하는 `NavigationMap`만 필수 `NavigationMapName` value object를 가진다. Node와 Edge에는 초기 모델에서 generic label을 추가하지 않는다.

`NavigationMapName`은 입력의 앞뒤 공백을 제거한 뒤 다음 규칙을 만족해야 한다.

| 항목 | 결정 |
| --- | --- |
| 길이 | 1~100자 |
| 빈 문자열·공백만 있는 값 | 거부 |
| 앞뒤 공백 | 제거 |
| 다른 Map과 같은 이름 | 허용 |

이름은 identity가 아니며 Map 이름을 변경하려면 새 `NavigationMapId`를 가진 snapshot을 생성한다. 향후 graph와 무관한 이름만 독립적으로 수정해야 하는 요구가 생기면 catalog metadata 분리를 검토한다.

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 사용성 | UUID 대신 사람이 읽을 수 있는 이름으로 Map을 구분한다. |
| 단순성 | 이름이 필요 없는 Node와 Edge에 optional 필드를 추가하지 않는다. |
| Domain 의미 | 향후 Station 같은 업무 위치가 자신의 이름과 `NodeId`를 소유할 수 있다. |
| Identity 분리 | 이름 중복을 허용해 이름이 두 번째 ID가 되는 것을 막는다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| Node와 Edge를 UUID 없이 설명하기 어렵다. | 초기 UI와 로그에서는 좌표와 축약 ID를 사용한다. |
| 이름만 바꿔도 새 snapshot이 필요하다. | 독립적인 이름 수정 요구가 생길 때 catalog metadata로 분리한다. |
| 중복 Map 이름이 사용자에게 혼란을 줄 수 있다. | UI에서 ID 일부 등 추가 정보를 함께 표시할 수 있다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| Map·Node·Edge 이름 필수 | 모든 요소를 사람이 읽기 쉽다. | 자동 생성 graph에 의미 없는 이름을 강제하고 업무 의미가 섞인다. |
| 모든 이름 선택 사항 | Import와 자동 생성이 자유롭다. | 사용자 관리 단위인 Map조차 이름 없이 존재할 수 있다. |
| Map 이름 unique | 목록에서 Map을 쉽게 구분한다. | 이름이 사실상 identity가 되고 복사와 명명 규칙이 경직된다. |

## 다시 검토할 조건

- Node 또는 Edge 이름을 직접 사용하는 UI 요구가 생긴 경우
- Map 이름을 snapshot 생성 없이 수정해야 하는 경우
- Station, charger 또는 작업 위치의 domain 모델을 설계하는 경우
