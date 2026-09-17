from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime, Float, Enum, Text, CheckConstraint
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
    telefono = Column(String, nullable=False)
    contrasena = Column(String, nullable=False) # Hash Argon2id
    is_active = Column(Boolean, default=True)
    user_type = Column(String, default="CLIENTE")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    # Relaciones con borrado en cascada
    roles = relationship("UserRole", back_populates="user", cascade="all, delete-orphan")
    equipments = relationship("Equipment", back_populates="owner", cascade="all, delete-orphan")
    client_requests = relationship("SupportRequest", back_populates="client", foreign_keys="SupportRequest.user_id", cascade="all, delete-orphan")
    tech_assignments = relationship("SupportRequest", back_populates="technician", foreign_keys="SupportRequest.technician_id")
    notifications = relationship("Notification", back_populates="user", cascade="all, delete-orphan")

class Role(Base):
    __tablename__ = "roles"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, nullable=False) # ADMIN, TECNICO, CLIENTE

    users = relationship("UserRole", back_populates="role")

class UserRole(Base):
    __tablename__ = "usuarios_roles"
    user_id = Column(Integer, ForeignKey("usuarios.id", ondelete="CASCADE"), primary_key=True)
    role_id = Column(Integer, ForeignKey("roles.id", ondelete="CASCADE"), primary_key=True)

    user = relationship("User", back_populates="roles")
    role = relationship("Role", back_populates="users")

class Technician(Base):
    __tablename__ = "tecnicos"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("usuarios.id", ondelete="CASCADE"), unique=True)
    specialty = Column(String, nullable=False)
    experience_years = Column(Integer, default=0)
    is_verified = Column(Boolean, default=False)
    average_rating = Column(Float, default=0.0)
    total_services = Column(Integer, default=0)

    user = relationship("User", backref="technician_profile")
    # Relación N:N con servicios que puede atender
    services = relationship("TechnicianService", back_populates="technician", cascade="all, delete-orphan")

class TechnicianService(Base):
    __tablename__ = "tecnicos_servicios"
    technician_id = Column(Integer, ForeignKey("tecnicos.id", ondelete="CASCADE"), primary_key=True)
    service_id = Column(Integer, ForeignKey("servicios.id", ondelete="CASCADE"), primary_key=True)

    technician = relationship("Technician", back_populates="services")
    service = relationship("Service", back_populates="technicians")

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
    category_id = Column(Integer, ForeignKey("categorias_problema.id", ondelete="SET NULL"), nullable=True)

    requests = relationship("SupportRequest", back_populates="service")
    technicians = relationship("TechnicianService", back_populates="service")

class Equipment(Base):
    __tablename__ = "equipos"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("usuarios.id", ondelete="CASCADE"), nullable=False, index=True)
    tipo = Column(String, nullable=False) # Portátil, Impresora, etc.
    marca = Column(String, nullable=False)
    modelo = Column(String, nullable=False)
    serial_number = Column(String, unique=True, index=True, nullable=False)
    operating_system = Column(String)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    owner = relationship("User", back_populates="equipments")
    requests = relationship("SupportRequest", back_populates="equipment")
    maintenances = relationship("Maintenance", back_populates="equipment", cascade="all, delete-orphan")

class SupportRequest(Base):
    __tablename__ = "solicitudes_soporte"

    id = Column(Integer, primary_key=True, index=True)
    ticket_number = Column(String, unique=True, index=True, nullable=False) # ST-000125
    user_id = Column(Integer, ForeignKey("usuarios.id", ondelete="CASCADE"), nullable=False, index=True)
    technician_id = Column(Integer, ForeignKey("usuarios.id", ondelete="SET NULL"), nullable=True, index=True)
    equipment_id = Column(Integer, ForeignKey("equipos.id", ondelete="SET NULL"), nullable=True, index=True)
    service_id = Column(Integer, ForeignKey("servicios.id", ondelete="SET NULL"), nullable=True)

    problem_description = Column(Text, nullable=False)
    modalidad = Column(String, nullable=False) # Remoto, Sitio, Taller
    estado = Column(String, default="NUEVO", nullable=False, index=True)
    prioridad = Column(String, default="Media", nullable=False)

    # Resultados Técnicos
    technical_solution = Column(Text)
    parts_used = Column(Text)

    created_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    completed_at = Column(DateTime(timezone=True), nullable=True)

    # Restricciones de Dominio
    __table_args__ = (
        CheckConstraint("prioridad IN ('Baja', 'Media', 'Alta')", name="check_prioridad"),
    )

    # Relaciones
    client = relationship("User", foreign_keys=[user_id], back_populates="client_requests")
    technician = relationship("User", foreign_keys=[technician_id], back_populates="tech_assignments")
    equipment = relationship("Equipment", back_populates="requests")
    service = relationship("Service", back_populates="requests")
    ai_diagnoses = relationship("AIDiagnosis", back_populates="request", cascade="all, delete-orphan")
    history = relationship("TicketHistory", back_populates="request", cascade="all, delete-orphan")
    rating = relationship("Rating", back_populates="request", uselist=False, cascade="all, delete-orphan")

class AIDiagnosis(Base):
    __tablename__ = "diagnosticos_ia"
    id = Column(Integer, primary_key=True, index=True)
    request_id = Column(Integer, ForeignKey("solicitudes_soporte.id", ondelete="CASCADE"), nullable=False, index=True)
    diagnosis_text = Column(Text, nullable=False)
    suggested_priority = Column(String)
    model_name = Column(String) # Llama3, Mistral, etc.
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    request = relationship("SupportRequest", back_populates="ai_diagnoses")

class TicketHistory(Base):
    __tablename__ = "historial_tickets"
    id = Column(Integer, primary_key=True, index=True)
    request_id = Column(Integer, ForeignKey("solicitudes_soporte.id", ondelete="CASCADE"), nullable=False, index=True)
    previous_status = Column(String)
    new_status = Column(String, nullable=False)
    changed_by_id = Column(Integer, ForeignKey("usuarios.id", ondelete="SET NULL"), nullable=True)
    notes = Column(Text)
    changed_at = Column(DateTime(timezone=True), server_default=func.now())

    request = relationship("SupportRequest", back_populates="history")

class Rating(Base):
    __tablename__ = "calificaciones"
    id = Column(Integer, primary_key=True, index=True)
    request_id = Column(Integer, ForeignKey("solicitudes_soporte.id", ondelete="CASCADE"), unique=True, nullable=False)
    tech_rating = Column(Integer, nullable=False) # 1-5
    service_rating = Column(Integer, nullable=False) # 1-5
    support_rating = Column(Integer, nullable=False) # 1-5
    general_score = Column(Float)
    comment = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    __table_args__ = (
        CheckConstraint("tech_rating BETWEEN 1 AND 5", name="check_tech_rating"),
        CheckConstraint("service_rating BETWEEN 1 AND 5", name="check_service_rating"),
        CheckConstraint("support_rating BETWEEN 1 AND 5", name="check_support_rating"),
    )

    request = relationship("SupportRequest", back_populates="rating")

class Maintenance(Base):
    __tablename__ = "mantenimientos"
    id = Column(Integer, primary_key=True, index=True)
    equipment_id = Column(Integer, ForeignKey("equipos.id", ondelete="CASCADE"), nullable=False)
    last_maintenance_at = Column(DateTime(timezone=True))
    next_maintenance_at = Column(DateTime(timezone=True), nullable=False)
    status = Column(String, default="PROGRAMADO") # PROGRAMADO, REALIZADO

    equipment = relationship("Equipment", back_populates="maintenances")

class Notification(Base):
    __tablename__ = "notificaciones"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("usuarios.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String, nullable=False)
    message = Column(Text, nullable=False)
    is_read = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="notifications")
