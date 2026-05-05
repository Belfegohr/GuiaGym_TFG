from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    app_name: str = "GuiaGym API"
    debug: bool = False

    # Database
    database_url: str = "sqlite:///./guiagym.db"

    # JWT
    secret_key: str = "change-this-secret-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 60 * 24  # 24 horas

    class Config:
        env_file = ".env"


@lru_cache
def get_settings() -> Settings:
    return Settings()
