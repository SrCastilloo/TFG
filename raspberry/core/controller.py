from pathlib import Path
from typing import Any, Dict, Optional

from core.state import SystemMode
from hardware.handControl import HandControl
from hardware.objectRec import ObjectRec


class HandSystemController:
    """
    Controlador principal del nuevo backend.

    Su responsabilidad es:
    - mantener el modo actual del sistema
    - exponer operaciones de alto nivel para la mano
    - exponer operaciones de alto nivel para la cámara
    - devolver un estado resumido del sistema

    IMPORTANTE:
    Esta clase no habla con Android directamente.
    Android hablará con la API, y la API usará esta clase.
    """

    # Mapeo inicial objeto -> posición predefinida de la mano
    # Esto sale del comportamiento ya existente en controlV3_1.py
    OBJECT_POSITION_MAPPING: Dict[str, int] = {
        "person": 1,
        "cup": 2,
        "tv": 0,
        "chair": 4,
        "scissors": 5,
        "bottle": 6,
        "mouse": 7,
        "keyboard": 8,
    }

    def __init__(self, config_path: str = "config/config.ini") -> None:
        self.config_path = str(Path(config_path))
        self.mode: SystemMode = SystemMode.INIT
        self.last_position_mapped: Optional[int] = None

        # Subsistemas mínimos para arrancar el backend nuevo
        self.hand = HandControl(self.config_path)
        self.object_rec = ObjectRec("object_rec", self.config_path)

    # -------------------------------------------------------------------------
    # MODOS
    # -------------------------------------------------------------------------

    def set_mode_hand(self) -> Dict[str, Any]:
        self.mode = SystemMode.HAND
        return {
            "ok": True,
            "mode": self.mode.value,
            "message": "Modo mano activado."
        }

    def set_mode_voice(self) -> Dict[str, Any]:
        self.mode = SystemMode.VOICE
        return {
            "ok": True,
            "mode": self.mode.value,
            "message": "Modo voz activado."
        }

    def set_mode_camera(self) -> Dict[str, Any]:
        self.mode = SystemMode.CAMERA
        return {
            "ok": True,
            "mode": self.mode.value,
            "message": "Modo cámara activado."
        }

    # -------------------------------------------------------------------------
    # MANO
    # -------------------------------------------------------------------------

    def open_hand(self) -> Dict[str, Any]:
        """
        Abre la mano usando el mismo esquema que el código legado.
        """
        command = {
            "ring": "close",
            "middle": "open",
            "index": "open",
            "thumb0": "open",
            "thumb1": "open",
        }
        self.hand.move_open_close(command)
        return {
            "ok": True,
            "message": "Orden de apertura enviada a la mano.",
            "command": command,
        }

    def stop_hand(self) -> Dict[str, Any]:
        """
        Para todos los dedos.
        """
        self.hand.stop_movement({
            "ring": "stop",
            "middle": "stop",
            "index": "stop",
            "thumb0": "stop",
            "thumb1": "stop",
        })
        return {
            "ok": True,
            "message": "Orden de parada enviada a la mano."
        }

    def move_to_position(self, position_id: int) -> Dict[str, Any]:
        """
        Mueve la mano a una posición predefinida del config.ini.
        """
        self.hand.move_position(position_id)
        self.last_position_mapped = position_id
        return {
            "ok": True,
            "message": f"Orden enviada para mover la mano a la posición {position_id}.",
            "position_id": position_id,
        }

    def move_manual(self, command: Dict[str, str]) -> Dict[str, Any]:
        """
        Movimiento manual simple.
        Ejemplo:
        {
            "ring": "open",
            "index": "close",
            "thumb0": "open"
        }
        """
        self.hand.move_open_close(command)
        return {
            "ok": True,
            "message": "Orden manual enviada a la mano.",
            "command": command,
        }

    # -------------------------------------------------------------------------
    # CÁMARA
    # -------------------------------------------------------------------------

    def detect_best_object(self) -> Dict[str, Any]:
        """
        Lanza la detección de objetos y devuelve el mejor objeto detectado.
        """
        self.object_rec.clear_status()
        self.object_rec.detect_object()

        best_object = self.object_rec.get_best_object()
        best_quality = self.object_rec.get_best_detection_quality()

        return {
            "ok": True,
            "mode": self.mode.value,
            "object": best_object,
            "detection_quality": best_quality,
        }

    def detect_object_and_move(self) -> Dict[str, Any]:
        """
        Detecta el mejor objeto y, si existe un mapeo, mueve la mano
        a la posición asociada.
        """
        detection = self.detect_best_object()

        best_object = detection["object"]
        best_quality = detection["detection_quality"]

        if best_object is None:
            return {
                "ok": False,
                "mode": self.mode.value,
                "message": "No se ha detectado ningún objeto.",
                "object": None,
                "detection_quality": best_quality,
            }

        if best_object not in self.OBJECT_POSITION_MAPPING:
            return {
                "ok": False,
                "mode": self.mode.value,
                "message": f"El objeto '{best_object}' no tiene una posición asociada.",
                "object": best_object,
                "detection_quality": best_quality,
            }

        target_position = self.OBJECT_POSITION_MAPPING[best_object]

        # Igual que en el legado: abrir antes de mover a una nueva posición
        self.open_hand()
        self.move_to_position(target_position)

        return {
            "ok": True,
            "mode": self.mode.value,
            "message": "Objeto detectado y orden de movimiento enviada.",
            "object": best_object,
            "detection_quality": best_quality,
            "target_position": target_position,
        }

    # -------------------------------------------------------------------------
    # ESTADO
    # -------------------------------------------------------------------------

    def refresh_hand_status(self) -> Dict[str, Any]:
        """
        Fuerza una lectura del estado actual de la mano.
        """
        self.hand.update_status()
        return {
            "ok": True,
            "hand_status": self.hand.get_status(),
            "hand_target": self.hand.get_target(),
        }

    def get_status(self) -> Dict[str, Any]:
        """
        Devuelve un resumen del estado del sistema.
        """
        hand_status = None
        hand_target = None
        object_status = None
        object_best_status = None

        try:
            hand_status = self.hand.get_status()
            hand_target = self.hand.get_target()
        except Exception:
            pass

        try:
            object_status = self.object_rec.get_status()
            object_best_status = self.object_rec.get_best_status()
        except Exception:
            pass

        return {
            "ok": True,
            "mode": self.mode.value,
            "last_position_mapped": self.last_position_mapped,
            "hand_status": hand_status,
            "hand_target": hand_target,
            "object_status": object_status,
            "object_best_status": object_best_status,
        }

    # -------------------------------------------------------------------------
    # CIERRE DEL SISTEMA
    # -------------------------------------------------------------------------

    def shutdown(self) -> Dict[str, Any]:
        """
        Libera recursos del sistema.
        """
        try:
            self.stop_hand()
        except Exception:
            pass

        try:
            self.hand.close_i2c()
        except Exception:
            pass

        return {
            "ok": True,
            "message": "Sistema detenido y recursos liberados."
        }