from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.analysis_controller import router as analysis_router
from app.api.health_controller import router as health_router
from app.config.logging_config import setup_logging
from app.config.settings import settings

setup_logging()

app = FastAPI(
    title=settings.service_name,
    version=settings.service_version,
    description="AI-сервис для анализа финансовых операций и генерации рекомендаций",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_router)
app.include_router(analysis_router)