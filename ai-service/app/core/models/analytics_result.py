from decimal import Decimal

from pydantic import BaseModel, Field

from app.schemas.analysis import CategoryAnalyticsDto


class AnalyticsResult(BaseModel):
    total_income: Decimal = Field(default=Decimal("0"))
    total_expense: Decimal = Field(default=Decimal("0"))
    balance: Decimal = Field(default=Decimal("0"))
    transaction_count: int = 0
    category_analytics: list[CategoryAnalyticsDto] = Field(default_factory=list)
    top_categories: list[CategoryAnalyticsDto] = Field(
        default_factory=list,
        max_length=3,
    )