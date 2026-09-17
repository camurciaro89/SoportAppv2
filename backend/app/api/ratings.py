from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ..database import get_db
from ..models import models
from ..schemas import schemas

router = APIRouter(
    prefix="/ratings",
    tags=["ratings"]
)

@router.post("/", response_model=schemas.RatingResponse)
def create_rating(rating: schemas.RatingCreate, ticket_id: int, db: Session = Depends(get_db)):
    # Verificar si el ticket existe
    db_ticket = db.query(models.SupportRequest).filter(models.SupportRequest.id == ticket_id).first()
    if not db_ticket:
        raise HTTPException(status_code=404, detail="Ticket no encontrado")

    # Calcular promedio
    avg = (rating.tech_rating + rating.service_rating + rating.support_rating) / 3.0

    db_rating = models.Rating(
        **rating.model_dump(),
        request_id=ticket_id,
        general_score=avg
    )
    db.add(db_rating)

    # Cerrar ticket definitivamente
    db_ticket.estado = "CERRADO"

    db.commit()
    db.refresh(db_rating)
    return db_rating
