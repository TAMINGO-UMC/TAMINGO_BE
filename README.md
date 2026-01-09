# Tamingo Server

**Tamingo** 프로젝트의 Spring Boot 기반 백엔드 레포지토리입니다.

---

## 👤 백엔드 팀원 소개

<div align="center">

| Backend | Backend |                                    Backend                                    | Backend | Backend |
|:------:|:------:|:-----------------------------------------------------------------------------:|:------:|:------:|
| <img src="https://github.com/HeejuKo.png" width="150" /> | <img src="https://github.com/JiwonLee42.png" width="150" /> |        <img src="https://github.com/dearmytwilight.png" width="150" />        | <img src="https://github.com/leegy21.png" width="150" /> | <img src="https://avatars.githubusercontent.com/u/0?v=4" width="150" /> |
| [고희주](https://github.com/HeejuKo)<br/>로그인 / 회원가입<br/>이메일 인증<br/>온보딩 | [이지원](https://github.com/JiwonLee42)<br/>홈 | [김도윤](https://github.com/dearmytwilight)<br/>마이페이지<br/>장소 & 시간 설정<br/>알림 / 설정 | [이가영](https://github.com/leegy21)<br/>일정<br/>할 일 | 김현강<br/>마이페이지<br/>주간 리포트<br/>캘린더 연동 |

</div>

---

## 📋 Github 관리 규칙

### 작업 흐름
1. 작업 단위로 Issue 생성
2. develop 브랜치에서 작업 브랜치 생성
3. 생성한 브랜치에서 작업 진행
4. 작업 완료 후 develop 브랜치로 Pull Request 생성

### 작업 전 규칙
- 모든 작업 시작 전, 작업 브랜치에서 최신 develop 브랜치를 pull

### PR 전 규칙
- PR 생성 전 원격 develop 브랜치에 변경 사항이 있을 경우  
  작업 브랜치에 develop 브랜치 merge 후 PR 생성
---

### Commit Message Convention

형식<br>
[#Issue Number] Type: commit title

예시
```
[#5] Feat: 로그인 기능 추가
```

| Type                 | 의미                   | 사용 예시                                    |
| -------------------- | -------------------- | ---------------------------------------- |
| **Feat**             | 새로운 기능 추가            | `Feat: 일정 상세 조회 API 추가`                  |
| **Fix**              | 버그 수정                | `Fix: 출발 판단 계산 오류 수정`                    |
| **Docs**             | 문서 수정                | `Docs: API 명세서 업데이트`                     |
| **Style**            | 코드 스타일 변경 (로직 영향 없음) | `Style: import 정리`                       |
| **Refactor**         | 코드 리팩토링 (기능 변경 없음)   | `Refactor: 출발 판단 로직 분리`                  |
| **Test**             | 테스트 코드 추가/수정         | `Test: 일정 상세 조회 테스트 추가`                  |
| **Chore**            | 빌드/설정/기타 작업          | `Chore: gradle 설정 업데이트`                  |
| **Design**           | UI/UX 디자인 변경         | `Design: 홈 화면 레이아웃 수정`                   |
| **Comment**          | 주석 추가/수정             | `Comment: 출발 판단 로직 설명 추가`                |
| **Rename**           | 파일/폴더명 변경            | `Rename: ScheduleDto → ScheduleResponse` |
| **Remove**           | 파일/코드 삭제             | `Remove: 사용하지 않는 테스트 삭제`                 |
| **Init**             | 프로젝트 초기 세팅           | `Init: Spring Boot 프로젝트 초기화`             |
| **Merge**            | 브랜치 병합               | `Merge: feature/schedule-detail`         |
| **!BREAKING CHANGE** | 하위 호환 불가 변경          | `!BREAKING CHANGE: 일정 API 응답 구조 변경`      |
| **!HOTFIX**          | 운영 긴급 수정             | `!HOTFIX: 출발 알림 실패 긴급 수정`                |
