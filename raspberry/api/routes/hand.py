from fastapi import APIRouter, Request
from pydantic import BaseModel, Field
from typing import Dict, List


router = APIRouter()


# Exponemos por API las acciones básicas de la mano

class ManualMoveRequest(BaseModel):
    command: Dict[str, str]


class SafeGripRequest(BaseModel):
    max_seconds: float = 4.0
    poll_interval: float = 0.15
    consecutive_reads: int = 2
    ignored_sensors: List[str] = Field(default_factory=list)

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


@router.post("/safe-grip")
def safe_grip(data: SafeGripRequest, request: Request):
    controller = request.app.state.controller
    return controller.safe_grip(
        max_seconds=data.max_seconds,
        poll_interval=data.poll_interval,
        consecutive_reads=data.consecutive_reads,
        ignored_sensors=data.ignored_sensors
    )