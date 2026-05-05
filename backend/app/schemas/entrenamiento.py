from datetime import datetime
from pydantic import BaseModel, Field
from .ejercicio import EjercicioResponse


class EntrenamientoCreate(BaseModel):
    rutina_id: int | None = None
    notas: str | None = None


class EntrenamientoFinalizar(BaseModel):
    notas: str | None = None


class RegistroEjercicioCreate(BaseModel):
    ejercicio_id: int
    serie_numero: int = Field(ge=1)
    repeticiones: int | None = Field(default=None, ge=0)
    peso: float | None = Field(default=None, ge=0)
    tiempo_segundos: int | None = Field(default=None, ge=0)


class RegistroEjercicioResponse(BaseModel):
    id: int
    ejercicio_id: int
    serie_numero: int
    repeticiones: int | None
    peso: float | None
    tiempo_segundos: int | None
    ejercicio: EjercicioResponse

    model_config = {"from_attributes": True}


class EntrenamientoResponse(BaseModel):
    id: int
    usuario_id: int
    rutina_id: int | None
    fecha_inicio: datetime
    fecha_fin: datetime | None
    notas: str | None
    registros: list[RegistroEjercicioResponse] = []

    model_config = {"from_attributes": True}


class EntrenamientoListResponse(BaseModel):
    """Sin registros para listados."""
    id: int
    usuario_id: int
    rutina_id: int | None
    fecha_inicio: datetime
    fecha_fin: datetime | None
    notas: str | None

    model_config = {"from_attributes": True}
