from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from ..database import get_db
from ..models import models
from ..schemas import schemas
from .deps import get_current_user

router = APIRouter(
    prefix="/equipments",
    tags=["equipments"]
)

@router.post("/", response_model=schemas.EquipmentResponse)
def create_equipment(
    equipment: schemas.EquipmentCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    # El user_id se extrae automáticamente del token
    db_equipment = models.Equipment(**equipment.model_dump(exclude={"user_id"}), user_id=current_user.id)
    db.add(db_equipment)
    db.commit()
    db.refresh(db_equipment)
    return db_equipment

@router.get("/", response_model=List[schemas.EquipmentResponse])
def get_user_equipments(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return db.query(models.Equipment).filter(models.Equipment.user_id == current_user.id).all()

@router.put("/{equipment_id}", response_model=schemas.EquipmentResponse)
def update_equipment(
    equipment_id: int,
    equipment_update: schemas.EquipmentBase,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_equipment = db.query(models.Equipment).filter(models.Equipment.id == equipment_id).first()
    if not db_equipment:
        raise HTTPException(status_code=404, detail="Equipo no encontrado")

    if db_equipment.user_id != current_user.id and current_user.user_type != "ADMIN":
        raise HTTPException(status_code=403, detail="No tienes acceso a este equipo")

    update_data = equipment_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_equipment, key, value)

    db.commit()
    db.refresh(db_equipment)
    return db_equipment

@router.delete("/{equipment_id}")
def delete_equipment(
    equipment_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_equipment = db.query(models.Equipment).filter(models.Equipment.id == equipment_id).first()
    if not db_equipment:
        raise HTTPException(status_code=404, detail="Equipo no encontrado")

    if db_equipment.user_id != current_user.id and current_user.user_type != "ADMIN":
        raise HTTPException(status_code=403, detail="No tienes acceso a este equipo")

    db.delete(db_equipment)
    db.commit()
    return {"status": "success"}
