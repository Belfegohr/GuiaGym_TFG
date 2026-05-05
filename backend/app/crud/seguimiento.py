from datetime import datetime, timezone
from sqlalchemy.orm import Session
from ..models.seguimiento import SeguimientoPeso
from ..schemas.seguimiento import SeguimientoPesoCreate


def get_by_id(db: Session, registro_id: int) -> SeguimientoPeso | None:
    return db.get(SeguimientoPeso, registro_id)


def get_all_for_user(
    db: Session,
    usuario_id: int,
    skip: int = 0,
    limit: int = 100,
) -> list[SeguimientoPeso]:
    return (
        db.query(SeguimientoPeso)
        .filter(SeguimientoPeso.usuario_id == usuario_id)
        .order_by(SeguimientoPeso.fecha.desc())
        .offset(skip).limit(limit).all()
    )


def create(db: Session, data: SeguimientoPesoCreate, usuario_id: int) -> SeguimientoPeso:
    registro = SeguimientoPeso(
        usuario_id=usuario_id,
        peso=data.peso,
        fecha=data.fecha or datetime.now(timezone.utc),
        notas=data.notas,
    )
    db.add(registro)
    db.commit()
    db.refresh(registro)
    return registro


def delete(db: Session, registro: SeguimientoPeso) -> None:
    db.delete(registro)
    db.commit()
