from enum import Enum
from typing import Optional

from pydantic import BaseModel


class AssistantProvider(str, Enum):
    OPENAI = "OPENAI"
    GEMINI = "GEMINI"


class AssistantChatRequest(BaseModel):
    message: str

    # Proveedor elegido por el usuario en Android.
    provider: AssistantProvider = AssistantProvider.OPENAI

    # API key propia del usuario.
    api_key: str

    # Modelo elegido por el usuario.
    model: Optional[str] = None


class AssistantChatResponse(BaseModel):
    ok: bool
    reply: str

    provider: Optional[str] = None
    model: Optional[str] = None

    # True si realmente se ha llamado a OpenAI/Gemini.
    # False si era una acción directa del sistema, como abrir mano o consultar estado.
    used_ai: bool = False

    prompt_tokens: Optional[int] = None
    completion_tokens: Optional[int] = None
    total_tokens: Optional[int] = None

    error: Optional[str] = None