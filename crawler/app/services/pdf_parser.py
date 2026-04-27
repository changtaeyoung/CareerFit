"""
pdf_parser.py — PDF 첨부 채용공고 파싱

전략:
  1) pdfplumber로 텍스트 추출 (텍스트 레이어가 있는 PDF)
  2) 텍스트가 너무 짧으면 (스캔 PDF 판단) → 각 페이지를 이미지로 변환 후 OpenAI Vision
"""

import httpx
import io
import logging
import base64
from typing import Optional

logger = logging.getLogger(__name__)

# 텍스트 레이어가 충분한지 판단하는 최소 문자 수
MIN_TEXT_CHARS_PER_PAGE = 50


async def parse_pdf_from_url(pdf_url: str, openai_api_key: str = "") -> Optional[str]:
    """
    PDF URL에서 텍스트를 추출한다.

    흐름:
      1) PDF 다운로드
      2) pdfplumber로 텍스트 추출 시도
      3) 텍스트 충분 → 반환
      4) 텍스트 부족 (스캔 PDF) → 이미지 변환 → OpenAI Vision
    """
    try:
        logger.info(f"[PdfParser] PDF 다운로드 — {pdf_url[:80]}...")
        async with httpx.AsyncClient(timeout=30.0, follow_redirects=True) as client:
            response = await client.get(pdf_url)
            response.raise_for_status()
            pdf_bytes = response.content

        logger.info(f"[PdfParser] PDF 다운로드 완료 — {len(pdf_bytes):,}bytes")
        return await parse_pdf_bytes(pdf_bytes, openai_api_key)

    except Exception as e:
        logger.error(f"[PdfParser] PDF 다운로드 실패: {e}")
        return None


async def parse_pdf_bytes(pdf_bytes: bytes, openai_api_key: str = "") -> Optional[str]:
    """
    PDF 바이트를 받아 텍스트를 추출한다.
    pdfplumber로 먼저 시도하고, 스캔 PDF면 Vision API로 폴백.
    """
    try:
        import pdfplumber

        text_pages = []
        with pdfplumber.open(io.BytesIO(pdf_bytes)) as pdf:
            for page_num, page in enumerate(pdf.pages):
                text = page.extract_text() or ""
                text_pages.append((page_num, text))

        # 텍스트 레이어 품질 판단
        total_text = "\n".join(t for _, t in text_pages)
        avg_chars = len(total_text) / max(len(text_pages), 1)

        if avg_chars >= MIN_TEXT_CHARS_PER_PAGE:
            logger.info(f"[PdfParser] pdfplumber 성공 — 평균 {avg_chars:.0f}자/페이지")
            return total_text

        # 텍스트 부족 → 스캔 PDF 판단 → Vision API로 폴백
        logger.info(f"[PdfParser] 텍스트 부족 (평균 {avg_chars:.0f}자) — 스캔 PDF 판단, Vision API로 폴백")

        if not openai_api_key:
            logger.warning("[PdfParser] OPENAI_API_KEY 없음 — 부분 텍스트 반환")
            return total_text or None

        return await _parse_scanned_pdf(pdf_bytes, openai_api_key)

    except ImportError:
        logger.error("[PdfParser] pdfplumber 미설치. pip install pdfplumber 실행 필요")
        return None
    except Exception as e:
        logger.error(f"[PdfParser] PDF 파싱 실패: {e}")
        return None


async def _parse_scanned_pdf(pdf_bytes: bytes, openai_api_key: str) -> Optional[str]:
    """
    스캔 PDF를 이미지로 변환 후 OpenAI Vision API로 텍스트 추출.

    pdf2image 라이브러리 사용 (poppler 필요):
      macOS: brew install poppler
      ubuntu: apt-get install poppler-utils
    """
    try:
        from pdf2image import convert_from_bytes
        from openai import AsyncOpenAI

        logger.info("[PdfParser] 스캔 PDF → 이미지 변환 시작")
        images = convert_from_bytes(pdf_bytes, dpi=200)  # 200 DPI가 OCR에 적합

        client_ai = AsyncOpenAI(api_key=openai_api_key)
        all_texts = []

        for page_num, image in enumerate(images):
            # PIL Image → base64
            import io as _io
            buf = _io.BytesIO()
            image.save(buf, format="PNG")
            image_b64 = base64.b64encode(buf.getvalue()).decode("utf-8")

            logger.info(f"[PdfParser] 페이지 {page_num + 1}/{len(images)} Vision API 호출")
            response = await client_ai.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": f"data:image/png;base64,{image_b64}",
                                    "detail": "high"
                                }
                            },
                            {
                                "type": "text",
                                "text": (
                                    "이 이미지는 한국 기업 채용공고 PDF의 한 페이지입니다. "
                                    "모든 텍스트를 빠짐없이 추출해주세요. "
                                    "원본 구조를 유지하되 텍스트만 출력하세요."
                                )
                            }
                        ]
                    }
                ],
                max_tokens=4096
            )
            all_texts.append(response.choices[0].message.content)

        combined = "\n\n".join(all_texts)
        logger.info(f"[PdfParser] 스캔 PDF 파싱 완료 — 총 {len(combined)}자")
        return combined

    except ImportError:
        logger.warning("[PdfParser] pdf2image 미설치. pip install pdf2image 필요")
        return None
    except Exception as e:
        logger.error(f"[PdfParser] 스캔 PDF 처리 실패: {e}")
        return None
