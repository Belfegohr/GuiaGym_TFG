from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ..database import get_db
from ..schemas.auth import LoginRequest, TokenResponse
from ..schemas.usuario import UsuarioCreate, UsuarioResponse
from ..crud import usuario as crud_usuario
from ..auth.password import verify_password
from ..auth.jwt_handler import create_access_token

router = APIRouter(prefix="/auth", tags=["Auth"])


@router.post("/registro", response_model=UsuarioResponse, status_code=status.HTTP_201_CREATED)
def registro(data: UsuarioCreate, db: Session = Depends(get_db)):
    if crud_usuario.get_by_email(db, data.email):
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Ya existe una cuenta con ese email",
        )
    return crud_usuario.create(db, data)


@router.post("/login", response_model=TokenResponse)
def login(data: LoginRequest, db: Session = Depends(get_db)):
    usuario = crud_usuario.get_by_email(db, data.email)
    if not usuario or not verify_password(data.password, usuario.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Email o contraseña incorrectos",
        )
    if not usuario.activo:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Cuenta desactivada",
        )
    return TokenResponse(access_token=create_access_token(usuario.id))
