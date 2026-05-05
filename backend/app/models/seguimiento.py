from datetime import datetime
from sqlalchemy import Column, DateTime, Float, ForeignKey, Integer, Text
from sqlalchemy.orm import relationship
from ..database import Base


class SeguimientoPeso(Base):
    __tablename__ = "seguimiento_peso"

    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer, ForeignKey("usuario.id"), nullable=False)
    peso = Column(Float, nullable=False)           # kg
    fecha = Column(DateTime, default=datetime.utcnow, index=True)
    notas = Column(Text, nullable=True)

    usuario = relationship("Usuario", back_populates="seguimiento_peso")
