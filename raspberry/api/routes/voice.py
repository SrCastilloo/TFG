from fastapi import APIRouter, Request

router = APIRouter()

@router.post("/read")
def read_voice(request: Request):
    controller = request.app.state.controller
    return controller.read_voice_command()

@router.post("/execute")
def execute_voice(request: Request):
    controller = request.app.state.controller
    return controller.execute_voice_command()