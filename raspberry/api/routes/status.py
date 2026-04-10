from fastapi import APIRouter, Request

router = APIRouter()


@router.get("/") # Ruta para obtener el estado actual del sistema
def get_status(request: Request):
    controller = request.app.state.controller
    return controller.get_status()



@router.post("/refresh-hand") # Fuerza una lectura del estado actual de la mano
def refresh_hand(request: Request):
    controller = request.app.state.controller
    return controller.refresh_hand_status()
