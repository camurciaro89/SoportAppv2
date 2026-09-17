from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from ..database import get_db
from ..models import models
from ..schemas import schemas

router = APIRouter(
    prefix="/equipments",
    tags=["equipments"]
)

@router.post("/", response_model=schemas.EquipmentResponse)
def create_equipment(equipment: schemas.EquipmentCreate, user_id: int, db: Session = Depends(get_db)):
    # En el futuro user_id se obtendrá del token JWT
    db_equipment = models.Equipment(**equipment.model_dump(), user_id=user_id)
    db.add(db_equipment)
    db.commit()
    db.refresh(db_equipment)
    return db_equipment

@router.get("/", response_model=List[schemas.EquipmentResponse])
def get_user_equipments(user_id: int, db: Session = Depends(get_db)):
    return db.query(models.Equipment).filter(models.Equipment.user_id == user_id).all()

@router.delete("/{equipment_id}")
def delete_equipment(equipment_id: int, db: Session = Depends(get_db)):
    db_equipment = db.query(models.Equipment).filter(models.Equipment.id == equipment_id).first()
    if not db_equipment:
        raise HTTPException(status_code=404, detail="Equipo no encontrado")
    db.delete(db_equipment)
    db.commit()
    return {"status": "success"}
