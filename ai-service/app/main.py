from fastapi import FastAPI

from app.api.health_controller import router as health_router

app = FastAPI(
    title="FinSight AI Service",
    description="AI-сервис для анализа финансовых операций и генерации рекомендаций",
    version="0.1.0",
)

app.include_router(health_router)