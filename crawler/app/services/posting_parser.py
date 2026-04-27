"""
posting_parser.py — 추출된 텍스트를 구조화된 공고 정보로 변환

역할:
  이미지/PDF/HTML에서 추출된 원시 텍스트를 GPT-4o-mini로 분석하여
  job_posting 테이블에 저장할 수 있는 구조화된 데이터로 변환.

출력 구조:
  {
    "title": "2024 하반기 신입행원 채용",
    "company_name": "IBK기업은행",
    "job_type": "IT",
    "season": "2024 하반기",
    "deadline": "2024-10-31",
    "required_certs": ["정보처리기사"],
    "preferred_certs": ["SQLD", "ADsP"],
    "required_languages": [{"lang": "TOEIC", "min_score": 700}],
    "required_skills": ["Java", "Spring", "Python"],
    "job_description": "IT 직군 업무 설명...",
    "raw_text": "원본 전체 텍스트"
  }
"""

import json
import logging
import re
from typing import Optional
from openai import AsyncOpenAI

logger = logging.getLogger(__name__)

PARSE_PROMPT = """
당신은 한국 기업 채용공고 분석 전문가입니다.
아래 채용공고 텍스트를 분석하여 JSON 형태로 구조화해주세요.

반드시 아래 JSON 형식으로만 응답하세요. 다른 설명은 절대 추가하지 마세요.

{
  "title": "공고 제목",
  "company_name": "회사명",
  "job_type": "IT | BACKEND | FRONTEND | FULLSTACK | DATA | AI | INFRA | SECURITY | DIGITAL | ETC",
  "season": "2024 상반기 | 2024 하반기 | 수시 등",
  "deadline": "YYYY-MM-DD 형식 또는 null (마감일 없으면 null)",
  "started_at": "YYYY-MM-DD 형식 또는 null (시작일 없으면 null)",
  "job_description": "직무 내용 요약 (500자 이내)",
  "required_certs": ["필수 자격증명 리스트"],
  "preferred_certs": ["우대 자격증명 리스트"],
  "required_languages": [
    {"lang_type": "TOEIC | TOEIC Speaking | OPIc | IELTS 등", "min_score": 숫자 또는 null, "min_grade": "등급 문자열 또는 null"}
  ],
  "required_skills": ["필요 기술스택 리스트 (Java, Python, Spring, SQL 등)"],
  "talent_keywords": ["인재상 관련 키워드 (도전, 혁신, 소통 등)"]
}

주의사항:
- 공고에 명시되지 않은 항목은 빈 배열([]) 또는 null로 표시
- 자격증은 공식 명칭으로 (정보처리기사, SQLD, ADsP, OPIc 등)
- job_type은 IT 직군이면 해당 세부 직군으로, 일반 직군이면 ETC
- 마감일 형식이 "~10/31"이면 올해 연도로 추정

채용공고 텍스트:
{text}
"""


async def parse_posting_text(raw_text: str, openai_api_key: str) -> Optional[dict]:
    """
    원시 텍스트를 GPT-4o-mini로 분석하여 구조화된 공고 정보를 반환.

    왜 GPT를 쓰나:
      정규식으로 파싱하면 사이트마다 형식이 달라서 유지보수가 어려움.
      GPT는 자연어를 이해하므로 형식에 관계없이 정보를 추출할 수 있음.
      gpt-4o-mini는 저렴해서 공고 1개 파싱 비용이 $0.001 미만.
    """
    if not openai_api_key:
        logger.error("[PostingParser] OPENAI_API_KEY 없음")
        return None

    if not raw_text or len(raw_text.strip()) < 50:
        logger.warning("[PostingParser] 텍스트가 너무 짧음")
        return None

    # 텍스트가 너무 길면 앞 8000자만 사용 (GPT 컨텍스트 제한 고려)
    text_to_parse = raw_text[:8000]

    try:
        logger.info(f"[PostingParser] GPT 파싱 시작 — 텍스트 {len(text_to_parse)}자")
        client = AsyncOpenAI(api_key=openai_api_key)

        response = await client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system",
                    "content": "당신은 채용공고 분석 전문가입니다. 지시에 따라 JSON만 출력하세요."
                },
                {
                    "role": "user",
                    "content": PARSE_PROMPT.replace("{text}", text_to_parse)
                }
            ],
            max_tokens=2000,
            temperature=0.1,  # 낮은 temperature → 일관성 있는 출력
        )

        content = response.choices[0].message.content.strip()

        # ```json ... ``` 코드블록 제거
        content = re.sub(r'^```json\s*', '', content)
        content = re.sub(r'\s*```$', '', content)

        parsed = json.loads(content)
        parsed["raw_text"] = raw_text  # 원본 텍스트도 보존
        logger.info(f"[PostingParser] 파싱 완료 — 제목: {parsed.get('title', '?')}")
        return parsed

    except json.JSONDecodeError as e:
        logger.error(f"[PostingParser] JSON 파싱 실패: {e}\n응답: {content[:200]}")
        return None
    except Exception as e:
        logger.error(f"[PostingParser] GPT 호출 실패: {e}")
        return None
