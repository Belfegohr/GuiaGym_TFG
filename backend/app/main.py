from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .config import get_settings
from .database import create_tables
from .routers import (
    auth_router, usuarios_router, ejercicios_router,
    rutinas_router, entrenamientos_router, seguimiento_router,
)

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    create_tables()
    yield


app = FastAPI(
    title=settings.app_name,
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # ajustar en producción
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(usuarios_router)
app.include_router(ejercicios_router)
app.include_router(rutinas_router)
app.include_router(entrenamientos_router)
app.include_router(seguimiento_router)


@app.get("/health", tags=["Health"])
def health():
    return {"status": "ok", "app": settings.app_name}
