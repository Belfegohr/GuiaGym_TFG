from datetime import datetime
from sqlalchemy import Column, DateTime, Float, ForeignKey, Integer, Text
from sqlalchemy.orm import relationship
from ..database import Base


class Entrenamiento(Base):
    __tablename__ = "entrenamiento"

    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer, ForeignKey("usuario.id"), nullable=False)
    rutina_id = Column(Integer, ForeignKey("rutina.id"), nullable=True)
    fecha_inicio = Column(DateTime, default=datetime.utcnow)
    fecha_fin = Column(DateTime, nullable=True)
    notas = Column(Text, nullable=True)

    usuario = relationship("Usuario", back_populates="entrenamientos")
    rutina = relationship("Rutina", back_populates="entrenamientos")
    registros = relationship("RegistroEjercicio", back_populates="entrenamiento", cascade="all, delete-orphan")


class RegistroEjercicio(Base):
    """Una serie realizada de un ejercicio durante un entrenamiento."""
    __tablename__ = "registro_ejercicio"

    id = Column(Integer, primary_key=True, index=True)
    entrenamiento_id = Column(Integer, ForeignKey("entrenamiento.id"), nullable=False)
    ejercicio_id = Column(Integer, ForeignKey("ejercicio.id"), nullable=False)
    serie_numero = Column(Integer, nullable=False)
    repeticiones = Column(Integer, nullable=True)
    peso = Column(Float, nullable=True)            # kg
    tiempo_segundos = Column(Integer, nullable=True)

    entrenamiento = relationship("Entrenamiento", back_populates="registros")
    ejercicio = relationship("Ejercicio", back_populates="registros")
