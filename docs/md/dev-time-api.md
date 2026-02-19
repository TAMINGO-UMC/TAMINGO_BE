# Dev Time API (데모용 시간 제어)

- 활성 조건: `dev` 프로필에서만 사용 (`@Profile("dev")`)
- 베이스 경로: `/api/dev/time`
- 목적: 데모 시 홈 화면 시간 흐름을 가속/이동하고, 필요 시 원복

## 1) 현재 시간 상태 조회

- `GET /api/dev/time`
- 응답 필드
  - `scale`: 현재 배속
  - `shiftMinutes`: 현재 누적 시간 이동(분)
  - `realNow`: 서버 실제 현재시간
  - `virtualNow`: 서비스가 사용하는 가상 현재시간

## 2) 배속 설정

- `POST /api/dev/time/scale?value={배속}`
- 예시: `POST /api/dev/time/scale?value=60` (1분을 60분처럼 흐르게)
- 허용 범위: `0.1 ~ 1000.0`

## 3) 시간 이동(분 단위)

- `POST /api/dev/time/shift?minutes={분}`
- 예시
  - `POST /api/dev/time/shift?minutes=20` (가상시간 +20분)
  - `POST /api/dev/time/shift?minutes=-20` (가상시간 -20분)

## 4) 이동만 초기화

- `POST /api/dev/time/shift/reset`
- 동작: `shiftMinutes`만 `0`으로 초기화, `scale`은 유지

## 5) 전체 초기화(배속 + 이동)

- `POST /api/dev/time/reset`
- 동작: `scale=1.0`, `shiftMinutes=0`으로 초기화

## curl 예시

```bash
# 배속 60배
curl -X POST "http://localhost:8080/api/dev/time/scale?value=60"

# +20분 이동
curl -X POST "http://localhost:8080/api/dev/time/shift?minutes=20"

# 상태 확인
curl -X GET "http://localhost:8080/api/dev/time"

# 이동만 초기화
curl -X POST "http://localhost:8080/api/dev/time/shift/reset"

# 전체 초기화(배속 + 이동)
curl -X POST "http://localhost:8080/api/dev/time/reset"
```
