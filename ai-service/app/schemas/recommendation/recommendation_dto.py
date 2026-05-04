from typing import Any

from pydantic import BaseModel, Field

from app.schemas.recommendation.enum import RecommendationType


class RecommendationDto(BaseModel):
    text: str
    type: RecommendationType
    data: dict[str, Any] | None = Field(default=None)