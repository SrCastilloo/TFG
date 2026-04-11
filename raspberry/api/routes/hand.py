from fastapi import APIRouter, Request
from pydantic import BaseModel

router = APIRouter()


#Exponemos por API las acciones básicas de la mano

class ManualMoveRequest(BaseModel):
    command: dict[str,str] 

@router.post("/open")
def open_hand(request: Request):
    controller = request.app.state.controller
    return controller.open_hand()


@router.post("/stop")
def stop_hand(request: Request):
    controller = request.app.state.controller
    return controller.stop_hand()


@router.post("/position/{position_id}")
def set_position(position_id: int, request: Request):
    controller = request.app.state.controller
    return controller.move_to_position(position_id)

@router.post("/manual")
def manual_move(data: ManualMoveRequest, request: Request):
    controller = request.app.state.controller
    return controller.move_manual(data.command)


@router.get("/positions")
def get_positions(request: Request):
    controller = request.app.state.controller
    return controller.get_available_positions()