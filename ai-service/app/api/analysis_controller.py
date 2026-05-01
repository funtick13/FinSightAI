from uuid import uuid4

from fastapi import APIRouter, Depends

from app.schemas import AnalysisRequest, AnalysisResponse
from app.schemas.analysis.enums import AnalysisStatus, FinancialState
from app.service.analysis_service import AnalysisService, get_analysis_service

router = APIRouter(
    prefix="/analysis",
    tags=["Analysis"]
)


@router.post("", response_model=AnalysisResponse)
def analyze_finances(
        request: AnalysisRequest,
        service: AnalysisService = Depends(get_analysis_service)
) -> AnalysisResponse:
    return service.analyze(request)
