from pydantic import BaseModel


class CategoryAnalyticsDto(BaseModel):
    category: str
    amount: float
    percent: float
    operationsCount: int