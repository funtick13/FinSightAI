from enum import Enum


class FinancialState(str, Enum):
    STABLE = "STABLE"
    LOW_SAVINGS = "LOW_SAVINGS"
    EXPENSE_HEAVY = "EXPENSE_HEAVY"
    RISKY = "RISKY"
    NO_DATA = "NO_DATA"