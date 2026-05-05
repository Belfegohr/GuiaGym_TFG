from sqlalchemy import Column, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship
from ..database import Base


class Ejercicio(Base):
    __tablename__ = "ejercicio"

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String(200), nullable=False, index=True)
    descripcion = Column(Text, nullable=True)
    grupo_muscular = Column(String(100), nullable=True)   # pecho, espalda, piernas, hombros, brazos, core
    tipo = Column(String(50), nullable=True)              # fuerza, cardio, flexibilidad, funcional
    dificultad = Column(String(20), nullable=True)        # principiante, intermedio, avanzado
    imagen_url = Column(String(500), nullable=True)
    creado_por = Column(Integer, ForeignKey("usuario.id"), nullable=True)

    creador = relationship("Usuario", back_populates="ejercicios_creados")
    rutina_ejercicios = relationship("RutinaEjercicio", back_populates="ejercicio", cascade="all, delete-orphan")
    registros = relationship("RegistroEjercicio", back_populates="ejercicio")
