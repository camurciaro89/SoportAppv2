"""upgrade to argon2

Revision ID: 003_upgrade_to_argon2
Revises: 002_add_indices
Create Date: 2024-09-17 11:15:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '003_upgrade_to_argon2'
down_revision: Union[str, None] = '002_add_indices'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # --- Usuarios ---
    # Eliminar columna salt ya que Argon2 la maneja internamente en el hash
    op.drop_column('usuarios', 'salt')


def downgrade() -> None:
    # --- Usuarios ---
    op.add_column('usuarios', sa.Column('salt', sa.String(), nullable=True))
