from fastapi import APIRouter, Depends, HTTPException, status, Body, Request
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from ..database import get_db
from ..models import models
from ..schemas import schemas
from .security import (
    create_access_token,
    create_refresh_token,
    create_password_reset_token,
    verify_password_reset_token
)
from passlib.context import CryptContext
from jose import jwt, JWTError
from .security import ALGORITHM, SECRET_KEY
from slowapi import Limiter
from slowapi.util import get_remote_address

router = APIRouter(
    prefix="/auth",
    tags=["auth"]
)

limiter = Limiter(key_func=get_remote_address)

# Configuración de hashing usando Argon2id
pwd_context = CryptContext(schemes=["argon2"], deprecated="auto")

@router.post("/login", response_model=schemas.Token)
@limiter.limit("5/minute")
def login(
    request: Request,
    db: Session = Depends(get_db),
    form_data: OAuth2PasswordRequestForm = Depends()
):
    user = db.query(models.User).filter(models.User.email == form_data.username).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales incorrectas"
        )

    if not pwd_context.verify(form_data.password, user.contrasena):
         raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales incorrectas"
        )

    return {
        "access_token": create_access_token(subject=user.id),
        "refresh_token": create_refresh_token(subject=user.id),
        "token_type": "bearer",
        "user_type": user.user_type
    }

@router.post("/refresh", response_model=schemas.Token)
@limiter.limit("10/minute")
def refresh_token(
    request: Request,
    refresh_token: str = Body(...),
    db: Session = Depends(get_db)
):
    try:
        payload = jwt.decode(refresh_token, SECRET_KEY, algorithms=[ALGORITHM])
        if payload.get("type") != "refresh":
            raise HTTPException(status_code=400, detail="Token inválido")
        user_id = payload.get("sub")
    except JWTError:
        raise HTTPException(status_code=401, detail="Token expirado o inválido")

    user = db.query(models.User).filter(models.User.id == int(user_id)).first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    return {
        "access_token": create_access_token(subject=user.id),
        "refresh_token": create_refresh_token(subject=user.id),
        "token_type": "bearer",
        "user_type": user.user_type
    }

@router.post("/register", response_model=schemas.UserResponse)
@limiter.limit("5/minute")
def register(request: Request, user_data: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == user_data.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="El correo ya existe")

    hashed_password = pwd_context.hash(user_data.contrasena)
    new_user = models.User(
        email=user_data.email,
        nombre=user_data.nombre,
        telefono=user_data.telefono,
        user_type=user_data.user_type,
        contrasena=hashed_password
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

@router.post("/forgot-password")
@limiter.limit("3/minute")
def forgot_password(request: Request, data: schemas.PasswordResetRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == request.email).first()
    if not user:
        # Por seguridad no revelamos si el correo existe o no
        return {"message": "Si el correo está registrado, recibirás un enlace de recuperación."}

    token = create_password_reset_token(email=user.email)

    # SIMULACIÓN DE ENVÍO DE CORREO
    print(f"\n--- [EMAIL SIMULATION] ---")
    print(f"To: {user.email}")
    print(f"Subject: Recuperación de Contraseña - SoportApp")
    print(f"Link: http://localhost:8000/auth/reset-password?token={token}")
    print(f"--------------------------\n")

    return {"message": "Si el correo está registrado, recibirás un enlace de recuperación."}

@router.post("/reset-password")
@limiter.limit("3/minute")
def reset_password(request: Request, data: schemas.PasswordResetConfirm, db: Session = Depends(get_db)):
    email = verify_password_reset_token(data.token)
    if not email:
        raise HTTPException(status_code=400, detail="Token inválido o expirado")

    user = db.query(models.User).filter(models.User.email == email).first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    user.contrasena = pwd_context.hash(data.new_password)
    db.commit()
    return {"message": "Contraseña actualizada exitosamente"}
