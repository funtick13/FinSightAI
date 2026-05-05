from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    service_name: str = "FinSight AI Service"
    service_version: str = "0.1.0"
    environment: str = "local"

    allowed_origins: list[str] = [
        "http://localhost:8080",
        "http://127.0.0.1:8080",
        "http://localhost:3000",
        "http://127.0.0.1:3000",
    ]

    class Config:
        env_file = ".env"


settings = Settings()