from decimal import Decimal, ROUND_HALF_UP

from app.core.models.analytics_result import AnalyticsResult
from app.core.models.detected_pattern import DetectedPattern
from app.schemas.analysis.enums import FinancialState

from app.schemas.insight.enums import InsightType
from app.schemas.insight.insight_dto import InsightDto
from app.schemas.pattern.enums import PatternType


class InsightGenerator:
    MAX_INSIGHTS = 5

    def generate(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
            patterns: list[DetectedPattern],
    ) -> list[InsightDto]:
        if analytics.transaction_count == 0:
            return [
                InsightDto(
                    type=InsightType.GENERAL,
                    text="Недостаточно данных для анализа финансового поведения.",
                    data=None,
                )
            ]

        insights: list[InsightDto] = []

        self._add_top_category_insight(analytics, insights)
        self._add_pattern_insights(patterns, insights)
        self._add_state_explanation(financial_state, insights)

        return insights[:self.MAX_INSIGHTS]

    def _add_top_category_insight(
            self,
            analytics: AnalyticsResult,
            insights: list[InsightDto],
    ) -> None:
        if not analytics.top_categories:
            return

        top_category = analytics.top_categories[0]

        insights.append(
            InsightDto(
                type=InsightType.CATEGORY_STRUCTURE,
                text=(
                    f"На категорию «{top_category.category}» приходится "
                    f"{top_category.percent} % ваших расходов "
                    f"(≈ {top_category.amount} ₽). Это ваша крупнейшая статья затрат."
                ),
                data={
                    "category": top_category.category,
                    "percent": str(top_category.percent),
                    "amount": str(top_category.amount),
                },
            )
        )

    def _add_pattern_insights(
            self,
            patterns: list[DetectedPattern],
            insights: list[InsightDto],
    ) -> None:
        for pattern in patterns:
            if pattern.type == PatternType.EXPENSES_EXCEED_INCOME:
                self._add_expenses_exceed_income_insight(pattern, insights)

            elif pattern.type == PatternType.HIGH_CONCENTRATION:
                self._add_high_concentration_insight(pattern, insights)

            elif pattern.type == PatternType.TOP3_DOMINANCE:
                self._add_top3_dominance_insight(pattern, insights)

            elif pattern.type == PatternType.LARGE_SINGLE_EXPENSE:
                self._add_large_expense_insight(pattern, insights)

            elif pattern.type == PatternType.FREQUENT_SMALL_EXPENSES:
                self._add_frequent_small_expenses_insight(pattern, insights)

            elif pattern.type == PatternType.NO_INCOME:
                self._add_no_income_insight(pattern, insights)

    def _add_expenses_exceed_income_insight(
            self,
            pattern: DetectedPattern,
            insights: list[InsightDto],
    ) -> None:
        difference = pattern.data.get("difference")

        insights.append(
            InsightDto(
                type=InsightType.INCOME_EXPENSE_RATIO,
                text=(
                    f"Ваши расходы превышают доходы на {difference} ₽, "
                    "что приводит к отрицательному балансу."
                ),
                data=pattern.data,
            )
        )

    def _add_high_concentration_insight(
            self,
            pattern: DetectedPattern,
            insights: list[InsightDto],
    ) -> None:
        category = pattern.data.get("category")
        percent = pattern.data.get("percent")

        insights.append(
            InsightDto(
                type=InsightType.HIGH_CONCENTRATION,
                text=(
                    f"Категория «{category}» занимает {percent} % всех расходов, "
                    "что превышает рекомендованный порог."
                ),
                data=pattern.data,
            )
        )

    def _add_top3_dominance_insight(
            self,
            pattern: DetectedPattern,
            insights: list[InsightDto],
    ) -> None:
        percent = pattern.data.get("percent")
        categories = pattern.data.get("categories", [])

        insights.append(
            InsightDto(
                type=InsightType.HIGH_CONCENTRATION,
                text=(
                    f"Три основные категории составляют {percent} % всех расходов: "
                    f"{', '.join(categories)}."
                ),
                data=pattern.data,
            )
        )

    def _add_large_expense_insight(
            self,
            pattern: DetectedPattern,
            insights: list[InsightDto],
    ) -> None:
        amount = pattern.data.get("amount")
        percent = pattern.data.get("percent")
        category = pattern.data.get("category")

        insights.append(
            InsightDto(
                type=InsightType.LARGE_EXPENSE,
                text=(
                    f"У вас была крупная трата в категории «{category}» "
                    f"на {amount} ₽ — это {percent} % от всех расходов."
                ),
                data=pattern.data,
            )
        )

    def _add_frequent_small_expenses_insight(
            self,
            pattern: DetectedPattern,
            insights: list[InsightDto],
    ) -> None:
        small_count = pattern.data.get("smallCount")
        amount = pattern.data.get("amount")

        insights.append(
            InsightDto(
                type=InsightType.SMALL_EXPENSES,
                text=(
                    f"Вы совершили {small_count} небольших покупок. "
                    f"В сумме они составили {amount} ₽."
                ),
                data=pattern.data,
            )
        )

    def _add_no_income_insight(
            self,
            pattern: DetectedPattern,
            insights: list[InsightDto],
    ) -> None:
        insights.append(
            InsightDto(
                type=InsightType.NO_INCOME,
                text=(
                    "За анализируемый период доходов не зафиксировано. "
                    "При активных расходах это может привести к дефициту бюджета."
                ),
                data=pattern.data,
            )
        )

    def _add_state_explanation(
            self,
            financial_state: FinancialState,
            insights: list[InsightDto],
    ) -> None:
        if financial_state == FinancialState.RISKY:
            insights.append(
                InsightDto(
                    type=InsightType.INCOME_EXPENSE_RATIO,
                    text=(
                        "Финансовое состояние выглядит рискованным: "
                        "расходы превышают доходы."
                    ),
                    data={"financialState": financial_state.value},
                )
            )

        elif financial_state == FinancialState.LOW_SAVINGS:
            insights.append(
                InsightDto(
                    type=InsightType.INCOME_EXPENSE_RATIO,
                    text=(
                        "Доходы превышают расходы, но свободный остаток небольшой. "
                        "Это снижает потенциал накоплений."
                    ),
                    data={"financialState": financial_state.value},
                )
            )