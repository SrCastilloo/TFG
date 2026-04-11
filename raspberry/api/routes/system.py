from fastapi import APIRouter, Request


router = APIRouter()


@router.get("/info") # Ruta para obtener información del sistema
def get_system_info(request: Request):
    controller = request.app.state.controller
    return controller.get_system_info()

