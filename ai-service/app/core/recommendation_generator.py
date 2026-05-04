from decimal import Decimal, ROUND_HALF_UP

from app.core.models.analytics_result import AnalyticsResult
from app.schemas.analysis.enums import FinancialState
from app.schemas.recommendation.enum import RecommendationType
from app.schemas.recommendation.recommendation_dto import RecommendationDto


class RecommendationGenerator:
    MIN_RECOMMENDATIONS = 2
    MAX_RECOMMENDATIONS = 5

    TOP_CATEGORY_TARGET_REDUCTION = Decimal("10.00")
    TOP_CATEGORY_GOAL_PERCENT = Decimal("30.00")
    SAVING_TARGET_PERCENT = Decimal("10.00")

    def generate(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
    ) -> list[RecommendationDto]:
        recommendations: list[RecommendationDto] = []

        self._add_risky_recommendations(analytics, financial_state, recommendations)
        self._add_top_category_recommendation(analytics, recommendations)
        self._add_saving_recommendation(analytics, financial_state, recommendations)
        self._add_no_income_recommendation(analytics, recommendations)
        self._add_general_recommendations(recommendations)

        return recommendations[:self.MAX_RECOMMENDATIONS]

    def _add_risky_recommendations(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
            recommendations: list[RecommendationDto],
    ) -> None:
        if financial_state != FinancialState.RISKY:
            return

        if analytics.total_income > 0 and analytics.balance < 0:
            diff_percent = (
                    abs(analytics.balance) / analytics.total_income * Decimal("100")
            ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            recommendations.append(
                RecommendationDto(
                    type=RecommendationType.BUDGET_CONTROL,
                    text=(
                        f"Ваши расходы превышают доходы на {diff_percent} %. "
                        "Рассмотрите возможность временно ограничить необязательные траты, "
                        "чтобы восстановить баланс."
                    ),
                    data={
                        "diffPercent": str(diff_percent),
                        "balance": str(analytics.balance),
                    },
                )
            )

    def _add_top_category_recommendation(
            self,
            analytics: AnalyticsResult,
            recommendations: list[RecommendationDto],
    ) -> None:
        if not analytics.top_categories:
            return

        top_category = analytics.top_categories[0]

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.EXPENSE_OPTIMIZATION,
                text=(
                    f"Категория «{top_category.category}» занимает "
                    f"{top_category.percent} % всех расходов. "
                    f"Попробуйте сократить траты в этой категории на "
                    f"{self.TOP_CATEGORY_TARGET_REDUCTION} %, чтобы снизить её долю "
                    f"до более комфортного уровня."
                ),
                data={
                    "category": top_category.category,
                    "currentPercent": str(top_category.percent),
                    "targetReductionPercent": str(self.TOP_CATEGORY_TARGET_REDUCTION),
                    "goalPercent": str(self.TOP_CATEGORY_GOAL_PERCENT),
                    "amount": str(top_category.amount),
                },
            )
        )

    def _add_saving_recommendation(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
            recommendations: list[RecommendationDto],
    ) -> None:
        if financial_state not in {
            FinancialState.LOW_SAVINGS,
            FinancialState.STABLE,
            FinancialState.RISKY,
        }:
            return

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.SAVING,
                text=(
                    f"Старайтесь откладывать хотя бы {self.SAVING_TARGET_PERCENT} % "
                    "дохода на резервный фонд. Это поможет справляться с "
                    "непредвиденными расходами."
                ),
                data={
                    "savePercent": str(self.SAVING_TARGET_PERCENT),
                    "totalIncome": str(analytics.total_income),
                    "balance": str(analytics.balance),
                },
            )
        )

    def _add_no_income_recommendation(
            self,
            analytics: AnalyticsResult,
            recommendations: list[RecommendationDto],
    ) -> None:
        if analytics.total_income > 0:
            return

        if analytics.total_expense <= 0:
            return

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.WARNING,
                text=(
                    f"За текущий период доходов не зафиксировано, но расходы составили "
                    f"{analytics.total_expense} ₽. При отсутствии поступлений "
                    "рекомендуется сократить необязательные траты."
                ),
                data={
                    "totalExpense": str(analytics.total_expense),
                },
            )
        )

    def _add_general_recommendations(
            self,
            recommendations: list[RecommendationDto],
    ) -> None:
        recommendations.append(
            RecommendationDto(
                type=RecommendationType.GENERAL_ADVICE,
                text=(
                    "Следите за всеми операциями: регулярно анализируйте доходы и расходы, "
                    "чтобы видеть полную картину бюджета."
                ),
                data=None,
            )
        )

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.GENERAL_ADVICE,
                text=(
                    "Периодически пересматривайте подписки и регулярные платежи — "
                    "возможно, часть расходов можно оптимизировать."
                ),
                data=None,
            )
        )