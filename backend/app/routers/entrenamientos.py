from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.usuario import Usuario
from ..schemas.entrenamiento import (
    EntrenamientoCreate, EntrenamientoFinalizar,
    EntrenamientoResponse, EntrenamientoListResponse,
    RegistroEjercicioCreate, RegistroEjercicioResponse,
)
from ..crud import entrenamiento as crud_entrenamiento
from ..auth.dependencies import get_current_user

router = APIRouter(prefix="/entrenamientos", tags=["Entrenamientos"])


def _get_entrenamiento_or_404(db: Session, entrenamiento_id: int) -> object:
    e = crud_entrenamiento.get_by_id(db, entrenamiento_id)
    if not e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Entrenamiento no encontrado")
    return e


def _assert_owner(entrenamiento, current_user: Usuario) -> None:
    if entrenamiento.usuario_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso sobre este entrenamiento")


# ── Entrenamiento ─────────────────────────────────────────────────────────────

@router.get("/", response_model=list[EntrenamientoListResponse])
def historial(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Historial de entrenamientos del usuario autenticado, más recientes primero."""
    return crud_entrenamiento.get_all_for_user(db, current_user.id, skip, limit)


@router.get("/{entrenamiento_id}", response_model=EntrenamientoResponse)
def get_entrenamiento(
    entrenamiento_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    e = _get_entrenamiento_or_404(db, entrenamiento_id)
    _assert_owner(e, current_user)
    return e


@router.post("/", response_model=EntrenamientoResponse, status_code=status.HTTP_201_CREATED)
def iniciar(
    data: EntrenamientoCreate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Inicia un nuevo entrenamiento (fecha_inicio = ahora)."""
    return crud_entrenamiento.create(db, data, current_user.id)


@router.patch("/{entrenamiento_id}/finalizar", response_model=EntrenamientoResponse)
def finalizar(
    entrenamiento_id: int,
    data: EntrenamientoFinalizar,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Cierra el entrenamiento registrando la hora de fin."""
    e = _get_entrenamiento_or_404(db, entrenamiento_id)
    _assert_owner(e, current_user)
    if e.fecha_fin is not None:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="El entrenamiento ya ha sido finalizado")
    return crud_entrenamiento.finalizar(db, e, data)


# ── Registros (series) ────────────────────────────────────────────────────────

@router.get("/{entrenamiento_id}/registros", response_model=list[RegistroEjercicioResponse])
def get_registros(
    entrenamiento_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    e = _get_entrenamiento_or_404(db, entrenamiento_id)
    _assert_owner(e, current_user)
    return crud_entrenamiento.get_registros(db, entrenamiento_id)


@router.post("/{entrenamiento_id}/registros", response_model=RegistroEjercicioResponse, status_code=status.HTTP_201_CREATED)
def add_registro(
    entrenamiento_id: int,
    data: RegistroEjercicioCreate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Registra una serie realizada durante el entrenamiento en curso."""
    e = _get_entrenamiento_or_404(db, entrenamiento_id)
    _assert_owner(e, current_user)
    if e.fecha_fin is not None:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="No puedes añadir series a un entrenamiento ya finalizado")
    return crud_entrenamiento.add_registro(db, entrenamiento_id, data)


@router.delete("/{entrenamiento_id}/registros/{registro_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_registro(
    entrenamiento_id: int,
    registro_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Elimina una serie registrada por error durante el entrenamiento."""
    e = _get_entrenamiento_or_404(db, entrenamiento_id)
    _assert_owner(e, current_user)
    if e.fecha_fin is not None:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="No puedes editar un entrenamiento ya finalizado")
    from ..models.entrenamiento import RegistroEjercicio
    registro = db.get(RegistroEjercicio, registro_id)
    if not registro or registro.entrenamiento_id != entrenamiento_id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Serie no encontrada en este entrenamiento")
    crud_entrenamiento.delete_registro(db, registro)
