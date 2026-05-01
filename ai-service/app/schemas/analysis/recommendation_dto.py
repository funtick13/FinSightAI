from typing import Any

from pydantic import BaseModel, Field

from app.schemas.analysis.enums.recommendation_type import RecommendationType


class RecommendationDto(BaseModel):
    text: str
    type: RecommendationType
    categories: list[str] = Field(default_factory=list)
    metrics: dict[str, Any] | None = None