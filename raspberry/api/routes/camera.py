from fastapi import APIRouter, Request, HTTPException
from fastapi.responses import Response

router = APIRouter()

@router.post("/detect")
def detect_best_object(request: Request):
    controller = request.app.state.controller
    return controller.detect_best_object()

@router.post("/detect-and-move")
def detect_and_move(request: Request):
    controller = request.app.state.controller
    return controller.detect_object_and_move()

@router.get("/frame")
def get_camera_frame(request: Request, draw: bool = False):
    controller = request.app.state.controller
    frame = controller.get_camera_frame(draw=draw)

    if frame is None:
        raise HTTPException(status_code=503, detail="No se pudo obtener imagen de la cámara")

    return Response(content=frame, media_type="image/jpeg")