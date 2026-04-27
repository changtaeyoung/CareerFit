"""
crawl_service.py — 채용공고 크롤링 메인 파이프라인

전체 흐름:
  URL 입력
    ↓
  HTML 수신 (httpx or Playwright)
    ↓
  콘텐츠 유형 감지 (TEXT / IMAGE / PDF / MIXED)
    ↓
  ┌── TEXT  → BeautifulSoup 파싱
  ├── IMAGE → OpenAI Vision API
  ├── PDF   → pdfplumber → (스캔이면) OpenAI Vision
  └── MIXED → 해당하는 방법 조합
    ↓
  GPT-4o-mini로 구조화 (제목, 자격증, 기술스택, 마감일 등)
    ↓
  결과 반환 (DB 저장은 API 레이어에서 담당)
"""

import logging
from typing import Optional
from app.services.fetcher import fetch_html
from app.services.content_detector import detect_content, DetectedContent
from app.services.image_parser import parse_multiple_images
from app.services.pdf_parser import parse_pdf_from_url
from app.services.posting_parser import parse_posting_text

logger = logging.getLogger(__name__)


class CrawlResult:
    """크롤링 결과 데이터 클래스"""
    def __init__(self, success: bool, data: Optional[dict] = None, error: str = ""):
        self.success = success
        self.data = data      # parse_posting_text 반환값
        self.error = error


async def crawl_job_posting(url: str, openai_api_key: str) -> CrawlResult:
    """
    채용공고 URL을 받아 구조화된 공고 정보를 반환하는 메인 함수.

    이 함수 하나로 텍스트/이미지/PDF/복합 형태 모두 처리.
    """
    logger.info(f"[CrawlService] 크롤링 시작 — URL: {url}")

    # ── Step 1: HTML 가져오기 ────────────────────────────────────────────────
    html = await fetch_html(url)
    if not html:
        return CrawlResult(success=False, error="HTML을 가져올 수 없습니다")

    # ── Step 2: 콘텐츠 유형 감지 ────────────────────────────────────────────
    detected: DetectedContent = detect_content(html, url)
    logger.info(f"[CrawlService] 감지된 콘텐츠 타입: {detected.content_type}")

    # ── Step 3: 콘텐츠 유형별 텍스트 추출 ──────────────────────────────────
    raw_text_parts = []

    # 텍스트가 있으면 우선 수집
    if detected.text_content and len(detected.text_content.strip()) > 100:
        raw_text_parts.append(detected.text_content)
        logger.info(f"[CrawlService] HTML 텍스트 수집 — {len(detected.text_content)}자")

    # 이미지 파싱 (IMAGE or MIXED)
    if detected.image_urls:
        logger.info(f"[CrawlService] 이미지 {len(detected.image_urls)}개 파싱 시작")
        image_text = await parse_multiple_images(detected.image_urls, openai_api_key)
        if image_text:
            raw_text_parts.append(image_text)

    # PDF 파싱 (PDF or MIXED)
    if detected.pdf_urls:
        logger.info(f"[CrawlService] PDF {len(detected.pdf_urls)}개 파싱 시작")
        for pdf_url in detected.pdf_urls:
            pdf_text = await parse_pdf_from_url(pdf_url, openai_api_key)
            if pdf_text:
                raw_text_parts.append(pdf_text)

    # ── Step 4: 아무것도 없으면 실패 ────────────────────────────────────────
    if not raw_text_parts:
        logger.error("[CrawlService] 텍스트 추출 실패 — 콘텐츠 없음")
        return CrawlResult(
            success=False,
            error=f"콘텐츠 추출 실패 (타입: {detected.content_type})"
        )

    # ── Step 5: 추출된 텍스트 합치기 ────────────────────────────────────────
    combined_text = "\n\n".join(raw_text_parts)
    logger.info(f"[CrawlService] 전체 추출 텍스트 — {len(combined_text)}자")

    # ── Step 6: GPT로 구조화 ────────────────────────────────────────────────
    parsed = await parse_posting_text(combined_text, openai_api_key)
    if not parsed:
        return CrawlResult(
            success=False,
            error="공고 정보 구조화 실패"
        )

    # 원본 URL 추가
    parsed["url"] = url
    parsed["content_type"] = detected.content_type

    logger.info(
        f"[CrawlService] 크롤링 완료 — "
        f"제목: {parsed.get('title', '?')}, "
        f"자격증(필수): {parsed.get('required_certs', [])}, "
        f"기술스택: {parsed.get('required_skills', [])}"
    )

    return CrawlResult(success=True, data=parsed)
