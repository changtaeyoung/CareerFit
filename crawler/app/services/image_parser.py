"""
image_parser.py — 이미지 기반 채용공고 파싱

이미지로 된 채용공고를 텍스트로 변환.

방법:
  OpenAI Vision API (gpt-4o-mini):
    - 한국어 인식 정확도 최고
    - 이미지를 직접 이해해서 구조화된 정보 추출 가능
    - 비용: 이미지 1장당 약 $0.001 (매우 저렴)

pytesseract는 설치 복잡성(tesseract 별도 설치 필요) + 한국어 정확도 문제로
OpenAI Vision으로 대체.
"""

import httpx
import base64
import logging
from typing import Optional
from openai import AsyncOpenAI

logger = logging.getLogger(__name__)


async def parse_image_to_text(image_url: str, openai_api_key: str) -> Optional[str]:
    """
    이미지 URL에서 채용공고 텍스트를 추출한다.

    흐름:
      1) 이미지 URL → 바이트 다운로드
      2) base64 인코딩
      3) OpenAI Vision API (gpt-4o-mini)로 텍스트 추출
      4) 추출된 텍스트 반환

    왜 base64로 전송하나:
      - OpenAI Vision은 URL 직접 전달도 되지만,
        일부 사이트는 외부 접근 차단이 되어 있음
      - base64로 직접 전달하면 접근 차단 우회 가능
    """
    if not openai_api_key:
        logger.error("[ImageParser] OPENAI_API_KEY 없음")
        return None

    try:
        # 1) 이미지 다운로드
        logger.info(f"[ImageParser] 이미지 다운로드 — {image_url[:80]}...")
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.get(
                image_url,
                headers={
                    "User-Agent": (
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                        "AppleWebKit/537.36"
                    )
                },
                follow_redirects=True
            )
            response.raise_for_status()
            image_bytes = response.content

        # 2) 이미지 타입 감지
        content_type = response.headers.get("content-type", "image/jpeg")
        if "png" in content_type:
            media_type = "image/png"
        elif "gif" in content_type:
            media_type = "image/gif"
        elif "webp" in content_type:
            media_type = "image/webp"
        else:
            media_type = "image/jpeg"

        # 3) base64 인코딩
        image_b64 = base64.b64encode(image_bytes).decode("utf-8")
        logger.info(f"[ImageParser] 이미지 크기: {len(image_bytes):,}bytes — Vision API 호출")

        # 4) OpenAI Vision API 호출
        client_ai = AsyncOpenAI(api_key=openai_api_key)
        response_ai = await client_ai.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:{media_type};base64,{image_b64}",
                                "detail": "high"  # 고해상도 분석
                            }
                        },
                        {
                            "type": "text",
                            "text": (
                                "이 이미지는 한국 기업의 채용공고입니다. "
                                "이미지에 있는 모든 텍스트를 빠짐없이 추출해주세요. "
                                "원본 레이아웃을 최대한 유지하되, 텍스트 내용만 출력하세요. "
                                "이미지에 없는 내용은 추가하지 마세요."
                            )
                        }
                    ]
                }
            ],
            max_tokens=4096
        )

        extracted_text = response_ai.choices[0].message.content
        logger.info(f"[ImageParser] 텍스트 추출 완료 — {len(extracted_text)}자")
        return extracted_text

    except Exception as e:
        logger.error(f"[ImageParser] 이미지 파싱 실패 — {image_url[:50]}: {e}")
        return None


async def parse_multiple_images(image_urls: list[str], openai_api_key: str) -> str:
    """
    여러 이미지를 순서대로 파싱하여 하나의 텍스트로 합침.
    공고가 여러 장 이미지로 구성된 경우 사용.
    """
    all_texts = []
    for i, url in enumerate(image_urls):
        logger.info(f"[ImageParser] 이미지 {i+1}/{len(image_urls)} 처리 중")
        text = await parse_image_to_text(url, openai_api_key)
        if text:
            all_texts.append(text)

    combined = "\n\n---\n\n".join(all_texts)
    logger.info(f"[ImageParser] 전체 이미지 파싱 완료 — 총 {len(combined)}자")
    return combined
