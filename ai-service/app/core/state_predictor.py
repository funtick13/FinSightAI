from decimal import Decimal

from app.core.models.analytics_result import AnalyticsResult
from app.schemas.analysis.enums import FinancialState


class StatePredictor:
    TOP_CATEGORY_THRESHOLD = Decimal("35.00")
    BALANCE_THRESHOLD = Decimal("0.10")
    RISKY_RATIO_THRESHOLD = Decimal("0.80")

    def predict(self, analytics: AnalyticsResult) -> FinancialState:
        if analytics.transaction_count == 0:
            return FinancialState.NO_DATA

        ratio = self._calculate_income_expense_ratio(
            total_income=analytics.total_income,
            total_expense=analytics.total_expense,
        )

        max_category_percent = self._get_max_category_percent(analytics)

        if ratio < self.RISKY_RATIO_THRESHOLD:
            return FinancialState.RISKY

        if max_category_percent >= self.TOP_CATEGORY_THRESHOLD:
            return FinancialState.EXPENSE_HEAVY

        if self._has_low_savings(analytics):
            return FinancialState.LOW_SAVINGS

        return FinancialState.STABLE

    @staticmethod
    def _calculate_income_expense_ratio(
            total_income: Decimal,
            total_expense: Decimal,
    ) -> Decimal:
        if total_expense <= 0:
            return Decimal("999999")

        return total_income / total_expense

    @staticmethod
    def _get_max_category_percent(analytics: AnalyticsResult) -> Decimal:
        if not analytics.category_analytics:
            return Decimal("0.00")

        return max(
            category.percent
            for category in analytics.category_analytics
        )

    def _has_low_savings(self, analytics: AnalyticsResult) -> bool:
        if analytics.total_income <= 0:
            return False

        if analytics.balance <= 0:
            return False

        savings_rate = analytics.balance / analytics.total_income

        return savings_rate < self.BALANCE_THRESHOLD