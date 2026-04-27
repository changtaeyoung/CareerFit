"""
content_detector.py — 채용공고 콘텐츠 유형 감지

HTML을 분석해서 공고 내용이 어떤 형태로 되어있는지 판단:
  - TEXT: 일반 HTML 텍스트
  - IMAGE: 이미지 파일 (jpg, png, gif)
  - PDF: PDF 파일 링크
  - MIXED: 이미지 + 텍스트 혼합

결과에 따라 파싱 전략이 달라짐.
"""

import re
import logging
from dataclasses import dataclass, field
from bs4 import BeautifulSoup
from typing import List

logger = logging.getLogger(__name__)

# 이미지 확장자 패턴
IMAGE_EXT_PATTERN = re.compile(r'\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$', re.IGNORECASE)

# PDF 확장자 패턴
PDF_EXT_PATTERN = re.compile(r'\.(pdf)(\?.*)?$', re.IGNORECASE)

# 채용공고 관련 키워드 (공고 이미지를 담는 <img> 태그 주변 컨텍스트 판단용)
JOB_CONTEXT_KEYWORDS = [
    '채용', '모집', '지원', '공고', '자격', '우대', '업무', '접수',
    '마감', '전형', '서류', '면접', '합격'
]


@dataclass
class DetectedContent:
    """감지된 콘텐츠 정보"""
    content_type: str                    # TEXT | IMAGE | PDF | MIXED
    text_content: str = ""               # HTML에서 직접 추출한 텍스트
    image_urls: List[str] = field(default_factory=list)   # 이미지 URL 목록
    pdf_urls: List[str] = field(default_factory=list)     # PDF URL 목록
    base_url: str = ""                   # 상대 URL → 절대 URL 변환용


def detect_content(html: str, page_url: str) -> DetectedContent:
    """
    HTML에서 공고 콘텐츠 유형을 감지한다.

    감지 우선순위:
      1) PDF 링크 있으면 → PDF 포함
      2) 이미지가 공고 콘텐츠인지 판단 → IMAGE 포함
      3) 텍스트 충분하면 → TEXT
      4) 텍스트 부족하면 → IMAGE로 폴백
    """
    soup = BeautifulSoup(html, "lxml")
    base_url = _extract_base_url(page_url)

    # 1) PDF 링크 감지
    pdf_urls = _find_pdf_urls(soup, base_url)

    # 2) 이미지 감지
    image_urls = _find_job_images(soup, base_url)

    # 3) 텍스트 감지
    text_content = _extract_text(soup)
    has_enough_text = len(text_content.strip()) > 200

    # 4) 콘텐츠 타입 결정
    has_pdf = len(pdf_urls) > 0
    has_image = len(image_urls) > 0

    if has_pdf and has_image:
        content_type = "MIXED"
    elif has_pdf:
        content_type = "PDF"
    elif has_image and not has_enough_text:
        content_type = "IMAGE"
    elif has_image and has_enough_text:
        content_type = "MIXED"
    else:
        content_type = "TEXT"

    logger.info(
        f"[ContentDetector] 감지 결과 — 타입: {content_type}, "
        f"텍스트: {len(text_content)}자, 이미지: {len(image_urls)}개, PDF: {len(pdf_urls)}개"
    )

    return DetectedContent(
        content_type=content_type,
        text_content=text_content,
        image_urls=image_urls,
        pdf_urls=pdf_urls,
        base_url=base_url,
    )


def _find_pdf_urls(soup: BeautifulSoup, base_url: str) -> List[str]:
    """PDF 파일 링크를 모두 수집"""
    urls = []
    for a in soup.find_all("a", href=True):
        href = a["href"]
        if PDF_EXT_PATTERN.search(href):
            urls.append(_to_absolute_url(href, base_url))
    return urls


def _find_job_images(soup: BeautifulSoup, base_url: str) -> List[str]:
    """
    공고 내용이 담긴 이미지를 감지.

    단순히 모든 img를 가져오면 로고, 아이콘 등이 포함됨.
    크기가 충분히 크거나 공고 관련 컨텍스트에 있는 이미지만 선택.
    """
    job_images = []

    for img in soup.find_all("img", src=True):
        src = img.get("src", "")

        # 이미지 확장자 아니면 스킵
        if not IMAGE_EXT_PATTERN.search(src):
            continue

        # 너무 작은 아이콘 스킵 (width/height가 명시된 경우)
        width = img.get("width", "")
        height = img.get("height", "")
        try:
            if width and int(str(width).replace("px", "")) < 200:
                continue
            if height and int(str(height).replace("px", "")) < 100:
                continue
        except (ValueError, TypeError):
            pass

        # 로고/아이콘 관련 파일명 스킵
        src_lower = src.lower()
        if any(kw in src_lower for kw in ["logo", "icon", "banner_top", "btn_", "bullet"]):
            continue

        abs_url = _to_absolute_url(src, "")  # 인크루트는 보통 절대 URL 사용
        if abs_url:
            job_images.append(abs_url)

    return job_images


def _extract_text(soup: BeautifulSoup) -> str:
    """
    공고 관련 텍스트를 추출.
    스크립트, 스타일, 네비게이션 등 노이즈 제거 후 텍스트 추출.
    """
    # 노이즈 태그 제거
    for tag in soup.find_all(["script", "style", "nav", "header", "footer", "noscript"]):
        tag.decompose()

    # 공고 본문 영역 우선 탐색 (사이트마다 다른 클래스명)
    content_selectors = [
        ".recruit_detail",      # 인크루트 공고 본문
        ".job_detail",
        ".job-description",
        "#jobDetail",
        "#recruit_detail",
        ".hire_view",
        ".content_area",
        "article",
        "main",
    ]

    for selector in content_selectors:
        area = soup.select_one(selector)
        if area:
            text = area.get_text(separator="\n", strip=True)
            if len(text) > 100:
                return text

    # 폴백: body 전체 텍스트
    body = soup.find("body")
    if body:
        return body.get_text(separator="\n", strip=True)

    return soup.get_text(separator="\n", strip=True)


def _extract_base_url(url: str) -> str:
    """URL에서 도메인 부분만 추출 (상대 URL → 절대 URL 변환용)"""
    match = re.match(r'(https?://[^/]+)', url)
    return match.group(1) if match else ""


def _to_absolute_url(url: str, base_url: str) -> str:
    """상대 URL을 절대 URL로 변환"""
    if not url:
        return ""
    if url.startswith("http"):
        return url
    if url.startswith("//"):
        return "https:" + url
    if url.startswith("/") and base_url:
        return base_url + url
    return url
