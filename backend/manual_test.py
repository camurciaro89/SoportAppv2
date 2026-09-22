import sys
import os
import json
from datetime import datetime
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from passlib.context import CryptContext

# Ajustar path para importar desde el directorio actual
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.database import Base
from app.models import models
from app.schemas import schemas

# --- CONFIGURACIÓN DE PRUEBA ---
SQLALCHEMY_DATABASE_URL = "sqlite:///./test_db.sqlite"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
pwd_context = CryptContext(schemes=["argon2"], deprecated="auto")

def run_test():
    print("\n🚀 INICIANDO PRUEBA INTEGRAL DE LÓGICA - SOPORTAPP\n" + "="*50)

    # 1. Preparar Base de Datos
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()

    try:
        # 2. Simular Registro de Usuario
        print("\n[1/5] Registrando nuevo cliente...")
        hashed_pass = pwd_context.hash("password123")
        user = models.User(
            nombre="Juan Pérez",
            email="juan@example.com",
            telefono="3001234567",
            contrasena=hashed_pass,
            user_type="CLIENTE"
        )
        db.add(user)
        db.commit()
        db.refresh(user)
        print(f"✅ Usuario creado: {user.nombre} (ID: {user.id})")

        # 3. Simular Registro de Equipo
        print("\n[2/5] Vinculando equipo al usuario...")
        equipment = models.Equipment(
            user_id=user.id,
            tipo="Portátil",
            marca="Lenovo",
            modelo="ThinkPad X1",
            serial_number="LNV-987654",
            operating_system="Windows 11"
        )
        db.add(equipment)
        db.commit()
        print(f"✅ Equipo registrado: {equipment.marca} {equipment.modelo} (S/N: {equipment.serial_number})")

        # 4. Simular Fase 1 de IA (Análisis Inicial)
        print("\n[3/5] Simulando Fase 1 de IA: Generación de preguntas...")
        problem_desc = "Mi laptop se calienta mucho y se apaga sola a los 10 minutos."
        # Mock de preguntas que generaría Ollama
        mock_questions = [
            "¿El ventilador hace ruidos extraños o parece estar bloqueado?",
            "¿Ocurre mientras usas programas pesados (juegos, edición) o siempre?",
            "¿Has notado si la base de la laptop está muy caliente al tacto?"
        ]
        print(f"   Problema reportado: \"{problem_desc}\"")
        print(f"   IA generó {len(mock_questions)} preguntas aclaratorias.")

        # 5. Simular Fase 2 de IA (Diagnóstico y Ticket)
        print("\n[4/5] Simulando Fase 2 de IA: Generación de veredicto y Ticket...")
        # Respuestas simuladas del usuario
        qa_history = [
            {"question": mock_questions[0], "answer": "Sí, el ventilador suena como una turbina."},
            {"question": mock_questions[1], "answer": "Ocurre siempre, incluso navegando en internet."},
            {"question": mock_questions[2], "answer": "Está hirviendo por debajo."}
        ]

        # Mock del diagnóstico JSON estructurado que devolvería Ollama
        mock_ai_json = {
            "diagnostico": "Falla térmica crítica. Probable obstrucción de ventilación o degradación de pasta térmica.",
            "causas": ["Ventilador obstruido", "Pasta térmica seca", "Sensor de calor fallido"],
            "prioridad": "Alta",
            "accion": "Limpieza interna preventiva inmediata. No encender el equipo.",
            "servicio": "Mantenimiento Técnico Avanzado"
        }

        # Crear el Ticket Real en BD
        db_ticket = models.SupportRequest(
            user_id=user.id,
            equipment_id=equipment.id,
            problem_description=problem_desc,
            modalidad="Sitio",
            prioridad=mock_ai_json["prioridad"],
            estado="NUEVO",
            ticket_number=f"ST-{datetime.now().strftime('%y%m%d%H%M')}"
        )
        db.add(db_ticket)
        db.commit()
        db.refresh(db_ticket)

        # Guardar Diagnóstico IA en Auditoría
        db_ai = models.AIDiagnosis(
            request_id=db_ticket.id,
            diagnosis_text=json.dumps(mock_ai_json, ensure_ascii=False),
            suggested_priority=mock_ai_json["prioridad"],
            model_name="llama3 (Mock)"
        )
        db.add(db_ai)
        db.commit()

        print(f"✅ Ticket #{db_ticket.ticket_number} creado exitosamente.")
        print(f"🔥 Prioridad asignada por IA: {db_ticket.prioridad}")

        # 6. Verificación Final
        print("\n[5/5] Verificando integridad de datos...")
        saved_ticket = db.query(models.SupportRequest).filter_by(id=db_ticket.id).first()
        saved_ai = db.query(models.AIDiagnosis).filter_by(request_id=db_ticket.id).first()

        if saved_ticket and saved_ai:
            print(f"✅ Integridad confirmada: Ticket vinculado a Usuario {saved_ticket.user_id} y Equipo {saved_ticket.equipment_id}")
            print(f"✅ Diagnóstico IA guardado en tabla de auditoría.")
            print("\n" + "="*50 + "\n🎯 PRUEBA FINALIZADA CON ÉXITO\n")
        else:
            print("❌ Error en la persistencia de datos.")

    finally:
        db.close()
        if os.path.exists("./test_db.sqlite"):
            os.remove("./test_db.sqlite")

if __name__ == "__main__":
    run_test()
