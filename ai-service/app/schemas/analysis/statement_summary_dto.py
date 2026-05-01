from decimal import Decimal

from pydantic import BaseModel, ConfigDict, Field


class StatementSummaryDto(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    opening_balance: Decimal = Field(alias="openingBalance")
    total_income: Decimal = Field(alias="totalIncome")
    total_expense: Decimal = Field(alias="totalExpense")
    closing_balance: Decimal = Field(alias="closingBalance")