from datetime import datetime
from pydantic import BaseModel, Field


class SeguimientoPesoCreate(BaseModel):
    peso: float = Field(gt=0, le=500)
    fecha: datetime | None = None
    notas: str | None = None


class SeguimientoPesoResponse(BaseModel):
    id: int
    usuario_id: int
    peso: float
    fecha: datetime
    notas: str | None

    model_config = {"from_attributes": True}
