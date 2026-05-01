from decimal import Decimal

from app.core.models.analytics_result import AnalyticsResult
from app.core.models.category_aggregate import CategoryAggregate
from app.schemas.analysis import AnalysisRequest, CategoryAnalyticsDto
from app.schemas.analysis.transaction_dto import TransactionType


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
            percent = Decimal("0")

            if total_expense > 0:
                percent = (aggregate.amount / total_expense) * Decimal("100")

            category_analytics.append(
                CategoryAnalyticsDto(
                    category=category_name,
                    amount=aggregate.amount,
                    percent=percent,
                    operations_count=aggregate.count,
                )
            )

        category_analytics.sort(
            key=lambda item: item.amount,
            reverse=True,
        )

        top_categories = category_analytics[:3]

        return AnalyticsResult(
            total_income=total_income,
            total_expense=total_expense,
            balance=balance,
            transaction_count=len(request.transactions),
            category_analytics=category_analytics,
            top_categories=top_categories,
        )