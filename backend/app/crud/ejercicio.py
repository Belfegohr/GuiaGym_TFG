from sqlalchemy.orm import Session
from ..models.ejercicio import Ejercicio
from ..schemas.ejercicio import EjercicioCreate, EjercicioUpdate


def get_by_id(db: Session, ejercicio_id: int) -> Ejercicio | None:
    return db.get(Ejercicio, ejercicio_id)


def get_all(
    db: Session,
    skip: int = 0,
    limit: int = 100,
    grupo_muscular: str | None = None,
    tipo: str | None = None,
    dificultad: str | None = None,
) -> list[Ejercicio]:
    q = db.query(Ejercicio)
    if grupo_muscular:
        q = q.filter(Ejercicio.grupo_muscular == grupo_muscular)
    if tipo:
        q = q.filter(Ejercicio.tipo == tipo)
    if dificultad:
        q = q.filter(Ejercicio.dificultad == dificultad)
    return q.offset(skip).limit(limit).all()


def create(db: Session, data: EjercicioCreate, usuario_id: int) -> Ejercicio:
    ejercicio = Ejercicio(**data.model_dump(), creado_por=usuario_id)
    db.add(ejercicio)
    db.commit()
    db.refresh(ejercicio)
    return ejercicio


def update(db: Session, ejercicio: Ejercicio, data: EjercicioUpdate) -> Ejercicio:
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(ejercicio, field, value)
    db.commit()
    db.refresh(ejercicio)
    return ejercicio


def delete(db: Session, ejercicio: Ejercicio) -> None:
    db.delete(ejercicio)
    db.commit()
