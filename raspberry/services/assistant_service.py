import os
from dotenv import load_dotenv
from openai import OpenAI


class AssistantService:
    def __init__(self):
        load_dotenv()

        api_key = os.getenv("OPENAI_API_KEY")

        if not api_key:
            raise ValueError("Falta la variable de entorno OPENAI_API_KEY")

        self.client = OpenAI(api_key=api_key)

    def ask(self, controller, message):
        try:
            status = controller.get_status()
        except Exception as e:
            status = {
                "error": "No se pudo obtener el estado del controlador",
                "detail": str(e)
            }

        system_prompt = """
Eres el asistente de una mano robótica controlada desde una Raspberry Pi.

Tu función es ayudar al usuario a manejar y entender el sistema de la mano robótica.
Responde siempre en español, de forma clara y breve.

Puedes explicar el estado de la mano, los modos disponibles y qué acciones puede hacer el usuario.
No inventes datos del hardware. Si no sabes algo, dilo claramente.

Estado actual del sistema:
{}
""".format(status)

        response = self.client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system",
                    "content": system_prompt
                },
                {
                    "role": "user",
                    "content": message
                }
            ]
        )

        return response.choices[0].message.content