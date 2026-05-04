from app.core.analytics_provider import AnalyticsProvider
from app.core.insight_generator import InsightGenerator
from app.core.state_predictor import StatePredictor
from app.schemas.analysis import AnalysisRequest, AnalysisResponse, SummaryDto
from app.schemas.analysis.enums import AnalysisStatus


class AnalysisService:
    def __init__(
            self,
            analytics_provider: AnalyticsProvider,
            state_predictor: StatePredictor,
            insight_generator: InsightGenerator
    ):
        self.analytics_provider = analytics_provider
        self.state_predictor = state_predictor
        self.insight_generator = insight_generator

    def analyze(self, request: AnalysisRequest) -> AnalysisResponse:
        analytics = self.analytics_provider.calculate(request)
        financial_state = self.state_predictor.predict(analytics)
        insight_generator = self.insight_generator.generate(
            analytics=analytics,
            financial_state=financial_state
        )

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
            insights=insight_generator,
            recommendations=[],
        )


def get_analysis_service() -> AnalysisService:
    analytics_provider = AnalyticsProvider()
    state_predictor = StatePredictor()
    insight_generator = InsightGenerator()

    return AnalysisService(
        analytics_provider=analytics_provider,
        state_predictor=state_predictor,
        insight_generator=insight_generator
    )