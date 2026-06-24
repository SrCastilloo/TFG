from fastapi import APIRouter, Request
from pydantic import BaseModel, Field
from typing import Dict, List, Optional


router = APIRouter()


# Exponemos por API las acciones básicas de la mano

class ManualMoveRequest(BaseModel):
    command: Dict[str, str]


class FullGripRequest(BaseModel):
    max_seconds: float = 12.0
    poll_interval: float = 0.08
    consecutive_reads: int = 2
    ignored_sensors: List[str] = Field(default_factory=list)
    required_sensors: Optional[List[str]] = None
    start_from_open: bool = True
    open_wait_seconds: float = 3.0
    close_step: int = 30
    step_settle_seconds: float = 0.15
    pause_between_steps: float = 0.15


class SafeGripRequest(BaseModel):
    max_seconds: float = 12.0
    poll_interval: float = 0.08
    consecutive_reads: int = 2
    ignored_sensors: List[str] = Field(default_factory=list)
    start_from_open: bool = True
    open_wait_seconds: float = 3.0
    close_step: int = 20
    step_settle_seconds: float = 0.12
    pause_between_steps: float = 0.20


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
        ignored_sensors=data.ignored_sensors,
        start_from_open=data.start_from_open,
        open_wait_seconds=data.open_wait_seconds,
        close_step=data.close_step,
        step_settle_seconds=data.step_settle_seconds,
        pause_between_steps=data.pause_between_steps
    )

@router.post("/full-grip")
def full_grip(data: FullGripRequest, request: Request):
    controller = request.app.state.controller
    return controller.full_grip(
        max_seconds=data.max_seconds,
        poll_interval=data.poll_interval,
        consecutive_reads=data.consecutive_reads,
        ignored_sensors=data.ignored_sensors,
        required_sensors=data.required_sensors,
        start_from_open=data.start_from_open,
        open_wait_seconds=data.open_wait_seconds,
        close_step=data.close_step,
        step_settle_seconds=data.step_settle_seconds,
        pause_between_steps=data.pause_between_steps
    )

