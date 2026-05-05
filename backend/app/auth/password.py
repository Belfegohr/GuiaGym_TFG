import base64
import hashlib
import bcrypt


def _prehash(plain: str) -> bytes:
    # SHA-256 el texto plano y lo codifica en base64 para que quede en 44 bytes
    # ASCII, eliminando el límite de 72 bytes de bcrypt sin pérdida de entropía.
    digest = hashlib.sha256(plain.encode()).digest()
    return base64.b64encode(digest)


def hash_password(plain: str) -> str:
    return bcrypt.hashpw(_prehash(plain), bcrypt.gensalt()).decode()


def verify_password(plain: str, hashed: str) -> bool:
    return bcrypt.checkpw(_prehash(plain), hashed.encode())
