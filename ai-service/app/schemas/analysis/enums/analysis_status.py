from enum import Enum


class AnalysisStatus(str, Enum):
    SUCCESS = "SUCCESS"
    NO_DATA = "NO_DATA"
    INVALID_INPUT = "INVALID_INPUT"
    ANALYSIS_ERROR = "ANALYSIS_ERROR"
    INTERNAL_ERROR = "INTERNAL_ERROR"