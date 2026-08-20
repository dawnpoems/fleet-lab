# 012. NavigationMap ID가 하나의 immutable snapshot을 식별

- 상태: 채택
- 결정일: 2026-08-20

## 맥락

Immutable `NavigationMap`을 수정하거나 복사할 때 같은 Map의 revision으로 관리할지 독립된 Map으로 관리할지 정해야 한다. Scenario는 실행에 사용한 정확한 Map을 계속 재현할 수 있어야 한다.

## 결정

UUID 기반 `NavigationMapId`가 하나의 완성된 immutable Map snapshot을 식별한다. ID는 DB 저장 전에 application 또는 입력 adapter에서 생성하고 생성 후 변경하지 않는다.

Map을 수정하거나 복사하면 새로운 `NavigationMapId`, `NodeId`, `EdgeId`를 발급해 독립된 `NavigationMap`을 만든다. 초기 모델에는 revision, 최신 버전, parent 또는 lineage를 두지 않는다.

Scenario는 논리적인 Map 이름이나 최신 Map이 아니라 실행에 사용할 정확한 `NavigationMapId`를 참조한다.

| 항목 | 결정 |
| --- | --- |
| Map ID | UUID 기반 `NavigationMapId` |
| ID 의미 | 하나의 immutable snapshot |
| 수정·복사 | 새 Map과 Node·Edge ID 발급 |
| Revision·lineage | 초기에는 사용하지 않음 |
| Scenario 참조 | 정확한 `NavigationMapId` |

## 선택의 장점

| 관점 | 장점 |
| --- | --- |
| 재현성 | 기존 Scenario가 참조한 Map 내용이 바뀌지 않는다. |
| 단순성 | Revision 번호, 최신 버전과 동시 편집 규칙이 필요 없다. |
| 독립성 | 각 Map과 그 Node·Edge의 lifecycle이 완전히 분리된다. |
| 영속성 | Scenario가 Map ID 하나만 foreign key로 참조할 수 있다. |

## 감수하는 단점

| 단점 | 대응 |
| --- | --- |
| 원본과 수정본의 관계를 알 수 없다. | 실제 버전 이력 요구가 생기면 snapshot을 묶는 별도 series를 추가한다. |
| Map 사이에서 대응하는 Node·Edge를 알 수 없다. | Cross-snapshot 비교가 필요할 때 lineage를 별도로 설계한다. |
| 버전 목록과 rollback을 제공할 수 없다. | 초기에는 독립 Map 선택과 복사만 지원한다. |

## 검토한 대안

| 대안 | 장점 | 선택하지 않은 이유 |
| --- | --- | --- |
| `MapId + revision` | 버전 이력과 rollback을 자연스럽게 지원한다. | 아직 없는 편집 lifecycle, revision 발급과 동시성 규칙이 필요하다. |
| 같은 ID의 Map 직접 수정 | 사용자에게 같은 Map으로 보인다. | Immutable 원칙과 Scenario 재현성을 깨뜨린다. |

## 다시 검토할 조건

- Map 버전 목록, rollback 또는 revision 비교가 제품 요구가 된 경우
- 여러 사용자의 동시 Map 편집과 발행 workflow가 필요한 경우
- Snapshot 사이의 Node·Edge lineage를 추적해야 하는 경우
