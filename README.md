<div align="center">

# 🎯 CareerFit

### 금융권 · 공기업 IT 직무 취준생을 위한 **1:1 맞춤 커리어 핏 분석 서비스**

> *"내 스펙이 이 기업에 얼마나 맞는지, 뭘 더 준비해야 하는지 — 한눈에."*

<br/>

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python_3.11-3776AB?style=flat-square&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=fastapi&logoColor=white)
![React](https://img.shields.io/badge/React_18-61DAFB?style=flat-square&logo=react&logoColor=black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)

</div>

---

## 📌 서비스 소개

채용 시즌마다 수십 개의 채용 공고를 뒤적이며 "내가 부족한 게 뭔지" 막막했던 경험, 한 번쯤 있으시죠?

**CareerFit**은 사용자의 스펙(자격증·어학·기술스택·경험)과 기업의 실제 채용 요건·기업 비전을 자동으로 비교 분석해, 정량 핏 점수와 우선순위 액션 플랜을 제공합니다.

| 기능 | 설명 |
|------|------|
| 📊 **정량 분석** | 자격증·어학·기술스택 조건 충족 여부 자동 비교 |
| 🔍 **정성 분석** | 경험 텍스트 ↔ 기업 JD 키워드 유사도 분석 |
| 🚀 **액션 플랜** | 합격 가능성을 올리는 우선순위 보완 항목 제시 |
| 🏢 **기업 정보** | DART·ALIO 기반 기업 비전·연봉·채용 공고 자동 수집 |
| 📈 **버전 관리** | 스펙 변경 이력 추적 → 분석 결과 완전 재현 가능 |

---

## 🏗 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│              (Vite · TypeScript · Zustand)               │
└──────────────────────┬──────────────────────────────────┘
                       │ REST API
┌──────────────────────▼──────────────────────────────────┐
│              Spring Boot API Server  :8080               │
│     auth │ user │ company │ analysis  (MyBatis · JWT)    │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP REST
┌──────────────────────▼──────────────────────────────────┐
│              Python FastAPI Crawler  :8000               │
│      crawlers │ ai │ scheduler │ dart │ alio             │
└──────────┬───────────────────────────┬──────────────────┘
           │                           │
    ┌──────▼──────┐            ┌───────▼──────────┐
    │ PostgreSQL  │            │  외부 API         │
    │   :5432     │            │  DART · ALIO      │
    └─────────────┘            │  OpenAI           │
                               └──────────────────┘
```

---

## 📁 프로젝트 구조

```
careerfit/
├── frontend/          # React (Vite + TypeScript)
├── backend/           # Spring Boot 3 (Java 17 / Maven)
├── crawler/           # FastAPI (Python 3.11)
├── sql/               # DDL · 초기 데이터
├── docker-compose.yml # 전체 서비스 통합 실행
└── .env.example       # 환경변수 템플릿 (실제 .env 는 커밋 제외)
```

---

## ⚙️ 기술 스택

### Backend — Spring Boot
| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3, Spring Security |
| ORM | MyBatis |
| Auth | JWT (Access / Refresh Token) |
| Database | PostgreSQL 15 |
| Build | Gradle |

### Crawler — Python
| 분류 | 기술 |
|------|------|
| Framework | FastAPI |
| Crawling | BeautifulSoup4, httpx, pdfplumber |
| AI 분석 | OpenAI API (gpt-4o-mini, text-embedding-3-small) |
| Scheduler | APScheduler |
| 외부 데이터 | DART Open API, ALIO Open API |

### Frontend — React
| 분류 | 기술 |
|------|------|
| Framework | React 18 + TypeScript |
| Build | Vite |
| 상태관리 | Zustand |
| HTTP | Axios |
| Routing | React Router v6 |

---

## 🚀 로컬 실행 방법

### 사전 조건

- [Docker & Docker Compose](https://docs.docker.com/get-docker/) 설치

### 1. 레포 클론

```bash
git clone https://github.com/{your-username}/careerfit.git
cd careerfit
```

### 2. 환경변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 아래 값 입력 (절대 커밋 금지)
```

```env
# Database
POSTGRES_DB=careerfit
POSTGRES_USER=careerfit
POSTGRES_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_32chars_minimum

# OpenAI
OPENAI_API_KEY=sk-...

# DART Open API  (https://opendart.fss.or.kr)
DART_API_KEY=your_dart_api_key

# ALIO Open API  (https://www.alio.go.kr)
ALIO_API_KEY=your_alio_api_key
```

### 3. Docker Compose 실행

```bash
docker-compose up --build
```

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost:5173 |
| Spring Boot API | http://localhost:8080 |
| FastAPI Crawler | http://localhost:8000 |
| PostgreSQL | localhost:5432 |

---

## 📡 API 엔드포인트

### Spring Boot `:8080`

```
POST   /api/auth/login              # 로그인
POST   /api/auth/register           # 회원가입

GET    /api/user/spec               # 내 스펙 조회
PUT    /api/user/spec               # 스펙 수정 (버전 자동 생성)

GET    /api/companies               # 기업 목록
GET    /api/companies/{id}          # 기업 상세

POST   /api/analysis                # 핏 분석 실행
GET    /api/analysis/history        # 분석 히스토리
```

### FastAPI `:8000`

```
POST   /api/crawl/trigger           # 수동 크롤링 트리거
GET    /api/crawl/status            # 파이프라인 상태 조회
GET    /health                      # 헬스체크
```

---

## 🗂 데이터베이스 주요 테이블

```
user                    사용자 기본 정보
user_spec_version       스펙 버전 (변경 이력)
user_certificate        보유 자격증
user_language_score     어학 점수

company                 기업 정보
job_posting             채용 공고 (URL 변경 감지 포함)
job_required_cert       공고 필수 자격증
job_skill               공고 기술스택

analysis_report         분석 리포트 (스냅샷)
analysis_gap            충족·부족 항목
analysis_recommendation 액션 플랜

keyword_dictionary      키워드 사전
data_pipeline_log       수집 로그
```

---

## 🔐 RBAC 권한 구조

| Role | 접근 가능 페이지 |
|------|----------------|
| `USER` | 대시보드, 내 분석, 내 스펙, 기업 목록, 채용 공고 |
| `ADMIN` | USER 전체 + 회원 관리, 파이프라인, 시스템 설정 |

---

## 👨‍💻 개발자

| | |
|--|--|
| **이름** | 장태영 |
| **포지션** | 백엔드 개발 |
| **기술** | Java · Spring Boot · Python |

---
