# FinSight AI Service

AI-сервис системы **FinSight AI** для анализа финансовых операций, выявления финансовых паттернов, определения финансового состояния пользователя, генерации инсайтов и персонализированных рекомендаций.

---

## Возможности

Сервис выполняет:

* анализ входящих и исходящих операций;
* расчёт финансовой сводки (`summary`);
* анализ расходов по категориям (`categoryAnalytics`);
* определение топ-3 категорий расходов (`topCategories`);
* обнаружение финансовых паттернов (`PatternDetector`);
* определение финансового состояния (`StatePredictor`);
* генерацию аналитических выводов (`InsightGenerator`);
* генерацию персональных рекомендаций (`RecommendationGenerator`);
* обработку ошибок и возврат статусных кодов анализа.

---

## Архитектура

```text
Client / Backend
      ↓
POST /analysis
      ↓
AnalysisController
      ↓
AnalysisService
      ↓
AnalyticsProvider
      ↓
PatternDetector
      ↓
StatePredictor
      ↓
InsightGenerator
      ↓
RecommendationGenerator
      ↓
AnalysisResponse
```

---

## Технологии

* Python 3.13+
* FastAPI
* Pydantic v2
* Uvicorn
* Docker
* OpenAPI / Swagger

---

## Структура проекта

```text
ai-service/
│
├── app/
│   ├── api/
│   │   ├── analysis_controller.py
│   │   └── health_controller.py
│   │
│   ├── core/
│   │   ├── analytics_provider.py
│   │   ├── pattern_detector.py
│   │   ├── state_predictor.py
│   │   ├── insight_generator.py
│   │   ├── recommendation_generator.py
│   │   └── models/
│   │
│   ├── schemas/
│   │   ├── analysis/
│   │   ├── insight/
│   │   ├── recommendation/
│   │   └── common/
│   │
│   ├── config/
│   │   ├── settings.py
│   │   └── logging_config.py
│   │
│   └── main.py
│
├── docs/
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
└── README.md
```

---

## Установка зависимостей

Создание виртуального окружения:

```bash
python -m venv .venv
```

Активация:

Windows:

```bash
.venv\Scripts\activate
```

Linux / MacOS:

```bash
source .venv/bin/activate
```

Установка зависимостей:

```bash
pip install -r requirements.txt
```

---

## Запуск локально

Запуск сервиса:

```bash
uvicorn app.main:app --reload
```

Сервис будет доступен:

```text
http://127.0.0.1:8000
```

Swagger UI:

```text
http://127.0.0.1:8000/docs
```

ReDoc:

```text
http://127.0.0.1:8000/redoc
```

---

## Docker запуск

Сборка:

```bash
docker compose build
```

Запуск:

```bash
docker compose up
```

Запуск в фоне:

```bash
docker compose up -d
```

Остановка:

```bash
docker compose down
```

---

## API

### Проверка состояния сервиса

```http
GET /health
```

Ответ:

```json
{
  "status": "OK"
}
```

---

### Анализ финансов

```http
POST /analysis
```

Request:

```json
{
  "requestId": "uuid",
  "userId": "uuid",
  "period": "2026-03/2026-04",
  "bank": "SBERBANK",
  "statementIds": ["uuid"],
  "statementPeriod": {},
  "statementSummary": {},
  "transactions": []
}
```

Response:

```json
{
  "requestId": "uuid",
  "userId": "uuid",
  "period": "2026-03/2026-04",
  "status": "SUCCESS",
  "summary": {},
  "financialState": "STABLE",
  "categoryAnalytics": [],
  "topCategories": [],
  "insights": [],
  "recommendations": [],
  "message": null
}
```

---

## Статусы анализа

Возможные статусы:

* `SUCCESS`
* `NO_DATA`
* `INVALID_INPUT`
* `ANALYSIS_ERROR`
* `INTERNAL_ERROR`

---

## Логирование

Сервис логирует:

* requestId;
* userId;
* период анализа;
* количество транзакций;
* найденные паттерны;
* финансовое состояние;
* количество инсайтов;
* количество рекомендаций;
* ошибки анализа.

---

## Интеграция с backend

Сервис проектировался как отдельный микросервис для интеграции с backend FinSight AI.

Схема:

```text
Flutter → Backend → AI-service → Backend → Flutter
```

Backend формирует `AnalysisRequest`, AI-service возвращает `AnalysisResponse`.

---

## Roadmap развития

Следующие этапы:

* unit tests;
* integration tests;
* Redis cache;
* PostgreSQL storage;
* ML-модель предсказания финансового состояния;
* LLM-инсайты;
* персонализация рекомендаций;
* аналитика по временным рядам.

---

