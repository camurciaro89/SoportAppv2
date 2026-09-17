from fastapi import FastAPI, Depends, HTTPException, Request, Response
from sqlalchemy.orm import Session
from .database import engine, Base, get_db
from .models import models
from .services.ai_service import ai_service
from .api import auth, equipments, tickets, users, ratings
from fastapi.middleware.cors import CORSMiddleware
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
import time
import logging

# Configuración de Rate Limiting
limiter = Limiter(key_func=get_remote_address)
app = FastAPI(
    title="SoportApp Backend",
    description="API para gestión de soporte técnico e IA local (Ollama)",
    version="1.0.0"
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Logger seguro
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Configuración de CORS
ALLOWED_ORIGINS = ["*"] # En producción, limitar a dominios específicos

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Middleware para Cabeceras de Seguridad y Manejo de Errores Global
@app.middleware("http")
async def add_security_headers_and_handle_errors(request: Request, call_next):
    try:
        response: Response = await call_next(request)
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
        return response
    except Exception as e:
        logger.error(f"Error inesperado: {str(e)}", exc_info=True)
        return Response(
            content='{"detail": "Ha ocurrido un error interno en el servidor. Por favor, intenta más tarde."}',
            status_code=500,
            media_type="application/json"
        )

@app.get("/")
@limiter.limit("100/minute")
def read_root(request: Request):
    return {"status": "SoportApp API Running", "ia_engine": "Ollama (Llama 3)"}

# Registro de rutas
app.include_router(auth.router)
app.include_router(users.router)
app.include_router(equipments.router)
app.include_router(tickets.router)
app.include_router(ratings.router)

# Aquí se agregarían los routers para auth, tickets, etc.
# app.include_router(auth.router)
# app.include_router(tickets.router)
