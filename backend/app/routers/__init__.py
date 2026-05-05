from .auth import router as auth_router
from .usuarios import router as usuarios_router
from .ejercicios import router as ejercicios_router
from .rutinas import router as rutinas_router
from .entrenamientos import router as entrenamientos_router
from .seguimiento import router as seguimiento_router

__all__ = [
    "auth_router",
    "usuarios_router",
    "ejercicios_router",
    "rutinas_router",
    "entrenamientos_router",
    "seguimiento_router",
]
