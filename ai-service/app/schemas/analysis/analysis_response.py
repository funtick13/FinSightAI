from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

from app.schemas.analysis.category_analytics_dto import CategoryAnalyticsDto
from app.schemas.analysis.enums.analysis_status import AnalysisStatus
from app.schemas.analysis.enums.financial_state import FinancialState
from app.schemas.insight.insight_dto import InsightDto
from app.schemas.analysis.recommendation_dto import RecommendationDto
from app.schemas.analysis.summary_dto import SummaryDto


class AnalysisResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    request_id: UUID = Field(alias="requestId")
    user_id: UUID = Field(alias="userId")

    period: str
    status: AnalysisStatus

    summary: SummaryDto | None = None

    financial_state: FinancialState | None = Field(
        default=None,
        alias="financialState",
    )

    category_analytics: list[CategoryAnalyticsDto] = Field(
        default_factory=list,
        alias="categoryAnalytics",
    )

    top_categories: list[CategoryAnalyticsDto] = Field(
        default_factory=list,
        alias="topCategories",
        max_length=3,
    )

    insights: list[InsightDto] = Field(default_factory=list)

    recommendations: list[RecommendationDto] = Field(
        default_factory=list,
        max_length=5,
    )