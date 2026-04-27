"""
qualitative_service.py — 자소서 정성 분석 서비스

역할:
  기업 인재상 + 공고 직무 내용 + 사용자 자소서 답변을 종합하여
  GPT-4o-mini로 정성적 핏 점수(0~50)와 피드백을 생성.

분석 기준:
  1) 인재상 키워드 반영도: 기업이 원하는 인재상이 자소서에 드러나는가?
  2) 직무 이해도: JD(공고 내용)에서 언급된 업무/기술을 얼마나 이해하고 있는가?
  3) 구체성: 경험/사례가 구체적으로 서술되어 있는가?
  4) 논리성: 지원동기 → 경험 → 목표로 이어지는 흐름이 자연스러운가?

점수 범위: 0~50점
  - 0점: 자소서 없음
  - 1~15점: 인재상/직무 연관성 낮음
  - 16~30점: 부분 반영
  - 31~40점: 잘 반영됨
  - 41~50점: 매우 잘 반영됨
"""

import json
import logging
import re
from typing import Optional
from openai import AsyncOpenAI

logger = logging.getLogger(__name__)

ANALYSIS_PROMPT = """
당신은 현재 {company_name}의 {job_title} 부문 수석 채용 면접관입니다.
단순히 직무 스킬이 뛰어난 사람을 넘어서, 우리 {company_name}의 DNA(인재상)를 완벽히 갖춘 인재를 찾고 있습니다.

아래 정보를 바탕으로 지원자의 자소서를 실무진의 시각에서 날카롭게 평가해 주세요.

[평가 및 피드백 절대 원칙]
1. 인재상 키워드를 지원자에게 그대로 사용하라고 조언하지 마십시오(예: "도전정신이라는 단어를 쓰세요"). 반드시 'Show, Don't Tell' 원칙에 따라, 지원자의 구체적인 행동과 결과 속에서 인재상이 자연스럽게(은연중에) 묻어나도록 서술 방식을 수정하라고 조언하십시오.
2. AI가 쓴 것 같은 거창하고 과장된 미사여구(예: '무한한 가능성', '혁신적인 마인드로 무장한')를 추가하라고 제안하지 마십시오. 담백하고 사실 기반의 실무적인 톤앤매너를 유지하도록 유도하세요.
3. "경험을 더 구체화하세요" 같은 보편적인 조언 대신, 실무 면접관 입장에서 이 지원자의 경험 중 어떤 부분이 설득력이 떨어지는지 명확하고 구체적으로 꼬집어 주십시오.

## 기업 인재상
{talent_image}

## 채용공고 직무 내용
{job_description}

## 지원자 자기소개서
{cover_letters}

## 평가 기준
다음 4가지 기준으로 각각 평가하여 총점(0~50점)을 산출하세요:

1. 인재상 부합도 (0~15점): 기업 인재상의 핵심 키워드가 지원자의 행동과 결과 속에서 억지스럽지 않고 자연스럽게 증명되고 있는가?
2. 직무 이해도 (0~15점): 공고에서 요구하는 업무와 기술에 대한 실무적 이해가 드러나는가?
3. 구체성 (0~10점): 경험/사례가 거창한 수식어가 아닌 객관적인 수치, 상황, 결과로 담백하게 서술되어 있는가?
4. 논리적 흐름 (0~10점): 지원동기, 역량, 포부가 일관성 있게 연결되는가?

## 출력 형식
반드시 아래 JSON 형식으로만 응답하세요. 다른 내용은 절대 추가하지 마세요.

{{
  "total_score": 점수(0~50 정수),
  "breakdown": {{
    "talent_match": 점수(0~15),
    "job_understanding": 점수(0~15),
    "specificity": 점수(0~10),
    "logical_flow": 점수(0~10)
  }},
  "strengths": ["강점1", "강점2"],
  "weaknesses": ["개선점1", "개선점2"],
  "feedback": "전체적인 피드백 (100자 내외)"
}}
"""


async def analyze_qualitative(
    company_name: Optional[str],
    job_title: Optional[str],
    talent_image: Optional[str],
    job_description: Optional[str],
    cover_letters: list[dict],
    openai_api_key: str,
) -> dict:
    """
    자소서 정성 분석 실행.

    :param company_name:    기업명
    :param job_title:       직무명
    :param talent_image:    기업의 인재상 (company.talent_image)
    :param job_description: 공고 직무 내용 (job_posting.raw_text)
    :param cover_letters:   자소서 목록. 각 항목: {"question": "...", "content": "..."}
    :param openai_api_key:  OpenAI API 키
    :return: 분석 결과 dict
    """
    if not cover_letters:
        logger.info("[QualitativeService] 자소서 없음 → 0점 반환")
        return _empty_result("자소서가 작성되지 않아 정성 평가를 진행할 수 없습니다.")

    if not openai_api_key:
        logger.error("[QualitativeService] OPENAI_API_KEY 없음")
        return _empty_result("AI 분석 서비스를 사용할 수 없습니다.")

    # 자소서 포맷팅: 문항별 Q/A 형태
    formatted_letters = "\n\n".join(
        f"[문항 {i+1}] {cl.get('question', '(문항 없음)')}\n{cl.get('content', '')}"
        for i, cl in enumerate(cover_letters)
    )

    prompt = ANALYSIS_PROMPT.format(
        company_name=company_name or "해당 기업",
        job_title=job_title or "해당 직무",
        talent_image=talent_image or "인재상 정보 없음 (직무 이해도와 구체성 위주로 평가)",
        job_description=job_description or "직무 내용 정보 없음",
        cover_letters=formatted_letters
    )

    try:
        logger.info(f"[QualitativeService] GPT 분석 시작 — 자소서 {len(cover_letters)}개")
        client = AsyncOpenAI(api_key=openai_api_key)

        response = await client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system",
                    "content": "당신은 채용 전문가입니다. JSON 형식으로만 응답하세요."
                },
                {"role": "user", "content": prompt}
            ],
            max_tokens=1000,
            temperature=0.3,  # 일관성 있는 평가를 위해 낮게 설정
        )

        content = response.choices[0].message.content.strip()

        # ```json ... ``` 코드블록 제거
        content = re.sub(r'^```json\s*', '', content)
        content = re.sub(r'\s*```$', '', content)

        result = json.loads(content)

        # 점수 범위 검증 (GPT가 범위를 벗어날 경우 대비)
        result["total_score"] = max(0, min(50, int(result.get("total_score", 0))))

        logger.info(f"[QualitativeService] 분석 완료 — 점수: {result['total_score']}점")
        return result

    except json.JSONDecodeError as e:
        logger.error(f"[QualitativeService] JSON 파싱 실패: {e}\n응답: {content[:200]}")
        return _empty_result("분석 결과 파싱에 실패했습니다.")
    except Exception as e:
        logger.error(f"[QualitativeService] GPT 호출 실패: {e}")
        return _empty_result(f"AI 분석 중 오류가 발생했습니다: {str(e)[:100]}")


def _empty_result(reason: str) -> dict:
    """자소서 없거나 분석 실패 시 반환하는 기본 결과"""
    return {
        "total_score": 0,
        "breakdown": {
            "talent_match": 0,
            "job_understanding": 0,
            "specificity": 0,
            "logical_flow": 0
        },
        "strengths": [],
        "weaknesses": [],
        "feedback": reason,
    }
