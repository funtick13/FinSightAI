from pydantic import BaseModel
from decimal import Decimal

class CategoryAnalyticsDto(BaseModel):
    category: str
    amount: Decimal
    percent: Decimal
    operations_count: int