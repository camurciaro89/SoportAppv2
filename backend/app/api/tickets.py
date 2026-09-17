from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List
from ..database import get_db
from ..models import models
from ..schemas import schemas
from ..services.ai_service import ai_service
from datetime import datetime

router = APIRouter(
    prefix="/tickets",
    tags=["tickets"]
)

@router.post("/", response_model=schemas.SupportRequestResponse)
async def create_ticket(ticket: schemas.SupportRequestCreate, user_id: int, db: Session = Depends(get_db)):
    # 1. Obtener diagnóstico de IA (Ollama)
    diagnosis_text = await ai_service.generate_diagnosis(ticket.problem_description)

    # Extracción simple de prioridad
    priority = "Media"
    if "Alta" in diagnosis_text: priority = "Alta"
    if "Baja" in diagnosis_text: priority = "Baja"

    # 2. Crear ticket
    db_ticket = models.SupportRequest(
        **ticket.model_dump(),
        user_id=user_id,
        prioridad=priority,
        estado="NUEVO",
        ticket_number=f"ST-{datetime.now().strftime('%H%M%S')}"
    )
    db.add(db_ticket)
    db.commit()
    db.refresh(db_ticket)

    # 3. Guardar diagnóstico en tabla de auditoría
    db_ai = models.AIDiagnosis(
        request_id=db_ticket.id,
        diagnosis_text=diagnosis_text,
        suggested_priority=priority,
        model_name="llama3"
    )
    db.add(db_ai)
    db.commit()

    return db_ticket

@router.get("/", response_model=List[schemas.SupportRequestResponse])
def get_tickets(user_id: int, role: str, db: Session = Depends(get_db)):
    if role == "ADMIN":
        return db.query(models.SupportRequest).all()
    elif role == "TECNICO":
        return db.query(models.SupportRequest).filter(models.SupportRequest.technician_id == user_id).all()
    else:
        return db.query(models.SupportRequest).filter(models.SupportRequest.user_id == user_id).all()

@router.patch("/{ticket_id}", response_model=schemas.SupportRequestResponse)
def update_ticket(ticket_id: int, update_data: schemas.SupportRequestUpdate, db: Session = Depends(get_db)):
    db_ticket = db.query(models.SupportRequest).filter(models.SupportRequest.id == ticket_id).first()
    if not db_ticket:
        raise HTTPException(status_code=404, detail="Ticket no encontrado")

    update_dict = update_data.model_dump(exclude_unset=True)
    for key, value in update_dict.items():
        setattr(db_ticket, key, value)

    if update_data.estado == "Finalizado":
        db_ticket.completed_at = datetime.now()

    db.commit()
    db.refresh(db_ticket)
    return db_ticket
