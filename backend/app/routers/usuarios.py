from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.usuario import Usuario
from ..schemas.usuario import UsuarioResponse, UsuarioUpdate
from ..crud import usuario as crud_usuario
from ..auth.dependencies import get_current_user

router = APIRouter(prefix="/usuarios", tags=["Usuarios"])


@router.get("/me", response_model=UsuarioResponse)
def get_me(current_user: Usuario = Depends(get_current_user)):
    return current_user


@router.put("/me", response_model=UsuarioResponse)
def update_me(
    data: UsuarioUpdate,
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    return crud_usuario.update(db, current_user, data)


@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_me(
    db: Session = Depends(get_db),
    current_user: Usuario = Depends(get_current_user),
):
    crud_usuario.delete(db, current_user)


# ── Endpoints de administración (solo para dev/testing) ──────────────────────

@router.get("/", response_model=list[UsuarioResponse])
def list_usuarios(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    _: Usuario = Depends(get_current_user),   # requiere auth
):
    return crud_usuario.get_all(db, skip, limit)


@router.get("/{usuario_id}", response_model=UsuarioResponse)
def get_usuario(
    usuario_id: int,
    db: Session = Depends(get_db),
    _: Usuario = Depends(get_current_user),
):
    usuario = crud_usuario.get_by_id(db, usuario_id)
    if not usuario:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Usuario no encontrado")
    return usuario
