from fastapi import APIRouter, Request

router = APIRouter()


@router.get("/status")
def get_capacitive_status(request: Request):
    controller = request.app.state.controller
    return controller.get_capacitive_status()


@router.post("/refresh")
def refresh_capacitive_status(request: Request):
    controller = request.app.state.controller
    return controller.refresh_capacitive_status()