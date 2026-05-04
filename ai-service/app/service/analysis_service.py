from app.core.analytics_provider import AnalyticsProvider
from app.core.insight_generator import InsightGenerator
from app.core.pattern_detector import PatternDetector
from app.core.recommendation_generator import RecommendationGenerator
from app.core.state_predictor import StatePredictor
from app.schemas.analysis import AnalysisRequest, AnalysisResponse, SummaryDto
from app.schemas.analysis.enums import AnalysisStatus


class AnalysisService:
    def __init__(
            self,
            analytics_provider: AnalyticsProvider,
            state_predictor: StatePredictor,
            insight_generator: InsightGenerator,
            recommendation_generator: RecommendationGenerator,
            pattern_detector: PatternDetector,
    ):
        self.analytics_provider = analytics_provider
        self.state_predictor = state_predictor
        self.insight_generator = insight_generator
        self.recommendation_generator = recommendation_generator
        self.pattern_detector = pattern_detector

    def analyze(self, request: AnalysisRequest) -> AnalysisResponse:
        analytics = self.analytics_provider.calculate(request)
        patterns = self.pattern_detector.detect(
            analytics=analytics,
            request=request,
        )
        financial_state = self.state_predictor.predict(analytics)
        insights = self.insight_generator.generate(
            analytics=analytics,
            financial_state=financial_state,
            patterns=patterns,
        )
        recommendations = self.recommendation_generator.generate(
            analytics=analytics,
            financial_state=financial_state,
            patterns=patterns,
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
            insights=insights,
            recommendations=recommendations,
        )


def get_analysis_service() -> AnalysisService:
    analytics_provider = AnalyticsProvider()
    state_predictor = StatePredictor()
    insight_generator = InsightGenerator()
    recommendation_generator = RecommendationGenerator()
    pattern_detector = PatternDetector()

    return AnalysisService(
        analytics_provider=analytics_provider,
        state_predictor=state_predictor,
        insight_generator=insight_generator,
        recommendation_generator=recommendation_generator,
        pattern_detector=pattern_detector,
    )