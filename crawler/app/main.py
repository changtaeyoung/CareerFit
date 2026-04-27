# app/main.py
# FastAPI 애플리케이션 진입점
# Spring Boot의 @SpringBootApplication 클래스와 동일한 역할

from fastapi import FastAPI
from app.config import settings
from app.api.health import router as health_router
from app.api.crawl import router as crawl_router
from app.api.analysis import router as analysis_router
import logging


# 로거 설정
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


# FastAPI 앱 생성
app = FastAPI(
    title="CareerFit Crawler & AI Service",
    description="채용공고 크롤링 및 AI 분석 API",
    version="1.0.0"
)

# 라우터 등록
app.include_router(health_router)
app.include_router(crawl_router)
app.include_router(analysis_router)


# 루트 엔드포인트
@app.get("/")
async def root():
    logger.info("루트 경로 접근됨")
    return {
        "service": "CareerFit AI Service",
        "status": "running",
        "version": "1.0.0"
    }


# 앱 시작 이벤트
@app.on_event("startup")
async def startup_event():
    logger.info("=" * 50)
    logger.info("FastAPI 서버 시작 중...")
    logger.info(f"HOST: {settings.HOST}")
    logger.info(f"PORT: {settings.PORT}")
    logger.info(f"Spring Boot URL: {settings.SPRING_BOOT_URL}")
    logger.info("=" * 50)


# 앱 종료 이벤트
@app.on_event("shutdown")
async def shutdown_event():
    logger.info("FastAPI 서버 종료됨")


# 직접 실행 시 서버 시작
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True
    )
