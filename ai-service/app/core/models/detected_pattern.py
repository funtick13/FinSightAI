from typing import Any

from pydantic import BaseModel, Field

from app.schemas.pattern.enums import PatternType


class DetectedPattern(BaseModel):
    type: PatternType
    data: dict[str, Any] = Field(default_factory=dict)