from decimal import Decimal

from app.core.models.analytics_result import AnalyticsResult
from app.core.models.detected_pattern import DetectedPattern
from app.schemas.analysis.enums import FinancialState
from app.schemas.pattern.enums import PatternType
from app.schemas.recommendation.enum import RecommendationType
from app.schemas.recommendation.recommendation_dto import RecommendationDto


class RecommendationGenerator:
    MAX_RECOMMENDATIONS = 5

    TARGET_REDUCTION_PERCENT = Decimal("10.00")
    SAVING_TARGET_PERCENT = Decimal("10.00")

    def generate(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
            patterns: list[DetectedPattern],
    ) -> list[RecommendationDto]:
        recommendations: list[RecommendationDto] = []

        self._add_pattern_recommendations(patterns, recommendations)
        self._add_state_recommendations(
            analytics=analytics,
            financial_state=financial_state,
            recommendations=recommendations,
        )
        self._add_general_recommendations(recommendations)

        return recommendations[:self.MAX_RECOMMENDATIONS]

    def _add_pattern_recommendations(
            self,
            patterns: list[DetectedPattern],
            recommendations: list[RecommendationDto],
    ) -> None:
        for pattern in patterns:
            if pattern.type == PatternType.EXPENSES_EXCEED_INCOME:
                self._add_expenses_exceed_income_recommendation(
                    pattern,
                    recommendations,
                )

            elif pattern.type == PatternType.HIGH_CONCENTRATION:
                self._add_high_concentration_recommendation(
                    pattern,
                    recommendations,
                )

            elif pattern.type == PatternType.TOP3_DOMINANCE:
                self._add_top3_dominance_recommendation(
                    pattern,
                    recommendations,
                )

            elif pattern.type == PatternType.LARGE_SINGLE_EXPENSE:
                self._add_large_expense_recommendation(
                    pattern,
                    recommendations,
                )

            elif pattern.type == PatternType.FREQUENT_SMALL_EXPENSES:
                self._add_frequent_small_expenses_recommendation(
                    pattern,
                    recommendations,
                )

            elif pattern.type == PatternType.NO_INCOME:
                self._add_no_income_recommendation(
                    pattern,
                    recommendations,
                )

    def _add_expenses_exceed_income_recommendation(
            self,
            pattern: DetectedPattern,
            recommendations: list[RecommendationDto],
    ) -> None:
        difference = pattern.data.get("difference")

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.BUDGET_CONTROL,
                text=(
                    f"Ваши расходы превышают доходы на {difference} ₽. "
                    "Рассмотрите возможность временно ограничить необязательные траты "
                    "или увеличить доходы, чтобы восстановить баланс."
                ),
                data=pattern.data,
            )
        )

    def _add_high_concentration_recommendation(
            self,
            pattern: DetectedPattern,
            recommendations: list[RecommendationDto],
    ) -> None:
        category = pattern.data.get("category")
        percent = pattern.data.get("percent")

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.EXPENSE_OPTIMIZATION,
                text=(
                    f"Категория «{category}» занимает {percent} % всех расходов. "
                    f"Попробуйте сократить траты в этой категории на "
                    f"{self.TARGET_REDUCTION_PERCENT} %, чтобы снизить нагрузку на бюджет."
                ),
                data={
                    **pattern.data,
                    "targetReductionPercent": str(self.TARGET_REDUCTION_PERCENT),
                },
            )
        )

    def _add_top3_dominance_recommendation(
            self,
            pattern: DetectedPattern,
            recommendations: list[RecommendationDto],
    ) -> None:
        percent = pattern.data.get("percent")
        categories = pattern.data.get("categories", [])

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.EXPENSE_OPTIMIZATION,
                text=(
                    f"Три основные категории занимают {percent} % всех расходов: "
                    f"{', '.join(categories)}. "
                    "Рекомендуется установить лимиты по этим направлениям, "
                    "чтобы снизить концентрацию трат."
                ),
                data=pattern.data,
            )
        )

    def _add_large_expense_recommendation(
            self,
            pattern: DetectedPattern,
            recommendations: list[RecommendationDto],
    ) -> None:
        amount = pattern.data.get("amount")
        percent = pattern.data.get("percent")
        category = pattern.data.get("category")

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.EXPENSE_OPTIMIZATION,
                text=(
                    f"Крупная трата в категории «{category}» составила {amount} ₽ "
                    f"или {percent} % всех расходов. "
                    "Рассмотрите возможность планировать такие покупки заранее "
                    "или распределять их на несколько периодов."
                ),
                data=pattern.data,
            )
        )

    def _add_frequent_small_expenses_recommendation(
            self,
            pattern: DetectedPattern,
            recommendations: list[RecommendationDto],
    ) -> None:
        small_count = pattern.data.get("smallCount")
        amount = pattern.data.get("amount")

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.EXPENSE_OPTIMIZATION,
                text=(
                    f"Вы совершили {small_count} небольших покупок на сумму {amount} ₽. "
                    "Попробуйте отслеживать микротраты: в сумме они могут заметно влиять "
                    "на бюджет."
                ),
                data=pattern.data,
            )
        )

    def _add_no_income_recommendation(
            self,
            pattern: DetectedPattern,
            recommendations: list[RecommendationDto],
    ) -> None:
        total_expense = pattern.data.get("totalExpense")

        recommendations.append(
            RecommendationDto(
                type=RecommendationType.WARNING,
                text=(
                    f"За текущий период доходов не зафиксировано, но расходы составили "
                    f"{total_expense} ₽. До появления новых поступлений рекомендуется "
                    "сократить необязательные траты."
                ),
                data=pattern.data,
            )
        )

    def _add_state_recommendations(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
            recommendations: list[RecommendationDto],
    ) -> None:
        if financial_state == FinancialState.LOW_SAVINGS:
            recommendations.append(
                RecommendationDto(
                    type=RecommendationType.SAVING,
                    text=(
                        f"Свободный остаток за период составляет {analytics.balance} ₽. "
                        f"Постарайтесь увеличить накопления, откладывая хотя бы "
                        f"{self.SAVING_TARGET_PERCENT} % дохода."
                    ),
                    data={
                        "balance": str(analytics.balance),
                        "savePercent": str(self.SAVING_TARGET_PERCENT),
                        "totalIncome": str(analytics.total_income),
                    },
                )
            )

        elif financial_state == FinancialState.STABLE:
            recommendations.append(
                RecommendationDto(
                    type=RecommendationType.SAVING,
                    text=(
                        f"Финансовое состояние выглядит стабильным. "
                        f"Попробуйте регулярно направлять около "
                        f"{self.SAVING_TARGET_PERCENT} % дохода в резервный фонд."
                    ),
                    data={
                        "savePercent": str(self.SAVING_TARGET_PERCENT),
                        "totalIncome": str(analytics.total_income),
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