import re
import json
import unicodedata
from typing import Any, Dict, Optional

from services.ai_provider_service import AiProviderService

class AssistantService:
    """
    Asistente de la mano robótica.

    Tiene dos responsabilidades:
    1. Resolver acciones directas sobre el sistema: abrir mano, parar, mover posición,
       cambiar modos, detectar objeto, etc.
    2. Usar OpenAI para responder preguntas generales, pero siempre con contexto real
       del controlador.
    """

    POSITION_WORDS = {
        "cero": 0,
        "uno": 1,
        "una": 1,
        "dos": 2,
        "tres": 3,
        "cuatro": 4,
        "cinco": 5,
        "seis": 6,
        "siete": 7,
        "ocho": 8,
        "nueve": 9,
        "diez": 10,
    }

    CAMERA_OBJECT_MAPPING = {
        "person": 1,
        "cup": 2,
        "tv": 0,
        "chair": 4,
        "scissors": 5,
        "bottle": 6,
        "mouse": 7,
        "keyboard": 8,
    }

    VOICE_COMMAND_MAPPING = {
        "0x11": "uno / posición 1",
        "0x19": "dos / posición 2",
        "0x1d": "tres / posición 3",
        "0x1f": "cuatro / posición 4",
        "0x1": "cinco / posición 5",
        "0x0": "ruido / no mueve la mano",
    }

    def __init__(self):
        self.ai_provider_service = AiProviderService()

    def ask(
    self,
    controller,
    message: str,
    provider: str,
    api_key: str,
    model: Optional[str] = None,
    ) -> Dict[str, Any]:
        """
        Punto principal llamado desde /assistant/chat.

        1. Primero intenta resolver comandos reales sin consumir IA.
        2. Si no es una acción directa, llama al proveedor elegido por el usuario.
        """
        normalized = self._normalize(message)
        provider = (provider or "OPENAI").strip().upper()

        direct_reply = self._try_direct_response(controller, message, normalized)

        if direct_reply is not None:
            return {
            "ok": True,
            "reply": direct_reply,
            "provider": provider,
            "model": model,
            "used_ai": False,
            "prompt_tokens": 0,
            "completion_tokens": 0,
            "total_tokens": 0,
            "error": None,
            }

        return self._ask_ai_with_context(
            controller=controller,
            message=message,
            provider=provider,
            api_key=api_key,
            model=model,
        )

    # ---------------------------------------------------------------------
    # RESPUESTAS DIRECTAS / ACCIONES
    # ---------------------------------------------------------------------

    def _try_direct_response(self, controller, original_message: str, text: str) -> Optional[str]:
        """
        Detecta intenciones claras sin depender de OpenAI.
        Esto es más seguro para controlar hardware.
        """

        # Ayuda / capacidades
        if self._contains_any(text, ["que puedes hacer", "que sabes hacer", "ayuda", "comandos disponibles"]):
            return self._capabilities_answer(controller)

        # Estado general
        if self._contains_any(text, ["estado", "resumen", "disponible", "funciona", "como esta el sistema"]):
            return self._status_answer(controller)

        # Modo actual
        if self._contains_any(text, ["modo actual", "en que modo", "modo esta", "modo activo"]):
            return self._mode_answer(controller)

        # Simulación / real
        if self._contains_any(text, ["simulacion", "simulado", "modo real", "hardware real", "real o simulacion"]):
            return self._simulation_answer(controller)

        # Posición actual / última posición
        if self._contains_any(text, ["en que posicion", "posicion actual", "ultima posicion", "donde esta la mano"]):
            return self._position_answer(controller)

        # Listar posiciones
        if self._contains_any(text, ["que posiciones", "posiciones disponibles", "lista de posiciones"]):
            return self._positions_answer()

        # Abrir mano
        if self._is_open_hand_intent(text):
            return self._execute_action(
                action_name="Abrir mano",
                action=lambda: controller.open_hand()
            )

        # Parar mano / emergencia
        if self._is_stop_hand_intent(text):
            return self._execute_action(
                action_name="Parar mano",
                action=lambda: controller.stop_hand()
            )

        # Cambiar modo
        if self._is_mode_change_intent(text, "mano"):
            return self._execute_action(
                action_name="Activar modo mano",
                action=lambda: controller.set_mode_hand()
            )

        if self._is_mode_change_intent(text, "voz"):
            return self._execute_action(
                action_name="Activar modo voz",
                action=lambda: controller.set_mode_voice()
            )

        if self._is_mode_change_intent(text, "camara"):
            return self._execute_action(
                action_name="Activar modo cámara",
                action=lambda: controller.set_mode_camera()
            )

        # Cámara: detectar objeto y mover
        if self._is_camera_detect_and_move_intent(text):
            def detect_and_move():
                try:
                    controller.set_mode_camera()
                except Exception:
                    pass

                return controller.detect_object_and_move()

            return self._execute_action(
                action_name="Detectar objeto y mover mano",
                action=detect_and_move
            )

        # Cámara: solo detectar objeto
        if self._is_camera_detect_intent(text):
            def detect_object():
                try:
                    controller.set_mode_camera()
                except Exception:
                    pass

                return controller.detect_best_object()

            return self._execute_action(
                action_name="Detectar objeto",
                action=detect_object
            )

        # Mover a posición concreta
        if self._is_move_position_intent(text):
            position_id = self._extract_position_id(text)

            if position_id is None:
                return (
                    "Quieres mover la mano, pero no he detectado la posición. "
                    "Prueba con algo como: `mueve la mano a la posición 2`."
                )

            return self._execute_action(
                action_name=f"Mover mano a posición {position_id}",
                action=lambda: controller.move_to_position(position_id)
            )

        return None

    # ---------------------------------------------------------------------
    # RESPUESTAS INFORMATIVAS DIRECTAS
    # ---------------------------------------------------------------------

    def _capabilities_answer(self, controller) -> str:
        sim_text = "simulación" if self._is_simulation(controller) else "hardware real"

        return (
            f"Estoy conectado al sistema en modo **{sim_text}**.\n\n"
            "Puedo ayudarte con estas cosas:\n\n"
            "1. Consultar el estado del sistema.\n"
            "2. Decirte el modo actual: mano, voz, cámara o inicio.\n"
            "3. Indicar la última posición ordenada a la mano.\n"
            "4. Abrir la mano.\n"
            "5. Parar la mano.\n"
            "6. Mover la mano a una posición concreta.\n"
            "7. Cambiar entre modo mano, modo voz y modo cámara.\n"
            "8. Lanzar detección por cámara.\n"
            "9. Detectar un objeto con cámara y mover la mano según el objeto detectado.\n\n"
            "Ejemplos que puedes decirme:\n"
            "`abre la mano`, `para la mano`, `mueve a la posición 3`, "
            "`activa modo cámara`, `detecta un objeto y mueve la mano`, "
            "`en qué modo está el sistema`."
        )

    def _status_answer(self, controller) -> str:
        status = self._safe_get_status(controller)

        mode = status.get("mode", "desconocido")
        last_position = status.get("last_position_mapped")
        hand_status = status.get("hand_status")
        hand_target = status.get("hand_target")
        object_status = status.get("object_status")
        object_best_status = status.get("object_best_status")
        sim_text = "simulación" if self._is_simulation(controller) else "hardware real"

        lines = [
            "Estado actual del sistema:",
            f"- Ejecución: {sim_text}.",
            f"- Modo actual: {mode}.",
            f"- Última posición ordenada: {last_position if last_position is not None else 'ninguna todavía'}.",
        ]

        if hand_status is not None:
            lines.append(f"- Estado de la mano disponible: sí.")
        else:
            lines.append(f"- Estado de la mano disponible: no se ha podido leer ahora mismo.")

        if hand_target is not None:
            lines.append(f"- Objetivo/target de la mano disponible: sí.")

        if object_best_status is not None:
            lines.append(f"- Mejor detección de cámara: {object_best_status}.")
        elif object_status is not None:
            lines.append(f"- Estado de cámara: {object_status}.")
        else:
            lines.append("- Cámara: sin información reciente.")

        lines.append(
            "\nNota: la posición física exacta no siempre puede saberse solo por software; "
            "lo que sí conozco es la última posición que se ha enviado a la mano."
        )

        return "\n".join(lines)

    def _mode_answer(self, controller) -> str:
        status = self._safe_get_status(controller)
        mode = status.get("mode", "desconocido")

        return f"El sistema está actualmente en modo **{mode}**."

    def _simulation_answer(self, controller) -> str:
        if self._is_simulation(controller):
            return (
                "Ahora mismo el sistema está en **modo simulación**. "
                "Eso significa que el backend responde como si ejecutara las acciones, "
                "pero no está usando directamente el hardware real."
            )

        return (
            "Ahora mismo el sistema está en **modo real**, usando el hardware de la Raspberry. "
            "Las órdenes que se envíen pueden actuar sobre la mano robótica real."
        )

    def _position_answer(self, controller) -> str:
        status = self._safe_get_status(controller)
        last_position = status.get("last_position_mapped")
        hand_target = status.get("hand_target")

        if last_position is None:
            return (
                "Todavía no tengo registrada ninguna posición enviada desde el controlador. "
                "Puedo mover la mano si me dices, por ejemplo: `mueve a la posición 2`."
            )

        response = (
            f"La última posición ordenada a la mano es la **posición {last_position}**.\n\n"
            "Ojo: esto indica la última orden enviada, no una medición física perfecta de la posición real."
        )

        if hand_target is not None:
            response += f"\n\nTarget actual de la mano: `{hand_target}`"

        return response

    def _positions_answer(self) -> str:
        return (
            "El sistema puede mover la mano a posiciones predefinidas usando su identificador numérico.\n\n"
            "Por voz tienes comprobado:\n"
            "- Uno → posición 1\n"
            "- Dos → posición 2\n"
            "- Tres → posición 3\n"
            "- Cuatro → posición 4\n"
            "- Cinco → posición 5\n\n"
            "Por cámara, el mapeo actual es:\n"
            "- person → posición 1\n"
            "- cup → posición 2\n"
            "- tv → posición 0\n"
            "- chair → posición 4\n"
            "- scissors → posición 5\n"
            "- bottle → posición 6\n"
            "- mouse → posición 7\n"
            "- keyboard → posición 8\n\n"
            "También puedes pedirme: `mueve la mano a la posición 3`."
        )

    # ---------------------------------------------------------------------
    # OPENAI CON CONTEXTO
    # ---------------------------------------------------------------------

    def _ask_ai_with_context(
    self,
    controller,
    message: str,
    provider: str,
    api_key: str,
    model: Optional[str],
    ) -> Dict[str, Any]:
        status = self._safe_get_status(controller)
        simulation = self._is_simulation(controller)

        context = {
        "simulation": simulation,
        "execution_mode": "simulation" if simulation else "real_hardware",
        "controller_status": status,
        "available_controller_actions": [
            "open_hand()",
            "stop_hand()",
            "move_to_position(position_id)",
            "move_manual(command)",
            "set_mode_hand()",
            "set_mode_voice()",
            "set_mode_camera()",
            "detect_best_object()",
            "detect_object_and_move()",
            "refresh_hand_status()",
            "get_status()",
        ],
        "voice_command_mapping": self.VOICE_COMMAND_MAPPING,
        "camera_object_mapping": self.CAMERA_OBJECT_MAPPING,
        "important_note": (
            "La posición física exacta de la mano no siempre se puede conocer. "
            "last_position_mapped representa la última posición enviada por software."
        ),
        }

        system_prompt = f"""
        Eres el asistente de una mano robótica controlada desde una Raspberry Pi.

        Responde siempre en español, de forma clara y breve.
        No uses Markdown. No uses negritas con **.
        Responde en texto plano, con frases cortas y listas simples si hace falta.

        Tu objetivo es ayudar al usuario a entender y manejar el sistema.

        Información importante:
        - El sistema controla una mano robótica.
        - El backend se ejecuta en una Raspberry Pi.
        - La app Android se conecta a este backend.
        - Algunas acciones se ejecutan directamente sin usar IA por seguridad.
        - Si el usuario pide abrir la mano, parar, mover a una posición o cambiar de modo, normalmente el backend lo resuelve antes de llamar a la IA.
        - No inventes estados del hardware.
        - Si no tienes un dato, dilo claramente.
        - Si hablas de posiciones, recuerda que last_position_mapped es la última orden enviada, no una medición física perfecta.

        Contexto técnico actual:
        {json.dumps(context, ensure_ascii=False, indent=2)}
        """

        result = self.ai_provider_service.generate(
        provider=provider,
        api_key=api_key,
        model=model,
        system_prompt=system_prompt,
        user_message=message,
        )

        return {
        "ok": result.ok,
        "reply": result.reply,
        "provider": result.provider,
        "model": result.model,
        "used_ai": result.used_ai,
        "prompt_tokens": result.prompt_tokens,
        "completion_tokens": result.completion_tokens,
        "total_tokens": result.total_tokens,
        "error": result.error,
        }

    # ---------------------------------------------------------------------
    # EJECUCIÓN SEGURA DE ACCIONES
    # ---------------------------------------------------------------------

    def _execute_action(self, action_name: str, action) -> str:
        try:
            result = action()

            if not isinstance(result, dict):
                return (
                f"He ejecutado la acción: {action_name}.\n\n"
                "La orden se ha enviado, aunque el sistema no ha devuelto detalles adicionales."
                )

            ok = result.get("ok", False)

            if not ok:
                error = result.get("error") or result.get("message") or "No se ha indicado el motivo."
                return (
                f"No he podido completar la acción: {action_name}.\n\n"
                f"Motivo: {error}"
            )

            return self._format_action_result(action_name, result)

        except Exception as e:
            return (
            f"No he podido ejecutar la acción: {action_name}.\n\n"
            f"Error: {str(e)}"
        )
        

    def _format_action_result(self, action_name: str, result: dict) -> str:
        action = action_name.lower()

        # Abrir mano
        if "abrir mano" in action:
            return (
            "Mano abierta correctamente.\n\n"
            "La mano se ha colocado en su posición inicial."
        )

        # Parar mano
        if "parar mano" in action:
            return (
            "Movimiento detenido correctamente.\n\n"
            "He enviado la orden de parada a la mano."
        )

        # Mover a posición concreta
        if "mover mano a posición" in action:
            position_id = result.get("position_id")

            if position_id is not None:
                return (
                f"Mano movida correctamente.\n\n"
                f"Posición enviada: {position_id}"
            )

            return (
            "Orden de movimiento enviada correctamente.\n\n"
            "La mano debería desplazarse a la posición solicitada."
            )

        # Cambiar modo
        if "modo mano" in action:
            return (
            "Modo mano activado.\n\n"
            "Ahora puedes controlar la mano manualmente desde la app."
        )

        if "modo voz" in action:
            return (
            "Modo voz activado.\n\n"
            "Ahora puedes usar comandos de voz para mover la mano."
        )

        if "modo cámara" in action or "modo camara" in action:
            return (
            "Modo cámara activado.\n\n"
            "Ahora el sistema puede usar la cámara para reconocer objetos."
        )

        # Solo detectar objeto
        if "detectar objeto" in action and "mover" not in action:
            detected_object = result.get("object")
            quality = result.get("detection_quality", 0)

            if detected_object is None:
                return (
                "No he detectado ningún objeto con suficiente seguridad.\n\n"
                f"Calidad de detección: {self._format_quality(quality)}"
            )

            object_name = self._translate_object_name(detected_object)

            return (
            "Objeto detectado correctamente.\n\n"
            f"Objeto: {object_name}\n"
            f"Calidad de detección: {self._format_quality(quality)}"
            )

        # Detectar objeto y mover mano
        if "detectar objeto y mover" in action:
            detected_object = result.get("object")
            quality = result.get("detection_quality", 0)

            if detected_object is None:
                return (
                "No he detectado ningún objeto con suficiente seguridad.\n\n"
                "Por seguridad, no he movido la mano.\n\n"
                f"Calidad de detección: {self._format_quality(quality)}"
            )

            object_name = self._translate_object_name(detected_object)

            position_id = result.get("position_id")

            if position_id is None:
                position_id = self.CAMERA_OBJECT_MAPPING.get(detected_object)

            if position_id is not None:
                return (
                "Objeto detectado y movimiento enviado correctamente.\n\n"
                f"Objeto detectado: {object_name}\n"
                f"Calidad de detección: {self._format_quality(quality)}\n"
                f"Posición enviada a la mano: {position_id}"
            )

            return (
            "Objeto detectado correctamente, pero no tengo una posición asociada para mover la mano.\n\n"
            f"Objeto detectado: {object_name}\n"
            f"Calidad de detección: {self._format_quality(quality)}"
            )

        # Respuesta genérica si no entra en ningún caso anterior
        message = result.get("message")

        if message:
            return (
            f"Acción realizada correctamente.\n\n"
            f"{message}"
            )

        return (
        f"Acción realizada correctamente.\n\n"
        f"Acción: {action_name}"
        )
    



    def _format_quality(self, quality) -> str:
        try:
            quality_float = float(quality)
            return f"{quality_float:.1f}%"
        except Exception:
            return "desconocida"


    def _translate_object_name(self, object_name: str) -> str:
        translations = {
        "person": "persona",
        "cup": "taza / vaso",
        "tv": "televisión",
        "chair": "silla",
        "scissors": "tijeras",
        "bottle": "botella",
        "mouse": "ratón",
        "keyboard": "teclado",
    }

        return translations.get(object_name, object_name)





    

    # ---------------------------------------------------------------------
    # DETECCIÓN DE INTENCIONES
    # ---------------------------------------------------------------------

    def _is_open_hand_intent(self, text: str) -> bool:
        return self._contains_any(text, ["abre la mano", "abrir la mano", "mano abierta"])

    def _is_stop_hand_intent(self, text: str) -> bool:
        return self._contains_any(
            text,
            [
                "para la mano",
                "parar la mano",
                "deten la mano",
                "detener la mano",
                "stop",
                "emergencia",
                "parada de emergencia",
            ]
        )

    def _is_move_position_intent(self, text: str) -> bool:
        has_move_verb = self._contains_any(
            text,
            [
                "mueve",
                "mover",
                "manda",
                "enviar",
                "pon",
                "poner",
                "coloca",
                "lleva",
                "ve a",
            ]
        )

        has_position = self._contains_any(text, ["posicion", "pos"])

        return has_move_verb and has_position

    def _is_mode_change_intent(self, text: str, mode: str) -> bool:
        has_change_verb = self._contains_any(
            text,
            [
                "activa",
                "activar",
                "cambia",
                "cambiar",
                "pon",
                "poner",
                "modo",
            ]
        )

        return has_change_verb and self._contains_any(text, [f"modo {mode}", mode])

    def _is_camera_detect_intent(self, text: str) -> bool:
        has_camera = self._contains_any(text, ["camara", "objeto", "reconocimiento"])
        has_detect = self._contains_any(text, ["detecta", "detectar", "reconoce", "reconocer"])

        return has_camera and has_detect

    def _is_camera_detect_and_move_intent(self, text: str) -> bool:
        has_detect = self._is_camera_detect_intent(text)
        has_move = self._contains_any(text, ["mueve", "mover", "agarra", "agarre", "posicion"])

        return has_detect and has_move

    def _extract_position_id(self, text: str) -> Optional[int]:
        # Primero busca número: posición 3, pos 2, etc.
        match = re.search(r"(?:posicion|pos)\s*(\d+)", text)
        if match:
            return int(match.group(1))

        # Luego busca palabras: posición tres, posición dos, etc.
        for word, value in self.POSITION_WORDS.items():
            if re.search(rf"(?:posicion|pos)\s+{word}\b", text):
                return value

        # Caso: "mueve la mano al tres"
        for word, value in self.POSITION_WORDS.items():
            if re.search(rf"\b{word}\b", text):
                return value

        return None

    # ---------------------------------------------------------------------
    # UTILIDADES
    # ---------------------------------------------------------------------

    def _safe_get_status(self, controller) -> Dict[str, Any]:
        try:
            return controller.get_status()
        except Exception as e:
            return {
                "ok": False,
                "error": "No se pudo obtener el estado del controlador",
                "detail": str(e),
            }

    def _is_simulation(self, controller) -> bool:
        return bool(getattr(controller, "simulation", False))

    def _normalize(self, text: str) -> str:
        text = text.lower().strip()
        text = unicodedata.normalize("NFD", text)
        text = "".join(char for char in text if unicodedata.category(char) != "Mn")
        return text

    def _contains_any(self, text: str, options) -> bool:
        return any(option in text for option in options)