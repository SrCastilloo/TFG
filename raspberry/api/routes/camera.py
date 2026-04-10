from fastapi import APIRouter, Request

router = APIRouter()

@router.post("/detect") # Ruta para activar la detección de objetos con la cámara
def detect_best_object(request: Request):
    controller = request.app.state.controller
    return controller.detect_best_object()


@router.post("/detect-and-move") # Ruta para activar la detección de objetos y mover la mano hacia el mejor objeto detectado
def detect_and_move(request: Request):
    controller = request.app.state.controller
    return controller.detect_object_and_move()

