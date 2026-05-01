from datetime import date
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

from app.schemas.analysis.statement_summary_dto import StatementSummaryDto
from app.schemas.analysis.transaction_dto import TransactionDto


class StatementPeriodDto(BaseModel):
    from_: date = Field(alias="from")
    to: date

    model_config = ConfigDict(populate_by_name=True)


class AnalysisRequest(BaseModel):
    request_id: UUID = Field(alias="requestId")
    user_id: UUID = Field(alias="userId")
    period: str
    bank: str
    statement_ids: list[UUID] = Field(alias="statementIds")
    statement_period: StatementPeriodDto = Field(alias="statementPeriod")
    statement_summary: StatementSummaryDto = Field(alias="statementSummary")
    transactions: list[TransactionDto]

    model_config = ConfigDict(populate_by_name=True)
