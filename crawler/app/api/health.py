# app/api/health.py
# 헬스체크 엔드포인트 (Spring Actuator의 /actuator/health와 동일)
# 
# Spring Boot에서 Controller를 별도 파일로 분리하는 것처럼
# FastAPI에서도 엔드포인트를 기능별로 파일 분리 가능

from fastapi import APIRouter
# ↑ Spring Boot의 @RestController 같은 역할
# 여러 엔드포인트를 묶어서 라우팅하는 객체

import logging

logger = logging.getLogger(__name__)


# APIRouter 생성 (Spring Boot의 @RequestMapping 역할)
router = APIRouter(
    prefix="/health",  # 이 라우터의 모든 엔드포인트는 /health로 시작
    tags=["Health Check"]  # Swagger 문서에서 그룹명
)
# ↑ Java에서:
# @RestController
# @RequestMapping("/health")
# public class HealthController { ... }


# 헬스체크 엔드포인트
@router.get("")
async def health_check():
    """
    서버 상태 확인용 엔드포인트
    
    Java 비교:
    @GetMapping("")  // prefix가 /health니까 최종 경로는 /health
    public Map<String, String> healthCheck() {
        return Map.of("status", "healthy");
    }
    
    사용처:
    - 로드밸런서가 서버 살아있는지 체크
    - 모니터링 도구에서 주기적으로 ping
    - 배포 후 서버 정상 동작 확인
    """
    logger.debug("헬스체크 호출됨")
    return {
        "status": "healthy",
        "service": "CareerFit AI"
    }


# 이 router를 main.py에서 등록해야 실제로 동작함
# main.py에 아래 코드 추가 필요:
# from app.api.health import router as health_router
# app.include_router(health_router)
