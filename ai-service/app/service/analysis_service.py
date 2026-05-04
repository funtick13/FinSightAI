from app.core.analytics_provider import AnalyticsProvider
from app.schemas.analysis import AnalysisRequest, AnalysisResponse, SummaryDto
from app.schemas.analysis.enums import AnalysisStatus, FinancialState
from app.core.state_predictor import StatePredictor

class AnalysisService:
    def __init__(
            self,
            analytics_provider: AnalyticsProvider,
            state_predictor: StatePredictor
    ):
        self.analytics_provider = analytics_provider
        self.state_predictor = state_predictor

    def analyze(self, request: AnalysisRequest) -> AnalysisResponse:
        analytics = self.analytics_provider.calculate(request)
        financial_state = self.state_predictor.predict(analytics)

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
            financial_state=financial_state,
            category_analytics=analytics.category_analytics,
            top_categories=analytics.top_categories,
            insights=[],
            recommendations=[],
        )


def get_analysis_service() -> AnalysisService:
    analytics_provider = AnalyticsProvider()
    state_predictor = StatePredictor()

    return AnalysisService(
        analytics_provider=analytics_provider,
        state_predictor=state_predictor,
    )