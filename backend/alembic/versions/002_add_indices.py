"""add indices for performance

Revision ID: 002_add_indices
Revises: 001_baseline
Create Date: 2024-09-17 11:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '002_add_indices'
down_revision: Union[str, None] = '001_baseline'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # --- Equipos ---
    op.create_index(op.f('ix_equipos_user_id'), 'equipos', ['user_id'], unique=False)

    # --- Solicitudes ---
    op.create_index(op.f('ix_solicitudes_soporte_user_id'), 'solicitudes_soporte', ['user_id'], unique=False)
    op.create_index(op.f('ix_solicitudes_soporte_technician_id'), 'solicitudes_soporte', ['technician_id'], unique=False)
    op.create_index(op.f('ix_solicitudes_soporte_equipment_id'), 'solicitudes_soporte', ['equipment_id'], unique=False)
    op.create_index(op.f('ix_solicitudes_soporte_estado'), 'solicitudes_soporte', ['estado'], unique=False)
    op.create_index(op.f('ix_solicitudes_soporte_created_at'), 'solicitudes_soporte', ['created_at'], unique=False)

    # --- Diagnósticos ---
    op.create_index(op.f('ix_diagnosticos_ia_request_id'), 'diagnosticos_ia', ['request_id'], unique=False)

    # --- Historial ---
    op.create_index(op.f('ix_historial_tickets_request_id'), 'historial_tickets', ['request_id'], unique=False)

    # --- Notificaciones ---
    op.create_index(op.f('ix_notificaciones_user_id'), 'notificaciones', ['user_id'], unique=False)


def downgrade() -> None:
    op.drop_index(op.f('ix_notificaciones_user_id'), table_name='notificaciones')
    op.drop_index(op.f('ix_historial_tickets_request_id'), table_name='historial_tickets')
    op.drop_index(op.f('ix_diagnosticos_ia_request_id'), table_name='diagnosticos_ia')
    op.drop_index(op.f('ix_solicitudes_soporte_created_at'), table_name='solicitudes_soporte')
    op.drop_index(op.f('ix_solicitudes_soporte_estado'), table_name='solicitudes_soporte')
    op.drop_index(op.f('ix_solicitudes_soporte_equipment_id'), table_name='solicitudes_soporte')
    op.drop_index(op.f('ix_solicitudes_soporte_technician_id'), table_name='solicitudes_soporte')
    op.drop_index(op.f('ix_solicitudes_soporte_user_id'), table_name='solicitudes_soporte')
    op.drop_index(op.f('ix_equipos_user_id'), table_name='equipos')
