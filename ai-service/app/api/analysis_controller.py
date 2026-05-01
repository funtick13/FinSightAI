from uuid import uuid4

from fastapi import APIRouter

from app.schemas import AnalysisRequest, AnalysisResponse
from app.schemas.analysis.enums import AnalysisStatus, FinancialState


router = APIRouter(
    prefix="/analysis",
    tags=["Analysis"]
)


@router.post("", response_model=AnalysisResponse)
def analyze_finances(request: AnalysisRequest) -> AnalysisResponse:
    return AnalysisResponse(
        request_id=uuid4(),
        user_id=request.user_id,
        period=request.period,
        status=AnalysisStatus.SUCCESS,
        summary=None,
        financial_state=FinancialState.NO_DATA,
        category_analytics=[],
        top_categories=[],
        insights=[],
        recommendations=[],
    )