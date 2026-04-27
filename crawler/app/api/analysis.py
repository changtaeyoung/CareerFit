"""
analysis.py — 자소서 정성 분석 API 엔드포인트

Spring Boot → POST /api/analysis/qualitative 으로 호출.
"""

import logging
from fastapi import APIRouter
from pydantic import BaseModel
from typing import Optional
from app.services.qualitative_service import analyze_qualitative
from app.config import settings

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/analysis", tags=["analysis"])


class CoverLetterItem(BaseModel):
    """자소서 문항 + 답변 한 쌍"""
    question: str
    content: str


class QualitativeRequest(BaseModel):
    """
    정성 분석 요청 DTO.
    Spring Boot의 AnalysisService에서 호출.
    """
    company_name: Optional[str] = None       # 기업명
    job_title: Optional[str] = None          # 직무명
    talent_image: Optional[str] = None       # 기업 인재상 (company.talent_image)
    job_description: Optional[str] = None    # 공고 직무 내용 (job_posting.raw_text)
    cover_letters: list[CoverLetterItem]     # 자소서 문항/답변 목록


class QualitativeResponse(BaseModel):
    """정성 분석 응답 DTO"""
    total_score: int                         # 정성 가점 (0~50)
    breakdown: dict                          # 항목별 점수
    strengths: list[str]                     # 강점
    weaknesses: list[str]                    # 개선점
    feedback: str                            # 종합 피드백


@router.post("/qualitative", response_model=QualitativeResponse)
async def qualitative_analysis(request: QualitativeRequest):
    """
    자소서 정성 분석 엔드포인트.

    Spring Boot AnalysisService에서 정성 분석이 필요할 때 호출.

    처리 흐름:
      1) 자소서가 없으면 즉시 0점 반환
      2) GPT-4o-mini로 인재상 + JD + 자소서 종합 분석
      3) 점수(0~50) + 피드백 반환
    """
    logger.info(
        f"[AnalysisAPI] 정성 분석 요청 — 자소서: {len(request.cover_letters)}개, "
        f"인재상 있음: {request.talent_image is not None}"
    )

    result = await analyze_qualitative(
        talent_image=request.talent_image,
        job_description=request.job_description,
        cover_letters=[cl.dict() for cl in request.cover_letters],
        openai_api_key=settings.OPENAI_API_KEY,
        company_name=request.company_name,
        job_title=request.job_title,
    )

    return QualitativeResponse(**result)
