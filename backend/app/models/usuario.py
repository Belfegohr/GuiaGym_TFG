from datetime import datetime
from sqlalchemy import Boolean, Column, DateTime, Float, Integer, String
from sqlalchemy.orm import relationship
from ..database import Base


class Usuario(Base):
    __tablename__ = "usuario"

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String(100), nullable=False)
    email = Column(String(255), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    peso_inicial = Column(Float, nullable=True)
    altura = Column(Float, nullable=True)          # centímetros
    fecha_registro = Column(DateTime, default=datetime.utcnow)
    activo = Column(Boolean, default=True)

    rutinas = relationship("Rutina", back_populates="usuario", cascade="all, delete-orphan")
    entrenamientos = relationship("Entrenamiento", back_populates="usuario", cascade="all, delete-orphan")
    seguimiento_peso = relationship("SeguimientoPeso", back_populates="usuario", cascade="all, delete-orphan")
    ejercicios_creados = relationship("Ejercicio", back_populates="creador")
