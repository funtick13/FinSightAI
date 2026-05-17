from decimal import Decimal, ROUND_HALF_UP

from app.core.models.analytics_result import AnalyticsResult
from app.core.models.category_aggregate import CategoryAggregate
from app.schemas.analysis import AnalysisRequest, CategoryAnalyticsDto

class AnalyticsProvider:
    def calculate(self, request: AnalysisRequest) -> AnalyticsResult:
        total_income = request.statement_summary.total_income
        total_expense = request.statement_summary.total_expense
        balance = total_income - total_expense

        categories: dict[str, CategoryAggregate] = {}

        for transaction in request.transactions:
            if transaction.type != "EXPENSE":
                continue

            category_name = transaction.category or "Прочее"

            if category_name not in categories:
                categories[category_name] = CategoryAggregate()

            categories[category_name].amount += transaction.amount
            categories[category_name].count += 1

        category_analytics: list[CategoryAnalyticsDto] = []

        for category_name, aggregate in categories.items():
            percent = self._calculate_percent(
                amount=aggregate.amount,
                total_expense=total_expense,
            )

            category_analytics.append(
                CategoryAnalyticsDto(
                    category=category_name,
                    amount=self._round_money(aggregate.amount),
                    percent=percent,
                    operations_count=aggregate.count,
                )
            )

        category_analytics.sort(
            key=lambda item: item.amount,
            reverse=True,
        )

        return AnalyticsResult(
            total_income=self._round_money(total_income),
            total_expense=self._round_money(total_expense),
            balance=self._round_money(balance),
            transaction_count=len(request.transactions),
            category_analytics=category_analytics,
            top_categories=category_analytics[:3],
        )

    @staticmethod
    def _calculate_percent(amount: Decimal, total_expense: Decimal) -> Decimal:
        if total_expense <= 0:
            return Decimal("0.00")

        return (
                amount / total_expense * Decimal("100")
        ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

    @staticmethod
    def _round_money(value: Decimal) -> Decimal:
        return value.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)