from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.usuario import Usuario
from ..schemas.ejercicio import EjercicioCreate, EjercicioResponse, EjercicioUpdate
from ..crud import ejercicio as crud_ejercicio
from ..auth.dependencies import get_current_user

router = APIRouter(prefix="/ejercicios", tags=["Ejercicios"])


@router.get("/", response_model=list[EjercicioResponse])
def list_ejercicios(
    skip: int = 0,
    limit: int = 100,
    grupo_muscular: str | None = Query(default=None),
    tipo: str | None = Query(default=None),
    dificultad: str | None = Query(default=None),
    db: Session = Depends(get_db),
    _: Usuario = Depends(get_current_user),
):
    return crud_ejercicio.get_all(db, skip, limit, grupo_muscular, tipo, dificultad)


@router.get("/{ejercicio_id}", response_model=EjercicioResponse)
def get_ejercicio(
    ejercicio_id: int,
    db: Session = Depends(get_db),
    _: Usuario = Depends(get_current_user),
):
    ejercicio = crud_ejercicio.get_by_id(db, ejercicio_id)
    if not ejercicio:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Ejercicio no encontrado")
    return ejercicio


@router.post("/", response_model=EjercicioResponse, status_code=status.HTTP_201_CREATED)
def create_ejercicio(
    data: EjercicioCreate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    return crud_ejercicio.create(db, data, current_user.id)


@router.put("/{ejercicio_id}", response_model=EjercicioResponse)
def update_ejercicio(
    ejercicio_id: int,
    data: EjercicioUpdate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    ejercicio = crud_ejercicio.get_by_id(db, ejercicio_id)
    if not ejercicio:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Ejercicio no encontrado")
    if ejercicio.creado_por != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso para editar este ejercicio")
    return crud_ejercicio.update(db, ejercicio, data)


@router.delete("/{ejercicio_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_ejercicio(
    ejercicio_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    ejercicio = crud_ejercicio.get_by_id(db, ejercicio_id)
    if not ejercicio:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Ejercicio no encontrado")
    if ejercicio.creado_por != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso para eliminar este ejercicio")
    crud_ejercicio.delete(db, ejercicio)
