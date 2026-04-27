"""
crawl.py — 채용공고 크롤링 API 엔드포인트

Spring Boot의 CompanyController 역할.
POST /api/crawl/posting  →  URL 받아서 크롤링 결과 반환
"""

import logging
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.fetcher import fetch_html
from app.services.content_detector import detect_content
from app.services.crawl_service import crawl_job_posting
from app.config import settings

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/crawl", tags=["crawl"])


class CrawlRequest(BaseModel):
    url: str


class CrawlResponse(BaseModel):
    success: bool
    data: dict | None = None
    error: str | None = None
    content_type: str | None = None


class DebugResponse(BaseModel):
    """디버깅용 — HTML 구조 확인"""
    html_length: int
    content_type: str
    text_preview: str          # 추출된 텍스트 앞 500자
    image_urls: list[str]      # 감지된 이미지 URL 전체
    pdf_urls: list[str]        # 감지된 PDF URL 전체


@router.post("/posting", response_model=CrawlResponse)
async def crawl_posting(request: CrawlRequest):
    """채용공고 URL 크롤링 → 구조화된 공고 정보 반환"""
    logger.info(f"[CrawlAPI] 크롤링 요청 — URL: {request.url}")

    if not settings.OPENAI_API_KEY:
        raise HTTPException(status_code=503, detail="OPENAI_API_KEY가 설정되지 않았습니다")

    result = await crawl_job_posting(url=request.url, openai_api_key=settings.OPENAI_API_KEY)

    if not result.success:
        return CrawlResponse(success=False, error=result.error)

    return CrawlResponse(
        success=True,
        data=result.data,
        content_type=result.data.get("content_type") if result.data else None
    )


@router.post("/debug", response_model=DebugResponse)
async def debug_page(request: CrawlRequest):
    """
    디버깅용 — Playwright로 가져온 HTML 구조 확인.
    이미지/PDF가 제대로 감지되는지, 텍스트가 뭐가 있는지 확인하는 용도.
    실제 크롤링 전에 먼저 이걸로 페이지 구조 파악.
    """
    logger.info(f"[CrawlAPI] 디버그 요청 — URL: {request.url}")

    html = await fetch_html(request.url)
    if not html:
        raise HTTPException(status_code=502, detail="HTML 수신 실패")

    detected = detect_content(html, request.url)

    return DebugResponse(
        html_length=len(html),
        content_type=detected.content_type,
        text_preview=detected.text_content[:500] if detected.text_content else "",
        image_urls=detected.image_urls,
        pdf_urls=detected.pdf_urls,
    )
