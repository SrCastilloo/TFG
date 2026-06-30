import json
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Optional

from openai import OpenAI


@dataclass
class AiProviderResult:
    ok: bool
    reply: str
    provider: str
    model: str
    used_ai: bool
    prompt_tokens: Optional[int] = None
    completion_tokens: Optional[int] = None
    total_tokens: Optional[int] = None
    error: Optional[str] = None


class AiProviderService:
    """
    Servicio común para llamar a proveedores de IA.

    No guarda claves.
    Recibe la API key en cada petición desde Android.
    """

    DEFAULT_OPENAI_MODEL = "gpt-4o-mini"
    DEFAULT_GEMINI_MODEL = "gemini-1.5-flash"

    def generate(
        self,
        provider: str,
        api_key: str,
        model: Optional[str],
        system_prompt: str,
        user_message: str,
    ) -> AiProviderResult:
        provider = self._normalize_provider(provider)

        if not api_key or not api_key.strip():
            return AiProviderResult(
                ok=False,
                reply="Falta la API key del usuario.",
                provider=provider,
                model=model or "",
                used_ai=False,
                error="missing_api_key",
            )

        if provider == "OPENAI":
            return self._generate_openai(
                api_key=api_key.strip(),
                model=(model or self.DEFAULT_OPENAI_MODEL).strip(),
                system_prompt=system_prompt,
                user_message=user_message,
            )

        if provider == "GEMINI":
            return self._generate_gemini(
                api_key=api_key.strip(),
                model=(model or self.DEFAULT_GEMINI_MODEL).strip(),
                system_prompt=system_prompt,
                user_message=user_message,
            )

        return AiProviderResult(
            ok=False,
            reply=f"Proveedor de IA no soportado: {provider}",
            provider=provider,
            model=model or "",
            used_ai=False,
            error="unsupported_provider",
        )

    def _generate_openai(
        self,
        api_key: str,
        model: str,
        system_prompt: str,
        user_message: str,
    ) -> AiProviderResult:
        try:
            client = OpenAI(api_key=api_key)

            response = client.chat.completions.create(
                model=model,
                temperature=0.2,
                messages=[
                    {
                        "role": "system",
                        "content": system_prompt,
                    },
                    {
                        "role": "user",
                        "content": user_message,
                    },
                ],
            )

            reply = response.choices[0].message.content or ""

            usage = getattr(response, "usage", None)

            return AiProviderResult(
                ok=True,
                reply=reply,
                provider="OPENAI",
                model=model,
                used_ai=True,
                prompt_tokens=getattr(usage, "prompt_tokens", None),
                completion_tokens=getattr(usage, "completion_tokens", None),
                total_tokens=getattr(usage, "total_tokens", None),
                error=None,
            )

        except Exception as e:
            return AiProviderResult(
                ok=False,
                reply=(
                    "No he podido contactar con OpenAI usando la clave configurada. "
                    "Revisa que la API key sea correcta y que tenga saldo/permisos."
                ),
                provider="OPENAI",
                model=model,
                used_ai=True,
                error=str(e),
            )

    def _generate_gemini(
        self,
        api_key: str,
        model: str,
        system_prompt: str,
        user_message: str,
    ) -> AiProviderResult:
        try:
            encoded_model = urllib.parse.quote(model, safe="")
            url = (
                "https://generativelanguage.googleapis.com/v1beta/models/"
                f"{encoded_model}:generateContent"
                f"?key={urllib.parse.quote(api_key, safe='')}"
            )

            payload = {
                "systemInstruction": {
                    "parts": [
                        {
                            "text": system_prompt,
                        }
                    ]
                },
                "contents": [
                    {
                        "role": "user",
                        "parts": [
                            {
                                "text": user_message,
                            }
                        ],
                    }
                ],
                "generationConfig": {
                    "temperature": 0.2,
                },
            }

            body = json.dumps(payload).encode("utf-8")

            request = urllib.request.Request(
                url=url,
                data=body,
                headers={
                    "Content-Type": "application/json",
                },
                method="POST",
            )

            with urllib.request.urlopen(request, timeout=45) as response:
                raw = response.read().decode("utf-8")
                data = json.loads(raw)

            reply = self._extract_gemini_text(data)

            usage = data.get("usageMetadata", {}) or {}

            return AiProviderResult(
                ok=True,
                reply=reply,
                provider="GEMINI",
                model=model,
                used_ai=True,
                prompt_tokens=usage.get("promptTokenCount"),
                completion_tokens=usage.get("candidatesTokenCount"),
                total_tokens=usage.get("totalTokenCount"),
                error=None,
            )

        except urllib.error.HTTPError as e:
            error_body = ""

            try:
                error_body = e.read().decode("utf-8")
            except Exception:
                error_body = str(e)

            return AiProviderResult(
                ok=False,
                reply=(
                    "No he podido contactar con Gemini usando la clave configurada. "
                    "Revisa que la API key sea correcta y que el modelo exista."
                ),
                provider="GEMINI",
                model=model,
                used_ai=True,
                error=error_body,
            )

        except Exception as e:
            return AiProviderResult(
                ok=False,
                reply=(
                    "No he podido contactar con Gemini ahora mismo. "
                    "Revisa la conexión, la API key y el modelo configurado."
                ),
                provider="GEMINI",
                model=model,
                used_ai=True,
                error=str(e),
            )

    def _extract_gemini_text(self, data: dict) -> str:
        candidates = data.get("candidates") or []

        if not candidates:
            return "Gemini no ha devuelto ningún texto."

        content = candidates[0].get("content") or {}
        parts = content.get("parts") or []

        texts = []

        for part in parts:
            text = part.get("text")

            if text:
                texts.append(text)

        if not texts:
            return "Gemini no ha devuelto contenido de texto."

        return "\n".join(texts).strip()

    def _normalize_provider(self, provider: str) -> str:
        value = (provider or "").strip().upper()

        if value in ["OPENAI", "CHATGPT"]:
            return "OPENAI"

        if value in ["GEMINI", "GOOGLE"]:
            return "GEMINI"

        return value