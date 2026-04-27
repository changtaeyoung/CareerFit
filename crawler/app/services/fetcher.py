"""
fetcher.py — 웹 페이지 HTML 가져오기

전략:
  1) httpx로 먼저 시도 (빠름)
  2) JS 렌더링이 필요한 페이지 감지 시 Playwright로 재시도 (느리지만 정확)

JS 렌더링 필요 판단 기준:
  - 텍스트 콘텐츠가 너무 짧음 (500자 미만)
  - 알려진 JS 렌더링 사이트 도메인
"""

import httpx
import asyncio
import logging
from typing import Optional

logger = logging.getLogger(__name__)

# JS 렌더링이 필요한 것으로 알려진 도메인 목록
JS_REQUIRED_DOMAINS = [
    "incruit.com",
    "saramin.co.kr",
    "jobkorea.co.kr",
    "linkedin.com",
    "wanted.co.kr",
    "jumpit.com",
]

# httpx 요청 헤더 (봇 차단 우회용 User-Agent)
HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/120.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
}


def _is_js_required(url: str, html: str) -> bool:
    """JS 렌더링이 필요한지 판단"""
    # 알려진 JS 필수 도메인 체크
    for domain in JS_REQUIRED_DOMAINS:
        if domain in url:
            return True
    # 텍스트가 너무 짧으면 JS 렌더링 필요로 판단
    if len(html.strip()) < 500:
        return True
    return False


async def fetch_html_httpx(url: str) -> Optional[str]:
    """httpx로 HTML 가져오기 (빠른 방법)"""
    try:
        async with httpx.AsyncClient(
            headers=HEADERS,
            follow_redirects=True,
            timeout=15.0
        ) as client:
            response = await client.get(url)
            response.raise_for_status()
            return response.text
    except Exception as e:
        logger.warning(f"[Fetcher] httpx 실패: {e}")
        return None


async def fetch_html_playwright(url: str) -> Optional[str]:
    """
    Playwright로 JS 렌더링 후 HTML 가져오기.

    인크루트, 사람인 등 JS 기반 채용 사이트에 필요.
    Playwright는 실제 크롬 브라우저를 헤드리스 모드로 실행해서
    JS 렌더링 완료 후 HTML을 반환함.

    설치 전제조건:
      pip install playwright
      playwright install chromium
    """
    try:
        from playwright.async_api import async_playwright

        async with async_playwright() as p:
            browser = await p.chromium.launch(headless=True)
            page = await browser.new_page(
                user_agent=HEADERS["User-Agent"],
                locale="ko-KR"
            )
            await page.goto(url, wait_until="networkidle", timeout=30000)

            # 공고 내용이 로드될 때까지 추가 대기 (최대 3초)
            await asyncio.sleep(2)

            html = await page.content()
            await browser.close()
            logger.info(f"[Fetcher] Playwright 성공 — HTML 크기: {len(html)}자")
            return html

    except Exception as e:
        logger.error(f"[Fetcher] Playwright 실패: {e}")
        return None


async def fetch_html(url: str) -> Optional[str]:
    """
    메인 진입점. httpx 시도 후 JS 렌더링 필요하면 Playwright로 전환.

    흐름:
      1) httpx로 시도
      2) 결과가 없거나 JS 렌더링 필요 판단 → Playwright로 재시도
      3) 둘 다 실패 → None 반환
    """
    logger.info(f"[Fetcher] HTML 가져오기 시작 — URL: {url}")

    # 1차: httpx
    html = await fetch_html_httpx(url)

    # JS 렌더링 필요 판단
    if html is None or _is_js_required(url, html or ""):
        logger.info(f"[Fetcher] JS 렌더링 필요 판단 — Playwright로 재시도")
        html = await fetch_html_playwright(url)

    if html:
        logger.info(f"[Fetcher] HTML 수신 완료 — 크기: {len(html)}자")
    else:
        logger.error(f"[Fetcher] HTML 수신 실패")

    return html
