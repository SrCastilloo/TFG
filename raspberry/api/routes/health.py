from fastapi import APIRouter

router = APIRouter()


# Ruta para verificar que la API está funcionando correctamente
@router.get("/")
def health():
    return {
        "ok": True,
        "message": "Backend operativo"
    }