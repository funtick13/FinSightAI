from typing import Any

from pydantic import BaseModel, Field

from app.schemas.insight.enums import InsightType


class InsightDto(BaseModel):
    text: str
    type: InsightType
    data: dict[str, Any] | None = Field(default=None)