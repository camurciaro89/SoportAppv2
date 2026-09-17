from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from .database import engine, Base, get_db
from .models import models
from .services.ai_service import ai_service
from .api import auth, equipments, tickets
from fastapi.middleware.cors import CORSMiddleware

# Crear tablas en la base de datos (En desarrollo)
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="SoportApp Backend",
    description="API para gestión de soporte técnico e IA local (Ollama)",
    version="1.0.0"
)

# Configuración de CORS para Flutter
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # En producción, limitar a los dominios de la App/Web
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def read_root():
    return {"status": "SoportApp API Running", "ia_engine": "Ollama (Llama 3)"}

# Registro de rutas
app.include_router(auth.router)
app.include_router(equipments.router)
app.include_router(tickets.router)

# Aquí se agregarían los routers para auth, tickets, etc.
# app.include_router(auth.router)
# app.include_router(tickets.router)
