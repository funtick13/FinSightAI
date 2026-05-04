from decimal import Decimal, ROUND_HALF_UP

from app.core.models.analytics_result import AnalyticsResult
from app.schemas.insight.insight_dto import InsightDto
from app.schemas.analysis.enums import FinancialState
from app.schemas.insight.enums import InsightType


class InsightGenerator:
    MAX_INSIGHTS = 5

    def generate(
            self,
            analytics: AnalyticsResult,
            financial_state: FinancialState,
    ) -> list[InsightDto]:
        if analytics.transaction_count == 0:
            return [
                InsightDto(
                    type=InsightType.NO_INCOME,
                    text="Недостаточно данных для анализа финансового поведения.",
                    data=None,
                )
            ]

        insights: list[InsightDto] = []

        self._add_top_category_insight(analytics, insights)
        self._add_top_three_categories_insight(analytics, insights)
        self._add_income_expense_insight(analytics, insights)
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

    def _add_top_three_categories_insight(
            self,
            analytics: AnalyticsResult,
            insights: list[InsightDto],
    ) -> None:
        if len(analytics.top_categories) < 3:
            return

        percent_top3 = sum(
            category.percent for category in analytics.top_categories
        ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

        category_names = [
            category.category for category in analytics.top_categories
        ]

        insights.append(
            InsightDto(
                type=InsightType.HIGH_CONCENTRATION,
                text=(
                    f"Три основные категории составляют {percent_top3} % "
                    f"всех расходов: {', '.join(category_names)}."
                ),
                data={
                    "percentTop3": str(percent_top3),
                    "categories": category_names,
                },
            )
        )

    def _add_income_expense_insight(
            self,
            analytics: AnalyticsResult,
            insights: list[InsightDto],
    ) -> None:
        if analytics.total_income <= 0:
            insights.append(
                InsightDto(
                    type=InsightType.NO_INCOME,
                    text=(
                        "За анализируемый период доходов не зафиксировано. "
                        "При активных расходах это может привести к дефициту бюджета."
                    ),
                    data=None,
                )
            )
            return

        if analytics.balance < 0:
            diff_percent = (
                    abs(analytics.balance) / analytics.total_income * Decimal("100")
            ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            insights.append(
                InsightDto(
                    type=InsightType.INCOME_EXPENSE_RATIO,
                    text=(
                        f"Ваши расходы превышают доходы на {diff_percent} %, "
                        f"что приводит к отрицательному балансу."
                    ),
                    data={
                        "diffPercent": str(diff_percent),
                        "balance": str(analytics.balance),
                    },
                )
            )
            return

        diff_percent = (
                analytics.balance / analytics.total_income * Decimal("100")
        ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

        insights.append(
            InsightDto(
                type=InsightType.INCOME_EXPENSE_RATIO,
                text=(
                    f"Ваши доходы превышают расходы на {diff_percent} % — "
                    f"вы сохраняете положительный баланс."
                ),
                data={
                    "diffPercent": str(diff_percent),
                    "balance": str(analytics.balance),
                },
            )
        )

    def _add_state_explanation(
            self,
            financial_state: FinancialState,
            insights: list[InsightDto],
    ) -> None:
        if financial_state == FinancialState.EXPENSE_HEAVY:
            insights.append(
                InsightDto(
                    type=InsightType.HIGH_CONCENTRATION,
                    text=(
                        "В расходах заметна высокая концентрация: "
                        "значительная часть бюджета приходится на одну или несколько категорий."
                    ),
                    data={"financialState": financial_state.value},
                )
            )

        if financial_state == FinancialState.LOW_SAVINGS:
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

        if financial_state == FinancialState.RISKY:
            insights.append(
                InsightDto(
                    type=InsightType.INCOME_EXPENSE_RATIO,
                    text=(
                        "Финансовое состояние выглядит рискованным: "
                        "расходы заметно превышают доходы."
                    ),
                    data={"financialState": financial_state.value},
                )
            )