from fastapi import APIRouter, Request
from api.schemas.assistant import AssistantChatRequest, AssistantChatResponse
from services.assistant_service import AssistantService

router = APIRouter()

assistant_service = AssistantService()


@router.post("/chat", response_model=AssistantChatResponse)
def assistant_chat(data: AssistantChatRequest, request: Request):
    controller = request.app.state.controller
    reply = assistant_service.ask(controller, data.message)

    return AssistantChatResponse(
        ok=True,
        reply=reply
    )