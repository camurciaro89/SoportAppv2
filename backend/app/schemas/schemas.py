from pydantic import BaseModel, EmailStr, field_validator
from typing import Optional, List
from datetime import datetime
import bleach

# --- USUARIOS ---

class UserBase(BaseModel):
    email: EmailStr
    nombre: str
    telefono: str
    user_type: str = "CLIENTE"

class UserCreate(UserBase):
    contrasena: str

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime
    technician_profile: Optional[TechnicianResponse] = None

    class Config:
        from_attributes = True

class LoginRequest(BaseModel):
    email: EmailStr
    contrasena: str

class Token(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str
    user_type: str

class TokenPayload(BaseModel):
    sub: Optional[int] = None

class PasswordResetRequest(BaseModel):
    email: EmailStr

class PasswordResetConfirm(BaseModel):
    token: str
    new_password: str

# --- TÉCNICOS ---

class TechnicianBase(BaseModel):
    specialty: str
    experience_years: int
    is_verified: bool = False

class TechnicianResponse(TechnicianBase):
    id: int
    average_rating: float
    total_services: int

    class Config:
        from_attributes = True

# --- EQUIPOS ---

class EquipmentBase(BaseModel):
    tipo: str
    marca: str
    modelo: str
    serial_number: str
    operating_system: Optional[str] = None

class EquipmentCreate(EquipmentBase):
    user_id: Optional[int] = None # Se puede inferir del token

class EquipmentResponse(EquipmentBase):
    id: int
    user_id: int
    created_at: datetime

    class Config:
        from_attributes = True

# --- TICKETS ---

class SupportRequestBase(BaseModel):
    equipment_id: Optional[int] = None
    service_id: Optional[int] = None
    problem_description: str
    modalidad: str

    @field_validator('problem_description')
    @classmethod
    def sanitize_description(cls, v: str) -> str:
        return bleach.clean(v, tags=[], strip=True)

class SupportRequestCreate(SupportRequestBase):
    pass

class SupportRequestUpdate(BaseModel):
    estado: Optional[str] = None
    technician_id: Optional[int] = None
    technical_solution: Optional[str] = None
    parts_used: Optional[str] = None

class SupportRequestResponse(SupportRequestBase):
    id: int
    ticket_number: Optional[str] = None
    user_id: int
    technician_id: Optional[int] = None
    estado: str
    prioridad: str
    created_at: datetime
    completed_at: Optional[datetime] = None

    class Config:
        from_attributes = True

# --- DIAGNÓSTICO IA ---

class DiagnosticQuestionRequest(BaseModel):
    problem_description: str

class DiagnosticAnswer(BaseModel):
    question: str
    answer: str

class FinalDiagnosisRequest(BaseModel):
    problem_description: str
    answers: List[DiagnosticAnswer]
    equipment_id: Optional[int] = None
    modalidad: str = "Sitio"

class AIDiagnosisResponse(BaseModel):
    id: int
    request_id: int
    diagnosis_text: str
    suggested_priority: str
    created_at: datetime

    class Config:
        from_attributes = True

# --- CALIFICACIONES ---

class RatingCreate(BaseModel):
    tech_rating: int
    service_rating: int
    support_rating: int
    comment: Optional[str] = None

    @field_validator('comment')
    @classmethod
    def sanitize_comment(cls, v: Optional[str]) -> Optional[str]:
        if v:
            return bleach.clean(v, tags=[], strip=True)
        return v

class RatingResponse(RatingCreate):
    id: int
    request_id: int
    general_score: float
    created_at: datetime

    class Config:
        from_attributes = True
