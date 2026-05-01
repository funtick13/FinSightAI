from datetime import date
from typing import Literal
from uuid import UUID

from decimal import Decimal
from pydantic import BaseModel, Field

TransactionType = Literal["INCOME", "EXPENSE"]


class TransactionDto(BaseModel):
    id: UUID
    date: date
    time: str | None = Field(default=None, pattern=r"^\d{2}:\d{2}$")
    type: TransactionType
    amount: Decimal
    category: str
    description: str | None = None
