from typing import Any

from pydantic import BaseModel


class InsightDto(BaseModel):
    text: str
    category: str | None = None
    metrics: dict[str, Any] | None = None
