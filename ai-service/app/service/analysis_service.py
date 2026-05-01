from app.core.analytics_provider import AnalyticsProvider
from app.schemas.analysis import AnalysisRequest, AnalysisResponse, SummaryDto
from app.schemas.analysis.enums import AnalysisStatus, FinancialState


class AnalysisService:
    def __init__(self, analytics_provider: AnalyticsProvider):
        self.analytics_provider = analytics_provider

    def analyze(self, request: AnalysisRequest) -> AnalysisResponse:
        analytics = self.analytics_provider.calculate(request)

        return AnalysisResponse(
            request_id=request.request_id,
            user_id=request.user_id,
            period=request.period,
            status=AnalysisStatus.SUCCESS,
            summary=SummaryDto(
                total_income=analytics.total_income,
                total_expense=analytics.total_expense,
                balance=analytics.balance,
                period=request.period,
            ),
            financial_state=FinancialState.NO_DATA,
            category_analytics=analytics.category_analytics,
            top_categories=analytics.top_categories,
            insights=[],
            recommendations=[],
        )


def get_analysis_service() -> AnalysisService:
    analytics_provider = AnalyticsProvider()
    return AnalysisService(analytics_provider)