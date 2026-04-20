from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any

app = FastAPI(title="FinSight AI Service", version="0.1.0")


class Operation(BaseModel):
    """Модель одной финансовой операции, извлечённой из выписки.

    Для MVP каждая операция состоит из даты, суммы, описания и
    категории.  Эту схему можно расширять по мере усложнения логики
    парсинга.
    """
    date: str
    amount: float
    description: str
    category: str


class AnalyzeRequest(BaseModel):
    """Модель запроса для эндпоинта `/analyze`.

    Серверная часть будет передавать список операций вместе с
    контекстной информацией (пользователь, месяц, банк и т. п.).
    Пока требуется только список операций; остальные поля можно
    добавить позже.
    """
    operations: List[Operation]


class Insight(BaseModel):
    title: str
    description: str


class AnalyzeResponse(BaseModel):
    """Модель ответа для эндпоинта `/analyze`.

    В рамках MVP AI реализован на базе правил и возвращает простые
    инсайты и рекомендации.  Заглушку в функции `analyze_operations`
    нужно заменить на реальную логику для подсчёта итогов, определения
    топ‑категорий и персональных советов.
    """
    income_total: float
    expense_total: float
    category_totals: Dict[str, float]
    insights: List[Insight]


@app.get("/health")
async def health() -> Dict[str, str]:
    """Эндпоинт для проверки работоспособности.

    Используется сервером или инструментами мониторинга для того, чтобы
    убедиться, что AI‑сервис запущен и готов принимать запросы.
    """
    return {"status": "ok"}


def analyze_operations(ops: List[Operation]) -> AnalyzeResponse:
    """Заглушка для анализа операций.

    Функция выполняет очень простую агрегацию списка операций:
    подсчитывает суммы доходов и расходов и группирует суммы по
    категориям.  Также возвращаются несколько примерных инсайтов.
    В будущем здесь должна быть реализована логика на основе правил,
    а позднее — использованы модели машинного обучения.
    """
    income_total = 0.0
    expense_total = 0.0
    category_totals: Dict[str, float] = {}

    for op in ops:
        # Положительные суммы считаем доходом, отрицательные — расходом.
        if op.amount >= 0:
            income_total += op.amount
        else:
            expense_total += abs(op.amount)

        category_totals[op.category] = category_totals.get(op.category, 0.0) + abs(op.amount)

    insights = [
        Insight(
            title="Top spending category",
            description="Your highest spending was in category: "
            + max(category_totals, key=category_totals.get)
            if category_totals else "No data provided",
        ),
        Insight(
            title="Total income vs expenses",
            description=f"You earned {income_total:.2f} and spent {expense_total:.2f} in the period."
        ),
    ]

    return AnalyzeResponse(
        income_total=income_total,
        expense_total=expense_total,
        category_totals=category_totals,
        insights=insights,
    )


@app.post("/analyze", response_model=AnalyzeResponse)
async def analyze(request: AnalyzeRequest) -> AnalyzeResponse:
    """Анализирует список операций и возвращает агрегированные результаты.

    Сейчас этот эндпоинт реализует упрощённый анализ на основе правил.  Если
    список операций пуст, возвращается ошибка HTTP 400.  Расширьте эту
    функцию, чтобы она реализовывала реальную интеллектуальную обработку,
    описанную в техническом задании: группирование операций по
    категориям, формирование инсайтов и персональных рекомендаций.
    """
    if not request.operations:
        raise HTTPException(status_code=400, detail="No operations provided.")

    return analyze_operations(request.operations)