from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime, Float, Enum, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from ..database import Base
import enum

# --- ENUMS ---

class UserRoleEnum(str, enum.Enum):
    ADMIN = "ADMIN"
    TECNICO = "TECNICO"
    CLIENTE = "CLIENTE"

class TicketStatusEnum(str, enum.Enum):
    NUEVO = "NUEVO"
    ASIGNADO = "ASIGNADO"
    EN_PROCESO = "EN_PROCESO"
    PENDIENTE_REPUESTO = "PENDIENTE_REPUESTO"
    FINALIZADO = "FINALIZADO"
    CERRADO = "CERRADO"

# --- MODELOS ---

class User(Base):
    __tablename__ = "usuarios"

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    telefono = Column(String)
    contrasena = Column(String, nullable=False) # Hash Bcrypt
    is_active = Column(Boolean, default=True)
    user_type = Column(String, default="CLIENTE") # Para compatibilidad rápida, pero usaremos roles
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    # Relaciones
    roles = relationship("UserRole", back_populates="user")
    equipments = relationship("Equipment", back_populates="owner")
    client_requests = relationship("SupportRequest", back_populates="client", foreign_keys="SupportRequest.user_id")
    tech_assignments = relationship("SupportRequest", back_populates="technician", foreign_keys="SupportRequest.technician_id")
    notifications = relationship("Notification", back_populates="user")

class Role(Base):
    __tablename__ = "roles"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, nullable=False) # ADMIN, TECNICO, CLIENTE

    users = relationship("UserRole", back_populates="role")

class UserRole(Base):
    __tablename__ = "usuarios_roles"
    user_id = Column(Integer, ForeignKey("usuarios.id"), primary_key=True)
    role_id = Column(Integer, ForeignKey("roles.id"), primary_key=True)

    user = relationship("User", back_populates="roles")
    role = relationship("Role", back_populates="users")

class Technician(Base):
    __tablename__ = "tecnicos"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("usuarios.id"), unique=True)
    specialty = Column(String)
    experience_years = Column(Integer, default=0)
    is_verified = Column(Boolean, default=False)
    average_rating = Column(Float, default=0.0)
    total_services = Column(Integer, default=0)

    user = relationship("User", backref="technician_profile")

class TicketStatus(Base):
    __tablename__ = "estados_ticket"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, nullable=False) # NUEVO, ASIGNADO, EN_PROCESO, etc.
    description = Column(Text)

class ProblemCategory(Base):
    __tablename__ = "categorias_problema"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, nullable=False) # Hardware, Software, Redes, etc.
    description = Column(Text)

class Service(Base):
    __tablename__ = "servicios"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    base_price = Column(Float, default=0.0)
    category_id = Column(Integer, ForeignKey("categorias_problema.id"))

    requests = relationship("SupportRequest", back_populates="service")

class Equipment(Base):
    __tablename__ = "equipos"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("usuarios.id"))
    tipo = Column(String, nullable=False) # Portátil, Impresora, etc.
    marca = Column(String)
    modelo = Column(String)
    serial_number = Column(String, unique=True, index=True)
    operating_system = Column(String)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    owner = relationship("User", back_populates="equipments")
    requests = relationship("SupportRequest", back_populates="equipment")
    maintenances = relationship("Maintenance", back_populates="equipment")

class SupportRequest(Base):
    __tablename__ = "solicitudes_soporte"

    id = Column(Integer, primary_key=True, index=True)
    ticket_number = Column(String, unique=True, index=True) # ST-000125
    user_id = Column(Integer, ForeignKey("usuarios.id"), nullable=False)
    technician_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)
    equipment_id = Column(Integer, ForeignKey("equipos.id"), nullable=True)
    service_id = Column(Integer, ForeignKey("servicios.id"), nullable=True)

    problem_description = Column(Text, nullable=False)
    modalidad = Column(String) # Remoto, Sitio, Taller
    estado = Column(String, default="NUEVO")
    prioridad = Column(String, default="Media")

    # Resultados Técnicos
    technical_solution = Column(Text)
    parts_used = Column(Text)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    completed_at = Column(DateTime(timezone=True), nullable=True)

    # Relaciones
    client = relationship("User", foreign_keys=[user_id], back_populates="client_requests")
    technician = relationship("User", foreign_keys=[technician_id], back_populates="tech_assignments")
    equipment = relationship("Equipment", back_populates="requests")
    service = relationship("Service", back_populates="requests")
    ai_diagnoses = relationship("AIDiagnosis", back_populates="request")
    history = relationship("TicketHistory", back_populates="request")
    rating = relationship("Rating", back_populates="request", uselist=False)

class AIDiagnosis(Base):
    __tablename__ = "diagnosticos_ia"
    id = Column(Integer, primary_key=True, index=True)
    request_id = Column(Integer, ForeignKey("solicitudes_soporte.id"))
    diagnosis_text = Column(Text, nullable=False)
    suggested_priority = Column(String)
    model_name = Column(String) # Llama3, Mistral, etc.
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    request = relationship("SupportRequest", back_populates="ai_diagnoses")

class TicketHistory(Base):
    __tablename__ = "historial_tickets"
    id = Column(Integer, primary_key=True, index=True)
    request_id = Column(Integer, ForeignKey("solicitudes_soporte.id"))
    previous_status = Column(String)
    new_status = Column(String)
    changed_by_id = Column(Integer, ForeignKey("usuarios.id"))
    notes = Column(Text)
    changed_at = Column(DateTime(timezone=True), server_default=func.now())

    request = relationship("SupportRequest", back_populates="history")

class Rating(Base):
    __tablename__ = "calificaciones"
    id = Column(Integer, primary_key=True, index=True)
    request_id = Column(Integer, ForeignKey("solicitudes_soporte.id"), unique=True)
    tech_rating = Column(Integer) # 1-5
    service_rating = Column(Integer) # 1-5
    support_rating = Column(Integer) # 1-5
    general_score = Column(Float)
    comment = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    request = relationship("SupportRequest", back_populates="rating")

class Maintenance(Base):
    __tablename__ = "mantenimientos"
    id = Column(Integer, primary_key=True, index=True)
    equipment_id = Column(Integer, ForeignKey("equipos.id"))
    last_maintenance_at = Column(DateTime(timezone=True))
    next_maintenance_at = Column(DateTime(timezone=True))
    status = Column(String) # PROGRAMADO, REALIZADO

    equipment = relationship("Equipment", back_populates="maintenances")

class Notification(Base):
    __tablename__ = "notificaciones"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("usuarios.id"))
    title = Column(String)
    message = Column(Text)
    is_read = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="notifications")
