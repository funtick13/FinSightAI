from pydantic import BaseModel


class SummaryDto(BaseModel):
    totalIncome: float
    totalExpense: float
    balance: float
    period: str