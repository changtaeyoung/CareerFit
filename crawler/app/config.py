# app/config.py
# Spring Boot의 @ConfigurationProperties와 동일한 역할
# .env 파일에서 환경변수를 읽어와서 타입 안전한 설정 객체로 만듦

from pydantic_settings import BaseSettings
# ↑ Java의 @ConfigurationProperties 같은 역할
# .env 파일을 자동으로 읽어서 클래스 필드에 주입해줌


# BaseSettings를 상속받으면 자동으로 환경변수를 읽어옴
class Settings(BaseSettings):
    """
    애플리케이션 전역 설정
    
    Java 비교:
    @ConfigurationProperties(prefix = "")
    public class Settings {
        private String host;  // application.yml의 host 값
        private int port;     // application.yml의 port 값
        ...
    }
    
    Python에서는:
    - 타입 힌트(: str, : int)로 자동 변환
    - 필드명이 대문자면 환경변수명과 매칭 (HOST → HOST)
    - .env 파일이 자동으로 로드됨
    """
    
    # 서버 설정
    HOST: str = "0.0.0.0"  # = "0.0.0.0" 은 기본값 (환경변수 없을 때)
    PORT: int = 8000
    
    # Spring Boot URL (나중에 HTTP 호출용)
    SPRING_BOOT_URL: str = "http://localhost:8080"
    
    # DB 설정
    DB_HOST: str = "localhost"
    DB_PORT: int = 5432
    DB_NAME: str = "careerfit"
    DB_USER: str = "postgres"
    DB_PASSWORD: str = ""
    
    # OpenAI API
    OPENAI_API_KEY: str = ""
    
    # 로그 레벨
    LOG_LEVEL: str = "INFO"
    
    # 내부 설정 (이 클래스가 어떻게 동작할지 정의)
    class Config:
        """
        pydantic-settings 동작 설정
        
        - env_file: .env 파일 경로 지정
        - case_sensitive: 환경변수명 대소문자 구분 여부
        """
        env_file = ".env"  # .env 파일에서 값 로드
        case_sensitive = True  # 대소문자 구분 (HOST와 host는 다름)


# 전역 설정 객체 생성 (싱글톤)
# Java의 @Bean 같은 역할 — 앱 전체에서 하나만 생성해서 공유
settings = Settings()

# 다른 파일에서 사용법:
# from app.config import settings
# print(settings.PORT)  # 8000 출력
