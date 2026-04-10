from fastapi import FastAPI
from pathlib import Path
from core.controller import HandSystemController
from api.routes.health import router as health_router
from api.routes.status import router as status_router
from api.routes.modes import router as modes_router
from api.routes.hand import router as hand_router
from api.routes.camera import router as camera_router


BASE_DIR = Path(__file__).resolve().parent
CONFIG_PATH = BASE_DIR / "config" / "config.ini"



app = FastAPI(title = "Raspberry Pi Hand Controller API", version="1.0.0")


# Crear una instancia global del controlador para que las rutas puedan acceder a él
app.state.controller = HandSystemController(config_path=str(CONFIG_PATH),
                                            simulation = True) 


@app.get("/") # Ruta raíz para verificar que la API está funcionando
def root():
    return{
        "ok": True,
        "message": "API de la mano robótica funcionando"
    }

# Incluir los routers de las diferentes rutas
app.include_router(health_router, prefix="/health", tags=["Health"])
app.include_router(status_router, prefix="/status", tags=["Status"])
app.include_router(modes_router, prefix="/modes", tags=["Modes"])
app.include_router(hand_router, prefix="/hand", tags=["Hand"])
app.include_router(camera_router, prefix="/camera", tags=["Camera"])
