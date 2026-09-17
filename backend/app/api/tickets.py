from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List
from ..database import get_db
from ..models import models
from ..schemas import schemas
from ..services.ai_service import ai_service
from .deps import get_current_user, RoleChecker
from datetime import datetime

router = APIRouter(
    prefix="/tickets",
    tags=["tickets"]
)

@router.post("/", response_model=schemas.SupportRequestResponse)
async def create_ticket(
    ticket: schemas.SupportRequestCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(RoleChecker(["CLIENTE", "ADMIN"]))
):
    # 1. Obtener diagnóstico de IA (Ollama)
    diagnosis_text = await ai_service.generate_diagnosis(ticket.problem_description)

    # Extracción simple de prioridad
    priority = "Media"
    if "Alta" in diagnosis_text: priority = "Alta"
    if "Baja" in diagnosis_text: priority = "Baja"

    # 2. Crear ticket
    db_ticket = models.SupportRequest(
        **ticket.model_dump(),
        user_id=current_user.id,
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
def get_tickets(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if current_user.user_type == "ADMIN":
        return db.query(models.SupportRequest).all()
    elif current_user.user_type == "TECNICO":
        return db.query(models.SupportRequest).filter(models.SupportRequest.technician_id == current_user.id).all()
    else:
        return db.query(models.SupportRequest).filter(models.SupportRequest.user_id == current_user.id).all()

@router.get("/{ticket_id}", response_model=schemas.SupportRequestResponse)
def get_ticket_detail(
    ticket_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_ticket = db.query(models.SupportRequest).filter(models.SupportRequest.id == ticket_id).first()
    if not db_ticket:
        raise HTTPException(status_code=404, detail="Ticket no encontrado")

    # Validar propiedad o rol
    if current_user.user_type == "CLIENTE" and db_ticket.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes acceso a este ticket")

    return db_ticket

@router.post("/{ticket_id}/diagnosis", response_model=schemas.AIDiagnosisResponse)
async def redo_diagnosis(
    ticket_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(RoleChecker(["TECNICO", "ADMIN"]))
):
    db_ticket = db.query(models.SupportRequest).filter(models.SupportRequest.id == ticket_id).first()
    if not db_ticket:
        raise HTTPException(status_code=404, detail="Ticket no encontrado")

    diagnosis_text = await ai_service.generate_diagnosis(db_ticket.problem_description)

    db_ai = models.AIDiagnosis(
        request_id=db_ticket.id,
        diagnosis_text=diagnosis_text,
        suggested_priority="Media", # Simplificado
        model_name="llama3"
    )
    db.add(db_ai)
    db.commit()
    db.refresh(db_ai)
    return db_ai

@router.post("/{ticket_id}/assign")
def assign_technician(
    ticket_id: int,
    technician_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(RoleChecker(["ADMIN"]))
):
    db_ticket = db.query(models.SupportRequest).filter(models.SupportRequest.id == ticket_id).first()
    if not db_ticket:
        raise HTTPException(status_code=404, detail="Ticket no encontrado")

    db_ticket.technician_id = technician_id
    db_ticket.estado = "ASIGNADO"

    # Registrar en historial
    history = models.TicketHistory(
        request_id=ticket_id,
        previous_status="NUEVO",
        new_status="ASIGNADO",
        notes=f"Asignado al técnico ID {technician_id} por {current_user.nombre}"
    )
    db.add(history)
    db.commit()
    return {"status": "success", "message": f"Ticket asignado al técnico {technician_id}"}

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
