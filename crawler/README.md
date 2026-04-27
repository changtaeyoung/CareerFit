# CareerFit Crawler & AI Service

FastAPI 기반 채용공고 크롤링 및 AI 분석 서비스

## 개발 환경 설정

### 1. Python 버전 확인
```bash
python3.12 --version
```

### 2. 가상환경 생성
```bash
cd crawler
python3.12 -m venv venv
```

### 3. 가상환경 활성화
```bash
source venv/bin/activate  # macOS/Linux
```

### 4. 패키지 설치
```bash
pip install -r requirements.txt
```

### 5. 환경변수 설정
```bash
cp .env.example .env
# .env 파일 열어서 실제 값 입력
```

### 6. 서버 실행
```bash
python -m app.main
```

서버가 http://localhost:8000 에서 실행됨

## API 문서

서버 실행 후 브라우저에서 접속:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 엔드포인트

- `GET /` — 루트 (서버 상태 확인)
- `GET /health` — 헬스체크
