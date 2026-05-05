from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.usuario import Usuario
from ..schemas.seguimiento import SeguimientoPesoCreate, SeguimientoPesoResponse
from ..crud import seguimiento as crud_seguimiento
from ..auth.dependencies import get_current_user

router = APIRouter(prefix="/seguimiento", tags=["Seguimiento de peso"])


@router.get("/", response_model=list[SeguimientoPesoResponse])
def historial_peso(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    """Devuelve el historial de pesos del usuario, más recientes primero."""
    return crud_seguimiento.get_all_for_user(db, current_user.id, skip, limit)


@router.post("/", response_model=SeguimientoPesoResponse, status_code=status.HTTP_201_CREATED)
def registrar_peso(
    data: SeguimientoPesoCreate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    return crud_seguimiento.create(db, data, current_user.id)


@router.delete("/{registro_id}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar_registro(
    registro_id: int,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    registro = crud_seguimiento.get_by_id(db, registro_id)
    if not registro:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Registro no encontrado")
    if registro.usuario_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso sobre este registro")
    crud_seguimiento.delete(db, registro)
