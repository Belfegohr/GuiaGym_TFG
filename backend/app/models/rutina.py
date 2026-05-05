from datetime import datetime
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship
from ..database import Base


class Rutina(Base):
    __tablename__ = "rutina"

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String(200), nullable=False)
    descripcion = Column(Text, nullable=True)
    usuario_id = Column(Integer, ForeignKey("usuario.id"), nullable=False)
    fecha_creacion = Column(DateTime, default=datetime.utcnow)
    activo = Column(Boolean, default=True)
    publica = Column(Boolean, default=False)

    usuario = relationship("Usuario", back_populates="rutinas")
    ejercicios = relationship("RutinaEjercicio", back_populates="rutina", cascade="all, delete-orphan", order_by="RutinaEjercicio.orden")
    entrenamientos = relationship("Entrenamiento", back_populates="rutina")


class RutinaEjercicio(Base):
    """Tabla de unión que guarda configuración por ejercicio dentro de una rutina."""
    __tablename__ = "rutina_ejercicio"

    id = Column(Integer, primary_key=True, index=True)
    rutina_id = Column(Integer, ForeignKey("rutina.id"), nullable=False)
    ejercicio_id = Column(Integer, ForeignKey("ejercicio.id"), nullable=False)
    series = Column(Integer, default=3)
    repeticiones = Column(Integer, default=10)
    peso_sugerido = Column(Float, nullable=True)
    descanso_segundos = Column(Integer, default=60)
    orden = Column(Integer, default=0)
    dia_semana = Column(String(20), nullable=True)

    rutina = relationship("Rutina", back_populates="ejercicios")
    ejercicio = relationship("Ejercicio", back_populates="rutina_ejercicios")
