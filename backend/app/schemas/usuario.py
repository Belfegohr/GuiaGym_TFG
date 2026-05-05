from datetime import datetime
from pydantic import BaseModel, EmailStr, field_validator


class UsuarioCreate(BaseModel):
    nombre: str
    email: EmailStr
    password: str
    peso_inicial: float | None = None
    altura: float | None = None

    @field_validator("password")
    @classmethod
    def password_min_length(cls, v: str) -> str:
        if len(v) < 8:
            raise ValueError("La contraseña debe tener al menos 8 caracteres")
        return v

    @field_validator("nombre")
    @classmethod
    def nombre_not_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("El nombre no puede estar vacío")
        return v.strip()


class UsuarioUpdate(BaseModel):
    nombre: str | None = None
    peso_inicial: float | None = None
    altura: float | None = None
    activo: bool | None = None


class UsuarioResponse(BaseModel):
    id: int
    nombre: str
    email: str
    peso_inicial: float | None
    altura: float | None
    fecha_registro: datetime
    activo: bool

    model_config = {"from_attributes": True}


class UsuarioPublic(BaseModel):
    """Versión mínima para referencias en otras respuestas."""
    id: int
    nombre: str

    model_config = {"from_attributes": True}
