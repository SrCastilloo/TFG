from fastapi import APIRouter, Request

router = APIRouter()


# Permite cambiar el modo de operación de la mano robótica

@router.post("/hand") # Cambia el modo a "mano"
def set_mode_hand(request: Request):
    controller = request.app.state.controller
    return controller.set_mode_hand()

@router.post("/voice") # Cambia el modo a "voz"
def set_mode_voice(request: Request):
    controller = request.app.state.controller
    return controller.set_mode_voice()


@router.post("/camera") # Cambia el modo a "cámara"
def set_mode_camera(request: Request):
    controller = request.app.state.controller
    return controller.set_mode_camera()

