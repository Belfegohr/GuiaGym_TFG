from sqlalchemy.orm import Session
from ..models.usuario import Usuario
from ..schemas.usuario import UsuarioCreate, UsuarioUpdate
from ..auth.password import hash_password


def get_by_id(db: Session, usuario_id: int) -> Usuario | None:
    return db.get(Usuario, usuario_id)


def get_by_email(db: Session, email: str) -> Usuario | None:
    return db.query(Usuario).filter(Usuario.email == email).first()


def get_all(db: Session, skip: int = 0, limit: int = 100) -> list[Usuario]:
    return db.query(Usuario).filter(Usuario.activo == True).offset(skip).limit(limit).all()


def create(db: Session, data: UsuarioCreate) -> Usuario:
    usuario = Usuario(
        nombre=data.nombre,
        email=data.email,
        password_hash=hash_password(data.password),
        peso_inicial=data.peso_inicial,
        altura=data.altura,
    )
    db.add(usuario)
    db.commit()
    db.refresh(usuario)
    return usuario


def update(db: Session, usuario: Usuario, data: UsuarioUpdate) -> Usuario:
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(usuario, field, value)
    db.commit()
    db.refresh(usuario)
    return usuario


def delete(db: Session, usuario: Usuario) -> None:
    usuario.activo = False
    db.commit()
