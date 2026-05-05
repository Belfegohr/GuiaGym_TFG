from datetime import datetime
from pydantic import BaseModel, Field
from .ejercicio import EjercicioResponse


class RutinaEjercicioCreate(BaseModel):
    ejercicio_id: int
    series: int = Field(default=3, ge=1, le=20)
    repeticiones: int = Field(default=10, ge=1, le=999)
    peso_sugerido: float | None = Field(default=None, ge=0)
    descanso_segundos: int = Field(default=60, ge=0, le=600)
    orden: int = Field(default=0, ge=0)
    dia_semana: str | None = None


class RutinaEjercicioUpdate(BaseModel):
    series: int | None = Field(default=None, ge=1, le=20)
    repeticiones: int | None = Field(default=None, ge=1, le=999)
    peso_sugerido: float | None = Field(default=None, ge=0)
    descanso_segundos: int | None = Field(default=None, ge=0, le=600)
    orden: int | None = Field(default=None, ge=0)
    dia_semana: str | None = None


class RutinaEjercicioResponse(BaseModel):
    id: int
    ejercicio_id: int
    series: int
    repeticiones: int
    peso_sugerido: float | None
    descanso_segundos: int
    orden: int
    dia_semana: str | None
    ejercicio: EjercicioResponse

    model_config = {"from_attributes": True}


class RutinaCreate(BaseModel):
    nombre: str
    descripcion: str | None = None
    publica: bool = False


class RutinaUpdate(BaseModel):
    nombre: str | None = None
    descripcion: str | None = None
    publica: bool | None = None
    activo: bool | None = None


class RutinaResponse(BaseModel):
    id: int
    nombre: str
    descripcion: str | None
    usuario_id: int
    fecha_creacion: datetime
    activo: bool
    publica: bool
    ejercicios: list[RutinaEjercicioResponse] = []

    model_config = {"from_attributes": True}


class RutinaListResponse(BaseModel):
    """Versión sin ejercicios para listados."""
    id: int
    nombre: str
    descripcion: str | None
    usuario_id: int
    fecha_creacion: datetime
    activo: bool
    publica: bool

    model_config = {"from_attributes": True}
