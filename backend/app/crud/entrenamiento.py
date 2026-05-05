from datetime import datetime, timezone
from sqlalchemy.orm import Session
from ..models.entrenamiento import Entrenamiento, RegistroEjercicio
from ..schemas.entrenamiento import EntrenamientoCreate, EntrenamientoFinalizar, RegistroEjercicioCreate


# ── Entrenamiento ────────────────────────────────────────────────────────────

def get_by_id(db: Session, entrenamiento_id: int) -> Entrenamiento | None:
    return db.get(Entrenamiento, entrenamiento_id)


def get_all_for_user(
    db: Session,
    usuario_id: int,
    skip: int = 0,
    limit: int = 100,
) -> list[Entrenamiento]:
    return (
        db.query(Entrenamiento)
        .filter(Entrenamiento.usuario_id == usuario_id)
        .order_by(Entrenamiento.fecha_inicio.desc())
        .offset(skip).limit(limit).all()
    )


def create(db: Session, data: EntrenamientoCreate, usuario_id: int) -> Entrenamiento:
    entrenamiento = Entrenamiento(
        usuario_id=usuario_id,
        rutina_id=data.rutina_id,
        notas=data.notas,
    )
    db.add(entrenamiento)
    db.commit()
    db.refresh(entrenamiento)
    return entrenamiento


def finalizar(db: Session, entrenamiento: Entrenamiento, data: EntrenamientoFinalizar) -> Entrenamiento:
    entrenamiento.fecha_fin = datetime.now(timezone.utc)
    if data.notas is not None:
        entrenamiento.notas = data.notas
    db.commit()
    db.refresh(entrenamiento)
    return entrenamiento


# ── RegistroEjercicio ────────────────────────────────────────────────────────

def add_registro(
    db: Session,
    entrenamiento_id: int,
    data: RegistroEjercicioCreate,
) -> RegistroEjercicio:
    registro = RegistroEjercicio(**data.model_dump(), entrenamiento_id=entrenamiento_id)
    db.add(registro)
    db.commit()
    db.refresh(registro)
    return registro


def get_registros(db: Session, entrenamiento_id: int) -> list[RegistroEjercicio]:
    return (
        db.query(RegistroEjercicio)
        .filter(RegistroEjercicio.entrenamiento_id == entrenamiento_id)
        .order_by(RegistroEjercicio.ejercicio_id, RegistroEjercicio.serie_numero)
        .all()
    )


def delete_registro(db: Session, registro: RegistroEjercicio) -> None:
    db.delete(registro)
    db.commit()
