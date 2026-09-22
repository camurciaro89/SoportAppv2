from sqlalchemy.orm import declarative_base, sessionmaker
from sqlalchemy import create_engine
import os

# En producción esto vendría de una variable de entorno
SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:soportapp2024@db:5432/soportapp"
)

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
