from decimal import Decimal, ROUND_HALF_UP

from app.core.models.analytics_result import AnalyticsResult
from app.core.models.detected_pattern import DetectedPattern
from app.schemas.analysis import AnalysisRequest
from app.schemas.pattern.enums import PatternType


class PatternDetector:
    HIGH_CATEGORY_THRESHOLD = Decimal("35.00")
    TOP3_DOMINANCE_THRESHOLD = Decimal("70.00")
    LARGE_SINGLE_EXPENSE_THRESHOLD = Decimal("25.00")
    SMALL_EXPENSE_THRESHOLD = Decimal("1.00")
    SMALL_EXPENSE_COUNT_THRESHOLD = 10

    def detect(
            self,
            analytics: AnalyticsResult,
            request: AnalysisRequest,
    ) -> list[DetectedPattern]:
        patterns: list[DetectedPattern] = []

        if analytics.transaction_count == 0:
            return patterns

        self._detect_expenses_exceed_income(analytics, patterns)
        self._detect_no_income(analytics, patterns)
        self._detect_high_concentration(analytics, patterns)
        self._detect_top3_dominance(analytics, patterns)
        self._detect_large_single_expense(analytics, request, patterns)
        self._detect_frequent_small_expenses(analytics, request, patterns)

        return patterns

    def _detect_expenses_exceed_income(
            self,
            analytics: AnalyticsResult,
            patterns: list[DetectedPattern],
    ) -> None:
        if analytics.total_expense <= analytics.total_income:
            return

        difference = analytics.total_expense - analytics.total_income

        patterns.append(
            DetectedPattern(
                type=PatternType.EXPENSES_EXCEED_INCOME,
                data={
                    "difference": str(self._round_money(difference)),
                    "totalIncome": str(analytics.total_income),
                    "totalExpense": str(analytics.total_expense),
                },
            )
        )

    def _detect_no_income(
            self,
            analytics: AnalyticsResult,
            patterns: list[DetectedPattern],
    ) -> None:
        if analytics.total_income != 0:
            return

        if analytics.total_expense <= 0:
            return

        patterns.append(
            DetectedPattern(
                type=PatternType.NO_INCOME,
                data={
                    "totalExpense": str(analytics.total_expense),
                },
            )
        )

    def _detect_high_concentration(
            self,
            analytics: AnalyticsResult,
            patterns: list[DetectedPattern],
    ) -> None:
        if not analytics.top_categories:
            return

        top_category = analytics.top_categories[0]

        if top_category.percent < self.HIGH_CATEGORY_THRESHOLD:
            return

        patterns.append(
            DetectedPattern(
                type=PatternType.HIGH_CONCENTRATION,
                data={
                    "category": top_category.category,
                    "percent": str(top_category.percent),
                    "amount": str(top_category.amount),
                },
            )
        )

    def _detect_top3_dominance(
            self,
            analytics: AnalyticsResult,
            patterns: list[DetectedPattern],
    ) -> None:
        if len(analytics.top_categories) < 3:
            return

        percent_top3 = sum(
            category.percent for category in analytics.top_categories
        ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

        if percent_top3 < self.TOP3_DOMINANCE_THRESHOLD:
            return

        patterns.append(
            DetectedPattern(
                type=PatternType.TOP3_DOMINANCE,
                data={
                    "percent": str(percent_top3),
                    "categories": [
                        category.category for category in analytics.top_categories
                    ],
                },
            )
        )

    def _detect_large_single_expense(
            self,
            analytics: AnalyticsResult,
            request: AnalysisRequest,
            patterns: list[DetectedPattern],
    ) -> None:
        if analytics.total_expense <= 0:
            return

        for transaction in request.transactions:
            if transaction.type != "EXPENSE":
                continue

            percent = (
                    transaction.amount / analytics.total_expense * Decimal("100")
            ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            if percent < self.LARGE_SINGLE_EXPENSE_THRESHOLD:
                continue

            patterns.append(
                DetectedPattern(
                    type=PatternType.LARGE_SINGLE_EXPENSE,
                    data={
                        "transactionId": str(transaction.id),
                        "amount": str(transaction.amount),
                        "percent": str(percent),
                        "category": transaction.category,
                        "description": transaction.description,
                    },
                )
            )

    def _detect_frequent_small_expenses(
            self,
            analytics: AnalyticsResult,
            request: AnalysisRequest,
            patterns: list[DetectedPattern],
    ) -> None:
        if analytics.total_expense <= 0:
            return

        small_count = 0
        small_total = Decimal("0")

        for transaction in request.transactions:
            if transaction.type != "EXPENSE":
                continue

            percent = (
                    transaction.amount / analytics.total_expense * Decimal("100")
            ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            if percent < self.SMALL_EXPENSE_THRESHOLD:
                small_count += 1
                small_total += transaction.amount

        if small_count <= self.SMALL_EXPENSE_COUNT_THRESHOLD:
            return

        patterns.append(
            DetectedPattern(
                type=PatternType.FREQUENT_SMALL_EXPENSES,
                data={
                    "smallCount": small_count,
                    "amount": str(self._round_money(small_total)),
                },
            )
        )

    @staticmethod
    def _round_money(value: Decimal) -> Decimal:
        return value.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)