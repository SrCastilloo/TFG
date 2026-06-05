import os
from dotenv import load_dotenv
from openai import OpenAI


class AssistantService:
    def __init__(self) -> None:
        load_dotenv()
        api_key = os.getenv("OPENAI_API_KEY")

        if not api_key:
            raise ValueError("Falta la variable de entorno OPENAI_API_KEY")

        self.client = OpenAI(api_key=api_key)

    def build_context(self, controller) -> str:
        system_info = controller.get_status()
        positions = controller.get_available_positions()

        return f"""
Eres el asistente de una app Android para controlar una mano robótica.

Tu trabajo es responder de forma clara, breve y útil preguntas sobre:
- el funcionamiento de la app
- los modos del sistema
- el estado actual del backend
- las posiciones disponibles
- si el sistema está en simulación o en modo real

Estado actual del sistema:
{system_info}

Posiciones disponibles:
{positions}

Reglas:
- Si el sistema está en simulación, dilo claramente.
- No inventes funciones que la app no tenga.
- Si preguntan algo que no sabes, dilo con honestidad.
- Responde siempre en español.
"""

    def ask(self, controller, user_message: str) -> str:
        context = self.build_context(controller)

        response = self.client.responses.create(
            model="gpt-5.4-mini",
            instructions=context,
            input=user_message
        )

        return response.output_text