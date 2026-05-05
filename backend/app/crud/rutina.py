from sqlalchemy.orm import Session
from ..models.rutina import Rutina, RutinaEjercicio
from ..schemas.rutina import RutinaCreate, RutinaUpdate, RutinaEjercicioCreate, RutinaEjercicioUpdate


# ── Rutina ───────────────────────────────────────────────────────────────────

def get_by_id(db: Session, rutina_id: int) -> Rutina | None:
    return db.get(Rutina, rutina_id)


def get_all_for_user(db: Session, usuario_id: int, skip: int = 0, limit: int = 100) -> list[Rutina]:
    return (
        db.query(Rutina)
        .filter(Rutina.usuario_id == usuario_id, Rutina.activo == True)
        .offset(skip).limit(limit).all()
    )


def get_publicas(db: Session, skip: int = 0, limit: int = 100) -> list[Rutina]:
    return (
        db.query(Rutina)
        .filter(Rutina.publica == True, Rutina.activo == True)
        .offset(skip).limit(limit).all()
    )


def create(db: Session, data: RutinaCreate, usuario_id: int) -> Rutina:
    rutina = Rutina(**data.model_dump(), usuario_id=usuario_id)
    db.add(rutina)
    db.commit()
    db.refresh(rutina)
    return rutina


def update(db: Session, rutina: Rutina, data: RutinaUpdate) -> Rutina:
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(rutina, field, value)
    db.commit()
    db.refresh(rutina)
    return rutina


def delete(db: Session, rutina: Rutina) -> None:
    rutina.activo = False
    db.commit()


# ── RutinaEjercicio ──────────────────────────────────────────────────────────

def get_rutina_ejercicio(db: Session, re_id: int) -> RutinaEjercicio | None:
    return db.get(RutinaEjercicio, re_id)


def add_ejercicio(db: Session, rutina_id: int, data: RutinaEjercicioCreate) -> RutinaEjercicio:
    re = RutinaEjercicio(**data.model_dump(), rutina_id=rutina_id)
    db.add(re)
    db.commit()
    db.refresh(re)
    return re


def update_ejercicio(db: Session, re: RutinaEjercicio, data: RutinaEjercicioUpdate) -> RutinaEjercicio:
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(re, field, value)
    db.commit()
    db.refresh(re)
    return re


def remove_ejercicio(db: Session, re: RutinaEjercicio) -> None:
    db.delete(re)
    db.commit()
