from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker, Session
from .config import get_settings

settings = get_settings()

connect_args = {"check_same_thread": False} if "sqlite" in settings.database_url else {}

engine = create_engine(settings.database_url, connect_args=connect_args)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass


def get_db() -> Session:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def create_tables():
    # Import all models so Base knows about them before creating tables
    from .models import usuario, ejercicio, rutina, entrenamiento, seguimiento  # noqa: F401
    Base.metadata.create_all(bind=engine)
