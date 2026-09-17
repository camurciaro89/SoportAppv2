"""baseline

Revision ID: 001_baseline
Revises:
Create Date: 2024-09-17 10:45:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '001_baseline'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # --- Usuarios ---
    op.create_table('usuarios',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('nombre', sa.String(), nullable=False),
        sa.Column('email', sa.String(), nullable=False),
        sa.Column('telefono', sa.String(), nullable=False),
        sa.Column('contrasena', sa.String(), nullable=False),
        sa.Column('is_active', sa.Boolean(), nullable=True),
        sa.Column('user_type', sa.String(), nullable=True),
        sa.Column('salt', sa.String(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_usuarios_email'), 'usuarios', ['email'], unique=True)
    op.create_index(op.f('ix_usuarios_id'), 'usuarios', ['id'], unique=False)

    # --- Roles ---
    op.create_table('roles',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(), nullable=False),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('name')
    )
    op.create_index(op.f('ix_roles_id'), 'roles', ['id'], unique=False)

    op.create_table('usuarios_roles',
        sa.Column('user_id', sa.Integer(), nullable=False),
        sa.Column('role_id', sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(['role_id'], ['roles.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['user_id'], ['usuarios.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('user_id', 'role_id')
    )

    # --- Tecnicos ---
    op.create_table('tecnicos',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), nullable=True),
        sa.Column('specialty', sa.String(), nullable=False),
        sa.Column('experience_years', sa.Integer(), nullable=True),
        sa.Column('is_verified', sa.Boolean(), nullable=True),
        sa.Column('average_rating', sa.Float(), nullable=True),
        sa.Column('total_services', sa.Integer(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['usuarios.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('user_id')
    )
    op.create_index(op.f('ix_tecnicos_id'), 'tecnicos', ['id'], unique=False)

    # --- Catálogo ---
    op.create_table('categorias_problema',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('name')
    )
    op.create_index(op.f('ix_categorias_problema_id'), 'categorias_problema', ['id'], unique=False)

    op.create_table('servicios',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(), nullable=False),
        sa.Column('base_price', sa.Float(), nullable=True),
        sa.Column('category_id', sa.Integer(), nullable=True),
        sa.ForeignKeyConstraint(['category_id'], ['categorias_problema.id'], ondelete='SET NULL'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_servicios_id'), 'servicios', ['id'], unique=False)

    op.create_table('tecnicos_servicios',
        sa.Column('technician_id', sa.Integer(), nullable=False),
        sa.Column('service_id', sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(['service_id'], ['servicios.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['technician_id'], ['tecnicos.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('technician_id', 'service_id')
    )

    # --- Equipos ---
    op.create_table('equipos',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), nullable=False),
        sa.Column('tipo', sa.String(), nullable=False),
        sa.Column('marca', sa.String(), nullable=False),
        sa.Column('modelo', sa.String(), nullable=False),
        sa.Column('serial_number', sa.String(), nullable=False),
        sa.Column('operating_system', sa.String(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['usuarios.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_equipos_id'), 'equipos', ['id'], unique=False)
    op.create_index(op.f('ix_equipos_serial_number'), 'equipos', ['serial_number'], unique=True)

    # --- Tickets ---
    op.create_table('solicitudes_soporte',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('ticket_number', sa.String(), nullable=False),
        sa.Column('user_id', sa.Integer(), nullable=False),
        sa.Column('technician_id', sa.Integer(), nullable=True),
        sa.Column('equipment_id', sa.Integer(), nullable=True),
        sa.Column('service_id', sa.Integer(), nullable=True),
        sa.Column('problem_description', sa.Text(), nullable=False),
        sa.Column('modalidad', sa.String(), nullable=False),
        sa.Column('estado', sa.String(), nullable=False),
        sa.Column('prioridad', sa.String(), nullable=False),
        sa.Column('technical_solution', sa.Text(), nullable=True),
        sa.Column('parts_used', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('completed_at', sa.DateTime(timezone=True), nullable=True),
        sa.CheckConstraint("prioridad IN ('Baja', 'Media', 'Alta')", name='check_prioridad'),
        sa.ForeignKeyConstraint(['equipment_id'], ['equipos.id'], ondelete='SET NULL'),
        sa.ForeignKeyConstraint(['service_id'], ['servicios.id'], ondelete='SET NULL'),
        sa.ForeignKeyConstraint(['technician_id'], ['usuarios.id'], ondelete='SET NULL'),
        sa.ForeignKeyConstraint(['user_id'], ['usuarios.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_solicitudes_soporte_id'), 'solicitudes_soporte', ['id'], unique=False)
    op.create_index(op.f('ix_solicitudes_soporte_ticket_number'), 'solicitudes_soporte', ['ticket_number'], unique=True)

    # --- Diagnósticos IA ---
    op.create_table('diagnosticos_ia',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('request_id', sa.Integer(), nullable=False),
        sa.Column('diagnosis_text', sa.Text(), nullable=False),
        sa.Column('suggested_priority', sa.String(), nullable=True),
        sa.Column('model_name', sa.String(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.ForeignKeyConstraint(['request_id'], ['solicitudes_soporte.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_diagnosticos_ia_id'), 'diagnosticos_ia', ['id'], unique=False)

    # --- Historial ---
    op.create_table('historial_tickets',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('request_id', sa.Integer(), nullable=False),
        sa.Column('previous_status', sa.String(), nullable=True),
        sa.Column('new_status', sa.String(), nullable=False),
        sa.Column('changed_by_id', sa.Integer(), nullable=True),
        sa.Column('notes', sa.Text(), nullable=True),
        sa.Column('changed_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.ForeignKeyConstraint(['changed_by_id'], ['usuarios.id'], ondelete='SET NULL'),
        sa.ForeignKeyConstraint(['request_id'], ['solicitudes_soporte.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_historial_tickets_id'), 'historial_tickets', ['id'], unique=False)

    # --- Calificaciones ---
    op.create_table('calificaciones',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('request_id', sa.Integer(), nullable=False),
        sa.Column('tech_rating', sa.Integer(), nullable=False),
        sa.Column('service_rating', sa.Integer(), nullable=False),
        sa.Column('support_rating', sa.Integer(), nullable=False),
        sa.Column('general_score', sa.Float(), nullable=True),
        sa.Column('comment', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.CheckConstraint('service_rating BETWEEN 1 AND 5', name='check_service_rating'),
        sa.CheckConstraint('support_rating BETWEEN 1 AND 5', name='check_support_rating'),
        sa.CheckConstraint('tech_rating BETWEEN 1 AND 5', name='check_tech_rating'),
        sa.ForeignKeyConstraint(['request_id'], ['solicitudes_soporte.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('request_id')
    )
    op.create_index(op.f('ix_calificaciones_id'), 'calificaciones', ['id'], unique=False)

    # --- Mantenimientos ---
    op.create_table('mantenimientos',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('equipment_id', sa.Integer(), nullable=False),
        sa.Column('last_maintenance_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('next_maintenance_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('status', sa.String(), nullable=True),
        sa.ForeignKeyConstraint(['equipment_id'], ['equipos.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_mantenimientos_id'), 'mantenimientos', ['id'], unique=False)

    # --- Notificaciones ---
    op.create_table('notificaciones',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), nullable=False),
        sa.Column('title', sa.String(), nullable=False),
        sa.Column('message', sa.Text(), nullable=False),
        sa.Column('is_read', sa.Boolean(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['usuarios.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_notificaciones_id'), 'notificaciones', ['id'], unique=False)


def downgrade() -> None:
    op.drop_table('notificaciones')
    op.drop_table('mantenimientos')
    op.drop_table('calificaciones')
    op.drop_table('historial_tickets')
    op.drop_table('diagnosticos_ia')
    op.drop_table('solicitudes_soporte')
    op.drop_table('equipos')
    op.drop_table('tecnicos_servicios')
    op.drop_table('servicios')
    op.drop_table('categorias_problema')
    op.drop_table('tecnicos')
    op.drop_table('usuarios_roles')
    op.drop_table('roles')
    op.drop_table('usuarios')
