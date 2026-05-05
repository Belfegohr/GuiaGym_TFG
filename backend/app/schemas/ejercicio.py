from typing import Literal
from pydantic import BaseModel

GrupoMuscular = Literal["pecho", "espalda", "piernas", "hombros", "brazos", "core", "cuerpo_completo", "otro"]
TipoEjercicio = Literal["fuerza", "cardio", "flexibilidad", "funcional"]
Dificultad = Literal["principiante", "intermedio", "avanzado"]


class EjercicioCreate(BaseModel):
    nombre: str
    descripcion: str | None = None
    grupo_muscular: GrupoMuscular | None = None
    tipo: TipoEjercicio | None = None
    dificultad: Dificultad | None = None
    imagen_url: str | None = None


class EjercicioUpdate(BaseModel):
    nombre: str | None = None
    descripcion: str | None = None
    grupo_muscular: GrupoMuscular | None = None
    tipo: TipoEjercicio | None = None
    dificultad: Dificultad | None = None
    imagen_url: str | None = None


class EjercicioResponse(BaseModel):
    id: int
    nombre: str
    descripcion: str | None
    grupo_muscular: str | None
    tipo: str | None
    dificultad: str | None
    imagen_url: str | None
    creado_por: int | None

    model_config = {"from_attributes": True}
