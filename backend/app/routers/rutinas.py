from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.usuario import Usuario
from ..schemas.rutina import (
    RutinaCreate, RutinaUpdate, RutinaResponse, RutinaListResponse,
    RutinaEjercicioCreate, RutinaEjercicioUpdate, RutinaEjercicioResponse,
)
from ..crud import rutina as crud_rutina
from ..auth.dependencies import get_current_user

router = APIRouter(prefix="/rutinas", tags=["Rutinas"])


def _get_rutina_or_404(db: Session, rutina_id: int) -> object:
    rutina = crud_rutina.get_by_id(db, rutina_id)
    if not rutina or not rutina.activo:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Rutina no encontrada")
    return rutina


def _assert_owner(rutina, current_user: Usuario) -> None:
    if rutina.usuario_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso sobre esta rutina")


# ── Rutinas ──────────────────────────────────────────────────────────────────

@router.get("/", response_model=list[RutinaListResponse])
def list_rutinas(
    publicas: bool = Query(default=False, description="Si true devuelve rutinas públicas de todos los usuarios"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    if publicas:
        return crud_rutina.get_publicas(db, skip, limit)
    return crud_rutina.get_all_for_user(db, current_user.id, skip, limit)


@router.get("/{rutina_id}", response_model=RutinaResponse)
def get_rutina(
    rutina_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    rutina = _get_rutina_or_404(db, rutina_id)
    if not rutina.publica and rutina.usuario_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Esta rutina es privada")
    return rutina


@router.post("/", response_model=RutinaResponse, status_code=status.HTTP_201_CREATED)
def create_rutina(
    data: RutinaCreate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    return crud_rutina.create(db, data, current_user.id)


@router.put("/{rutina_id}", response_model=RutinaResponse)
def update_rutina(
    rutina_id: int,
    data: RutinaUpdate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    rutina = _get_rutina_or_404(db, rutina_id)
    _assert_owner(rutina, current_user)
    return crud_rutina.update(db, rutina, data)


@router.patch("/{rutina_id}/visibilidad", response_model=RutinaResponse)
def toggle_visibilidad(
    rutina_id: int,
    publica: bool,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Atajo para cambiar solo la visibilidad pública/privada."""
    rutina = _get_rutina_or_404(db, rutina_id)
    _assert_owner(rutina, current_user)
    return crud_rutina.update(db, rutina, RutinaUpdate(publica=publica))


@router.delete("/{rutina_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_rutina(
    rutina_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    rutina = _get_rutina_or_404(db, rutina_id)
    _assert_owner(rutina, current_user)
    crud_rutina.delete(db, rutina)


# ── Ejercicios de la rutina ───────────────────────────────────────────────────

@router.post("/{rutina_id}/ejercicios", response_model=RutinaEjercicioResponse, status_code=status.HTTP_201_CREATED)
def add_ejercicio(
    rutina_id: int,
    data: RutinaEjercicioCreate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    rutina = _get_rutina_or_404(db, rutina_id)
    _assert_owner(rutina, current_user)
    return crud_rutina.add_ejercicio(db, rutina_id, data)


@router.put("/{rutina_id}/ejercicios/{re_id}", response_model=RutinaEjercicioResponse)
def update_ejercicio(
    rutina_id: int,
    re_id: int,
    data: RutinaEjercicioUpdate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    rutina = _get_rutina_or_404(db, rutina_id)
    _assert_owner(rutina, current_user)
    re = crud_rutina.get_rutina_ejercicio(db, re_id)
    if not re or re.rutina_id != rutina_id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Entrada no encontrada en esta rutina")
    return crud_rutina.update_ejercicio(db, re, data)


@router.delete("/{rutina_id}/ejercicios/{re_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_ejercicio(
    rutina_id: int,
    re_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    rutina = _get_rutina_or_404(db, rutina_id)
    _assert_owner(rutina, current_user)
    re = crud_rutina.get_rutina_ejercicio(db, re_id)
    if not re or re.rutina_id != rutina_id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Entrada no encontrada en esta rutina")
    crud_rutina.remove_ejercicio(db, re)
