from decimal import Decimal

from pydantic import BaseModel, Field


class CategoryAggregate(BaseModel):
    amount: Decimal = Field(default=Decimal("0"))
    count: int = 0