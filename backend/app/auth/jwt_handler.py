from datetime import datetime, timedelta, timezone
from jose import JWTError, jwt
from ..config import get_settings

settings = get_settings()


def create_access_token(usuario_id: int) -> str:
    expire = datetime.now(timezone.utc) + timedelta(minutes=settings.access_token_expire_minutes)
    payload = {"sub": str(usuario_id), "exp": expire}
    return jwt.encode(payload, settings.secret_key, algorithm=settings.algorithm)


def decode_access_token(token: str) -> int | None:
    """Devuelve el usuario_id si el token es válido, None si no lo es."""
    try:
        payload = jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])
        sub: str | None = payload.get("sub")
        if sub is None:
            return None
        return int(sub)
    except (JWTError, ValueError):
        return None
