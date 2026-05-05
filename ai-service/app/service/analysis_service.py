from app.core.analytics_provider import AnalyticsProvider
from app.core.insight_generator import InsightGenerator
from app.core.pattern_detector import PatternDetector
from app.core.recommendation_generator import RecommendationGenerator
from app.core.state_predictor import StatePredictor
from app.schemas.analysis import AnalysisRequest, AnalysisResponse, SummaryDto
from app.schemas.analysis.enums import AnalysisStatus, FinancialState
import logging

logger = logging.getLogger(__name__)

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
        logger.info(
            "Start analysis: requestId=%s userId=%s period=%s transactions=%s",
            request.request_id,
            request.user_id,
            request.period,
            len(request.transactions),
        )

        if not request.transactions:
            return self._no_data_response(request)

        try:
            analytics = self.analytics_provider.calculate(request)

            if analytics.transaction_count == 0:
                return self._no_data_response(request)

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

            logger.info(
                "Analysis completed: requestId=%s status=%s financialState=%s patterns=%s insights=%s recommendations=%s",
                request.request_id,
                AnalysisStatus.SUCCESS,
                financial_state,
                len(patterns),
                len(insights),
                len(recommendations),
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
                message=None,
            )

        except ValueError as error:
            return self._analysis_error_response(
                request=request,
                message=str(error),
            )

        except Exception:
            logger.exception(
                "Analysis failed: requestId=%s userId=%s",
                request.request_id,
                request.user_id,
            )
            return self._internal_error_response(request)



    def _no_data_response(self, request: AnalysisRequest) -> AnalysisResponse:
        return AnalysisResponse(
            request_id=request.request_id,
            user_id=request.user_id,
            period=request.period,
            status=AnalysisStatus.NO_DATA,
            summary=None,
            financial_state=FinancialState.NO_DATA,
            category_analytics=[],
            top_categories=[],
            insights=[],
            recommendations=[],
            message="Нет данных для анализа.",
        )

    def _analysis_error_response(
            self,
            request: AnalysisRequest,
            message: str,
    ) -> AnalysisResponse:
        return AnalysisResponse(
            request_id=request.request_id,
            user_id=request.user_id,
            period=request.period,
            status=AnalysisStatus.ANALYSIS_ERROR,
            summary=None,
            financial_state=None,
            category_analytics=[],
            top_categories=[],
            insights=[],
            recommendations=[],
            message=message,
        )

    def _internal_error_response(self, request: AnalysisRequest) -> AnalysisResponse:
        return AnalysisResponse(
            request_id=request.request_id,
            user_id=request.user_id,
            period=request.period,
            status=AnalysisStatus.INTERNAL_ERROR,
            summary=None,
            financial_state=None,
            category_analytics=[],
            top_categories=[],
            insights=[],
            recommendations=[],
            message="Внутренняя ошибка AI-сервиса.",
        )

def get_analysis_service() -> AnalysisService:
    return AnalysisService(
        analytics_provider=AnalyticsProvider(),
        state_predictor=StatePredictor(),
        insight_generator=InsightGenerator(),
        recommendation_generator=RecommendationGenerator(),
        pattern_detector=PatternDetector(),
    )