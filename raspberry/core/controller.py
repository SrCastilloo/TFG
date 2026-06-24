from pathlib import Path
from typing import Any, Dict, Optional, List

from core.state import SystemMode
from configparser import ConfigParser
import re

import time
import RPi.GPIO as GPIO
from hardware.cmdDevice import CmdDevice
from hardware.capacitiveControl import CapacitiveControl
import threading
from hardware.speakerControl import SpeakerControl

"""
Resumen de lo que hace cada atributo que tiene el controlador:
- config_path: ruta al archivo de configuración (config.ini)
- mode: modo actual del sistema (init, hand, voice, camera)
- last_position_mapped: última posición mapeada a un objeto detectado (si existe)
- hand: instancia del subsistema de control de la mano
- object_rec: instancia del subsistema de reconocimiento de objetos
- speech: instancia del subsistema de voz
- capacitive: instancia del subsistema de sensores capacitivos
- OBJECT_POSITION_MAPPING: mapeo de objetos detectados a posiciones predefinidas de la mano

Responsabilidades de la clase:
- Mantener el modo actual del sistema
- Exponer operaciones de alto nivel para la mano
- Exponer operaciones de alto nivel para la cámara
- Exponer operaciones de alto nivel para voz
- Exponer operaciones de lectura de sensores capacitivos
- Devolver un estado resumido del sistema
- Manejar el cierre del sistema liberando recursos
"""


class HandSystemController:
    """
    Controlador principal del nuevo backend.

    Su responsabilidad es:
    - mantener el modo actual del sistema
    - exponer operaciones de alto nivel para la mano
    - exponer operaciones de alto nivel para la cámara
    - exponer operaciones de alto nivel para voz
    - exponer lectura de sensores capacitivos
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

    # Mapeo inicial voz -> posición predefinida de la mano
    VOICE_POSITION_MAPPING: Dict[str, int] = {
        "0x11": 1,  # uno
        "0x19": 2,  # dos
        "0x1d": 3,  # tres
        "0x1f": 4,  # cuatro
        "0x1": 5,   # cinco
        "0x0": 0    # ruido / sin comando útil
    }

    def __init__(self, config_path: str = "config/config.ini", simulation: bool = False) -> None:
        self.config_path = str(Path(config_path))
        self.mode: SystemMode = SystemMode.INIT
        self.last_position_mapped: Optional[int] = None
        self.simulation = simulation

        # Atributos comunes para evitar errores aunque algún subsistema falle
        self.hand = None
        self.object_rec = None
        self.speech = None
        self.capacitive = None

        self.capacitive_available = False

        self.pin_wkup_speech = None
        self.pin_int_speech = None

        if not self.simulation:
            # Subsistemas mínimos para arrancar el backend nuevo
            from hardware.handControl import HandControl
            from hardware.objectRec import ObjectRec

            self.hand = HandControl(self.config_path)  # Inicializa la mano con el config.ini
            self.object_rec = ObjectRec("object_rec", self.config_path)  # Inicializa la cámara con el config.ini

            # Subsistema de sensores capacitivos
            try:
                self.capacitive = CapacitiveControl("cc_hand", self.config_path)
                self.capacitive_available = True
                print("Sensores capacitivos inicializados correctamente.")
            except Exception as e:
                self.capacitive = None
                self.capacitive_available = False
                print(f"No se pudieron inicializar los sensores capacitivos: {e}")

            try:
                if not self.simulation:
                    self.speaker = SpeakerControl("speaker_control", self.config_path)
                    self.speaker_available = True
                    print("Altavoz inicializado correctamente.")
                else:
                    self.speaker = None
                    self.speaker_available = False

            except Exception as e:
                self.speaker = None
                self.speaker_available = False
                print(f"No se pudo inicializar el altavoz: {e}")

            # Subsistema de voz
            self.speech = CmdDevice("cmd_speech", self.config_path)

            config = ConfigParser()
            config.read(self.config_path)

            self.pin_wkup_speech = config.getint("GPIO", "pin_wkup_speech")
            self.pin_int_speech = config.getint("GPIO", "pin_int_speech")

            GPIO.setwarnings(False)
            GPIO.setmode(GPIO.BCM)
            GPIO.setup(self.pin_wkup_speech, GPIO.OUT, initial=GPIO.LOW)
            GPIO.setup(self.pin_int_speech, GPIO.IN)

    # -------------------------------------------------------------------------
    # MODOS
    # -------------------------------------------------------------------------

    def set_mode_hand(self) -> Dict[str, Any]:
        self.mode = SystemMode.HAND
        self._play_sound_async("hand")
        return {
            "ok": True,
            "mode": self.mode.value,
            "message": "Modo mano activado."
        }

    def set_mode_voice(self) -> Dict[str, Any]:
        self.mode = SystemMode.VOICE
        self._play_sound_async("voice")
        return {
            "ok": True,
            "mode": self.mode.value,
            "message": "Modo voz activado."
        }

    def set_mode_camera(self) -> Dict[str, Any]:
        self.mode = SystemMode.CAMERA
        self._play_sound_async("camera")
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
        Abre la mano moviéndola a la posición 0 del config.ini.
        """
        if self.simulation:
            return {
                "ok": True,
                "message": "Simulación: orden enviada para abrir la mano en la posición 0.",
                "position_id": 0,
            }

        self.last_position_mapped = 0
        self.hand.move_position(0)

        return {
            "ok": True,
            "message": "Orden enviada para abrir la mano en la posición 0.",
            "position_id": 0,
        }

    def stop_hand(self) -> Dict[str, Any]:
        """
        Para todos los dedos.
        """
        command = {
            "ring": "stop",
            "middle": "stop",
            "index": "stop",
            "thumb0": "stop",
            "thumb1": "stop",
        }

        if self.simulation:
            return {
                "ok": True,
                "message": "Simulación: orden de parada enviada a la mano.",
                "command": command,
            }

        self.hand.stop_movement(command)

        return {
            "ok": True,
            "message": "Orden de parada enviada a la mano.",
            "command": command,
        }

    def move_to_position(self, position_id: int) -> Dict[str, Any]:
        """
        Mueve la mano a una posición predefinida del config.ini.
        """
        self.last_position_mapped = position_id

        if self.simulation:
            return {
                "ok": True,
                "message": f"Simulación: orden enviada para mover la mano a la posición {position_id}.",
                "position_id": position_id,
            }

        self.hand.move_position(position_id)

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
        if self.simulation:
            return {
                "ok": True,
                "message": "Simulación: orden manual enviada a la mano.",
                "command": command,
            }

        self.hand.move_open_close(command)

        return {
            "ok": True,
            "message": "Orden manual enviada a la mano.",
            "command": command,
        }

    def get_available_positions(self) -> Dict[str, Any]:
        """
        Devuelve las posiciones predefinidas disponibles en el config.ini.
        """
        config_file = Path(self.config_path)

        if not config_file.exists():
            return {
                "ok": False,
                "message": f"No existe el fichero de configuración: {config_file}",
                "positions": [],
            }

        config = ConfigParser()
        read_files = config.read(config_file, encoding="utf-8")

        if not read_files:
            return {
                "ok": False,
                "message": f"No se pudo leer el fichero de configuración: {config_file}",
                "positions": [],
            }

        if "positions" not in config.sections():
            return {
                "ok": False,
                "message": f"Se ha leído el config.ini, pero no existe la sección 'positions'. Secciones encontradas: {config.sections()}",
                "positions": [],
            }

        position_ids = set()

        for key in config["positions"].keys():
            match = re.match(r"p(\d+)_", key)
            if match:
                position_ids.add(int(match.group(1)))

        sorted_positions = sorted(position_ids)

        return {
            "ok": True,
            "positions": sorted_positions,
            "count": len(sorted_positions),
        }
    

    def safe_grip(
        self,
        max_seconds: float = 12.0,
        poll_interval: float = 0.08,
        consecutive_reads: int = 2,
        ignored_sensors: Optional[List[str]] = None,
        start_from_open: bool = True,
        open_wait_seconds: float = 3.0,
        close_step: int = 20,
        step_settle_seconds: float = 0.12,
        pause_between_steps: float = 0.20
    ) -> Dict[str, Any]:
        """
        Ejecuta un agarre seguro progresivo.

        La mano parte de una posición abierta y va cerrando poco a poco.
        Se detiene automáticamente cuando cualquier sensor capacitivo válido
        detecta contacto.

        Permite ignorar sensores defectuosos:
        ignored_sensors=["ring"]
        """

        max_seconds = max(0.5, min(float(max_seconds), 25.0))
        poll_interval = max(0.03, min(float(poll_interval), 0.5))
        consecutive_reads = max(1, min(int(consecutive_reads), 5))
        open_wait_seconds = max(0.0, min(float(open_wait_seconds), 8.0))
        close_step = max(5, min(int(close_step), 300))
        step_settle_seconds = max(0.05, min(float(step_settle_seconds), 1.0))
        pause_between_steps = max(0.03, min(float(pause_between_steps), 1.0))

        ignored_sensors = ignored_sensors or []
        ignored_set = set(sensor.strip().lower() for sensor in ignored_sensors)

        total_start_time = time.monotonic()

        step_command_template = {
            "ring": "step_close",
            "middle": "step_close",
            "index": "step_close",
            "thumb0": "step_close",
            "thumb1": "step_close",
        }

        if self.simulation:
            simulated_capacitive = {
                "ok": True,
                "available": True,
                "simulation": True,
                "status": {
                    "pinky": 140,
                    "ring": 2500,
                    "middle": 70,
                    "index": 2200,
                    "thumb": 25,
                    "palm": 240,
                },
                "heights": {
                    "pinky": 1700,
                    "ring": 1700,
                    "middle": 1200,
                    "index": 1700,
                    "thumb": 1700,
                    "palm": 1700,
                },
                "contacts": {
                    "pinky": False,
                    "ring": True,
                    "middle": False,
                    "index": True,
                    "thumb": False,
                    "palm": False,
                },
                "contact_count": 2,
                "message": "Simulación: contacto detectado.",
            }

            filtered_contacts = self._filter_ignored_contacts(
                simulated_capacitive["contacts"],
                ignored_set
            )

            contact_count = sum(1 for value in filtered_contacts.values() if value)
            contact_sensor = self._first_contact_sensor(filtered_contacts)

            return {
                "ok": True,
                "message": "Simulación: agarre seguro progresivo detenido al detectar contacto.",
                "moved": True,
                "stopped": True,
                "contact_detected": contact_count > 0,
                "reason": "contact_detected" if contact_count > 0 else "timeout",
                "elapsed_seconds": 1.2,
                "contact_sensor": contact_sensor,
                "contact_count": contact_count,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": 4,
                "close_step": close_step,
                "command": step_command_template,
                "capacitive": simulated_capacitive,
            }

        if self.hand is None:
            return {
                "ok": False,
                "message": "No se puede ejecutar agarre seguro: la mano no está inicializada.",
                "moved": False,
                "stopped": False,
                "contact_detected": False,
                "reason": "hand_not_available",
                "elapsed_seconds": 0,
                "contact_sensor": None,
                "contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": 0,
                "close_step": close_step,
                "command": step_command_template,
                "capacitive": None,
            }

        if not self.capacitive_available or self.capacitive is None:
            return {
                "ok": False,
                "message": "No se puede ejecutar agarre seguro: sensores capacitivos no disponibles.",
                "moved": False,
                "stopped": False,
                "contact_detected": False,
                "reason": "capacitive_not_available",
                "elapsed_seconds": 0,
                "contact_sensor": None,
                "contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": 0,
                "close_step": close_step,
                "command": step_command_template,
                "capacitive": None,
            }

        last_capacitive = None
        step_count = 0

        try:
            if start_from_open:
                self.open_hand()
                time.sleep(open_wait_seconds)
                self.stop_hand()
                time.sleep(pause_between_steps)

            initial_capacitive = self.refresh_capacitive_status()
            last_capacitive = initial_capacitive

            raw_initial_contacts = initial_capacitive.get("contacts") or {}
            initial_contacts = self._filter_ignored_contacts(raw_initial_contacts, ignored_set)
            initial_contact_count = sum(1 for value in initial_contacts.values() if value)

            if initial_contact_count > 0:
                self.stop_hand()

                contact_sensor = self._first_contact_sensor(initial_contacts)

                return {
                    "ok": True,
                    "message": "Contacto detectado antes de iniciar el cierre. Mano detenida por seguridad.",
                    "moved": False,
                    "stopped": True,
                    "contact_detected": True,
                    "reason": "initial_contact",
                    "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                    "contact_sensor": contact_sensor,
                    "contact_count": initial_contact_count,
                    "ignored_sensors": sorted(list(ignored_set)),
                    "start_from_open": start_from_open,
                    "step_count": step_count,
                    "close_step": close_step,
                    "command": step_command_template,
                    "capacitive": initial_capacitive,
                }

            grip_start_time = time.monotonic()
            consecutive_contact_counter = 0

            while time.monotonic() - grip_start_time < max_seconds:
                step_count += 1

                # Cierre real progresivo:
                # cada vuelta solo avanza un paso pequeño, no va al tope.
                step_target = self.hand.step_close(close_step=close_step)

                time.sleep(step_settle_seconds)

                # Paramos tras cada paso para evitar cierre continuo.
                self.stop_hand()

                time.sleep(pause_between_steps)

                capacitive_status = self.refresh_capacitive_status()
                last_capacitive = capacitive_status

                raw_contacts = capacitive_status.get("contacts") or {}
                contacts = self._filter_ignored_contacts(raw_contacts, ignored_set)
                contact_count = sum(1 for value in contacts.values() if value)

                if contact_count > 0:
                    consecutive_contact_counter += 1

                    if consecutive_contact_counter >= consecutive_reads:
                        self.stop_hand()

                        contact_sensor = self._first_contact_sensor(contacts)

                        return {
                            "ok": True,
                            "message": "Contacto detectado. Mano detenida automáticamente.",
                            "moved": True,
                            "stopped": True,
                            "contact_detected": True,
                            "reason": "contact_detected",
                            "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                            "contact_sensor": contact_sensor,
                            "contact_count": contact_count,
                            "ignored_sensors": sorted(list(ignored_set)),
                            "start_from_open": start_from_open,
                            "step_count": step_count,
                            "close_step": close_step,
                            "last_step_target": step_target,
                            "command": step_command_template,
                            "capacitive": capacitive_status,
                        }
                else:
                    consecutive_contact_counter = 0

            self.stop_hand()

            return {
                "ok": True,
                "message": "Tiempo máximo alcanzado sin contacto. Mano detenida por seguridad.",
                "moved": True,
                "stopped": True,
                "contact_detected": False,
                "reason": "timeout",
                "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                "contact_sensor": None,
                "contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": step_count,
                "close_step": close_step,
                "command": step_command_template,
                "capacitive": last_capacitive,
            }

        except Exception as e:
            try:
                self.stop_hand()
            except Exception:
                pass

            return {
                "ok": False,
                "message": f"Error durante el agarre seguro: {str(e)}",
                "moved": True,
                "stopped": True,
                "contact_detected": False,
                "reason": "error",
                "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                "contact_sensor": None,
                "contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": step_count,
                "close_step": close_step,
                "command": step_command_template,
                "capacitive": last_capacitive,
            }
        
    def _first_contact_sensor(self, contacts: Dict[str, bool]) -> Optional[str]:
        """
        Devuelve el primer sensor que está detectando contacto.
        """
        for sensor, has_contact in contacts.items():
            if has_contact:
                return sensor

        return None
    
    
    def _filter_ignored_contacts(
        self,
        contacts: Dict[str, bool],
        ignored_sensors
    ) -> Dict[str, bool]:
        """
        Elimina de la lógica de contacto los sensores indicados como ignorados.
        """
        filtered_contacts = {}

        for sensor, has_contact in contacts.items():
            if sensor.lower() not in ignored_sensors:
                filtered_contacts[sensor] = has_contact

        return filtered_contacts
    
    
    
    def _build_full_grip_progress(
        self,
        contacts: Dict[str, bool],
        ignored_sensors,
        required_sensors: Optional[List[str]] = None
    ) -> Dict[str, Any]:
        """
        Calcula el progreso del agarre completo:
        - sensores activos
        - sensores que ya detectan contacto
        - sensores que faltan
        - si todos los sensores válidos detectan contacto
        """

        normalized_contacts = {}

        for sensor, has_contact in contacts.items():
            normalized_contacts[sensor.lower()] = bool(has_contact)

        if required_sensors is None:
            active_sensors = [
                sensor
                for sensor in normalized_contacts.keys()
                if sensor not in ignored_sensors
            ]
        else:
            active_sensors = [
                sensor
                for sensor in required_sensors
                if sensor not in ignored_sensors
            ]

        contact_sensors = []
        missing_sensors = []

        for sensor in active_sensors:
            if normalized_contacts.get(sensor, False):
                contact_sensors.append(sensor)
            else:
                missing_sensors.append(sensor)

        required_contact_count = len(active_sensors)
        contact_count = len(contact_sensors)

        return {
            "active_sensors": active_sensors,
            "contact_sensors": contact_sensors,
            "missing_sensors": missing_sensors,
            "contact_count": contact_count,
            "required_contact_count": required_contact_count,
            "all_contacts_detected": required_contact_count > 0 and contact_count == required_contact_count,
        }
    




    def full_grip(
        self,
        max_seconds: float = 12.0,
        poll_interval: float = 0.08,
        consecutive_reads: int = 2,
        ignored_sensors: Optional[List[str]] = None,
        required_sensors: Optional[List[str]] = None,
        start_from_open: bool = True,
        open_wait_seconds: float = 3.0,
        close_step: int = 30,
        step_settle_seconds: float = 0.15,
        pause_between_steps: float = 0.15
    ) -> Dict[str, Any]:
        """
        Ejecuta un agarre completo progresivo.

        La mano parte de posición abierta y va cerrando poco a poco por pasos.
        Solo finaliza correctamente cuando todos los sensores válidos detectan contacto.
        """

        max_seconds = max(0.5, min(float(max_seconds), 25.0))
        poll_interval = max(0.03, min(float(poll_interval), 0.5))
        consecutive_reads = max(1, min(int(consecutive_reads), 5))
        open_wait_seconds = max(0.0, min(float(open_wait_seconds), 8.0))
        close_step = max(5, min(int(close_step), 300))
        step_settle_seconds = max(0.05, min(float(step_settle_seconds), 1.0))
        pause_between_steps = max(0.03, min(float(pause_between_steps), 1.0))

        ignored_sensors = ignored_sensors or []
        ignored_set = set(sensor.strip().lower() for sensor in ignored_sensors)

        normalized_required_sensors = None
        if required_sensors is not None:
            normalized_required_sensors = [
                sensor.strip().lower()
                for sensor in required_sensors
                if sensor.strip()
            ]

        total_start_time = time.monotonic()

        close_command_template = {
            "ring": "step_close",
            "middle": "step_close",
            "index": "step_close",
            "thumb0": "step_close",
            "thumb1": "step_close",
        }

        if self.simulation:
            return {
                "ok": True,
                "message": "Simulación: agarre completo progresivo finalizado.",
                "moved": True,
                "stopped": True,
                "all_contacts_detected": True,
                "reason": "all_contacts_detected",
                "elapsed_seconds": 1.5,
                "active_sensors": ["pinky", "middle", "index", "thumb", "palm"],
                "contact_sensors": ["pinky", "middle", "index", "thumb", "palm"],
                "missing_sensors": [],
                "contact_count": 5,
                "required_contact_count": 5,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": 8,
                "close_step": close_step,
                "command": close_command_template,
                "capacitive": self.refresh_capacitive_status(),
            }

        if self.hand is None:
            return {
                "ok": False,
                "message": "No se puede ejecutar agarre completo: la mano no está inicializada.",
                "moved": False,
                "stopped": False,
                "all_contacts_detected": False,
                "reason": "hand_not_available",
                "elapsed_seconds": 0,
                "active_sensors": [],
                "contact_sensors": [],
                "missing_sensors": [],
                "contact_count": 0,
                "required_contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": 0,
                "close_step": close_step,
                "command": close_command_template,
                "capacitive": None,
            }

        if not self.capacitive_available or self.capacitive is None:
            return {
                "ok": False,
                "message": "No se puede ejecutar agarre completo: sensores capacitivos no disponibles.",
                "moved": False,
                "stopped": False,
                "all_contacts_detected": False,
                "reason": "capacitive_not_available",
                "elapsed_seconds": 0,
                "active_sensors": [],
                "contact_sensors": [],
                "missing_sensors": [],
                "contact_count": 0,
                "required_contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": 0,
                "close_step": close_step,
                "command": close_command_template,
                "capacitive": None,
            }

        last_capacitive = None
        step_count = 0

        try:
            if start_from_open:
                self.open_hand()
                time.sleep(open_wait_seconds)
                self.stop_hand()
                time.sleep(pause_between_steps)

            initial_capacitive = self.refresh_capacitive_status()
            last_capacitive = initial_capacitive

            initial_contacts = initial_capacitive.get("contacts") or {}
            initial_progress = self._build_full_grip_progress(
                contacts=initial_contacts,
                ignored_sensors=ignored_set,
                required_sensors=normalized_required_sensors
            )

            if initial_progress["required_contact_count"] == 0:
                self.stop_hand()

                return {
                    "ok": False,
                    "message": "No hay sensores válidos para ejecutar el agarre completo.",
                    "moved": False,
                    "stopped": True,
                    "all_contacts_detected": False,
                    "reason": "no_valid_sensors",
                    "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                    "active_sensors": [],
                    "contact_sensors": [],
                    "missing_sensors": [],
                    "contact_count": 0,
                    "required_contact_count": 0,
                    "ignored_sensors": sorted(list(ignored_set)),
                    "start_from_open": start_from_open,
                    "step_count": step_count,
                    "close_step": close_step,
                    "command": close_command_template,
                    "capacitive": initial_capacitive,
                }

            if initial_progress["all_contacts_detected"]:
                self.stop_hand()

                return {
                    "ok": True,
                    "message": "Todos los sensores válidos ya detectaban contacto antes de cerrar.",
                    "moved": False,
                    "stopped": True,
                    "all_contacts_detected": True,
                    "reason": "initial_all_contacts",
                    "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                    "active_sensors": initial_progress["active_sensors"],
                    "contact_sensors": initial_progress["contact_sensors"],
                    "missing_sensors": initial_progress["missing_sensors"],
                    "contact_count": initial_progress["contact_count"],
                    "required_contact_count": initial_progress["required_contact_count"],
                    "ignored_sensors": sorted(list(ignored_set)),
                    "start_from_open": start_from_open,
                    "step_count": step_count,
                    "close_step": close_step,
                    "command": close_command_template,
                    "capacitive": initial_capacitive,
                }

            grip_start_time = time.monotonic()
            complete_contact_counter = 0

            while time.monotonic() - grip_start_time < max_seconds:
                step_count += 1

                # Cierre real progresivo:
                # en vez de mandar cerrar hasta el mínimo, solo baja un pequeño paso.
                step_target = self.hand.step_close(close_step=close_step)

                time.sleep(step_settle_seconds)

                # Parada breve para evitar que siga arrastrando.
                self.stop_hand()

                time.sleep(pause_between_steps)

                capacitive_status = self.refresh_capacitive_status()
                last_capacitive = capacitive_status

                contacts = capacitive_status.get("contacts") or {}
                progress = self._build_full_grip_progress(
                    contacts=contacts,
                    ignored_sensors=ignored_set,
                    required_sensors=normalized_required_sensors
                )

                if progress["all_contacts_detected"]:
                    complete_contact_counter += 1

                    if complete_contact_counter >= consecutive_reads:
                        self.stop_hand()

                        return {
                            "ok": True,
                            "message": "Agarre completo finalizado. Todos los sensores válidos detectan contacto.",
                            "moved": True,
                            "stopped": True,
                            "all_contacts_detected": True,
                            "reason": "all_contacts_detected",
                            "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                            "active_sensors": progress["active_sensors"],
                            "contact_sensors": progress["contact_sensors"],
                            "missing_sensors": progress["missing_sensors"],
                            "contact_count": progress["contact_count"],
                            "required_contact_count": progress["required_contact_count"],
                            "ignored_sensors": sorted(list(ignored_set)),
                            "start_from_open": start_from_open,
                            "step_count": step_count,
                            "close_step": close_step,
                            "last_step_target": step_target,
                            "command": close_command_template,
                            "capacitive": capacitive_status,
                        }
                else:
                    complete_contact_counter = 0

            self.stop_hand()

            final_contacts = {}
            if last_capacitive is not None:
                final_contacts = last_capacitive.get("contacts") or {}

            final_progress = self._build_full_grip_progress(
                contacts=final_contacts,
                ignored_sensors=ignored_set,
                required_sensors=normalized_required_sensors
            )

            return {
                "ok": True,
                "message": "Tiempo máximo alcanzado antes de que todos los sensores válidos detectaran contacto. Mano detenida por seguridad.",
                "moved": True,
                "stopped": True,
                "all_contacts_detected": False,
                "reason": "timeout",
                "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                "active_sensors": final_progress["active_sensors"],
                "contact_sensors": final_progress["contact_sensors"],
                "missing_sensors": final_progress["missing_sensors"],
                "contact_count": final_progress["contact_count"],
                "required_contact_count": final_progress["required_contact_count"],
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": step_count,
                "close_step": close_step,
                "command": close_command_template,
                "capacitive": last_capacitive,
            }

        except Exception as e:
            try:
                self.stop_hand()
            except Exception:
                pass

            return {
                "ok": False,
                "message": f"Error durante el agarre completo: {str(e)}",
                "moved": True,
                "stopped": True,
                "all_contacts_detected": False,
                "reason": "error",
                "elapsed_seconds": round(time.monotonic() - total_start_time, 3),
                "active_sensors": [],
                "contact_sensors": [],
                "missing_sensors": [],
                "contact_count": 0,
                "required_contact_count": 0,
                "ignored_sensors": sorted(list(ignored_set)),
                "start_from_open": start_from_open,
                "step_count": step_count,
                "close_step": close_step,
                "command": close_command_template,
                "capacitive": last_capacitive,
            }
    # -------------------------------------------------------------------------
    # CÁMARA
    # -------------------------------------------------------------------------

    def detect_best_object(self) -> Dict[str, Any]:
        """
        Lanza la detección de objetos y devuelve el mejor objeto detectado.
        """
        if self.simulation:
            return {
                "ok": True,
                "mode": self.mode.value,
                "object": "cup",
                "detection_quality": 87.5,
            }

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
    # SENSORES CAPACITIVOS
    # -------------------------------------------------------------------------

    def refresh_capacitive_status(self) -> Dict[str, Any]:
        """
        Fuerza una lectura de los sensores capacitivos.
        """
        if self.simulation:
            status = {
                "pinky": 140,
                "ring": 5500,
                "middle": 70,
                "index": 10,
                "thumb": 25,
                "palm": 240,
            }

            heights = {
                "pinky": 1700,
                "ring": 1700,
                "middle": 1200,
                "index": 1700,
                "thumb": 1700,
                "palm": 1700,
            }

            contacts = self._calculate_capacitive_contacts(status, heights)

            return {
                "ok": True,
                "available": True,
                "simulation": True,
                "status": status,
                "heights": heights,
                "contacts": contacts,
                "contact_count": sum(1 for value in contacts.values() if value),
                "message": "Simulación: sensores capacitivos actualizados.",
            }

        if not self.capacitive_available or self.capacitive is None:
            return {
                "ok": False,
                "available": False,
                "simulation": False,
                "status": None,
                "heights": None,
                "contacts": None,
                "contact_count": 0,
                "message": "Sensores capacitivos no disponibles.",
            }

        self.capacitive.update_status()

        status = self.capacitive.get_status()
        heights = self.capacitive.get_heights()
        contacts = self._calculate_capacitive_contacts(status, heights)

        return {
            "ok": True,
            "available": True,
            "simulation": False,
            "status": status,
            "heights": heights,
            "contacts": contacts,
            "contact_count": sum(1 for value in contacts.values() if value),
            "message": "Sensores capacitivos actualizados.",
        }

    def get_capacitive_status(self) -> Dict[str, Any]:
        """
        Devuelve la última lectura conocida de los sensores capacitivos.
        """
        if self.simulation:
            return self.refresh_capacitive_status()

        if not self.capacitive_available or self.capacitive is None:
            return {
                "ok": False,
                "available": False,
                "simulation": False,
                "status": None,
                "heights": None,
                "contacts": None,
                "contact_count": 0,
                "message": "Sensores capacitivos no disponibles.",
            }

        status = self.capacitive.get_status()
        heights = self.capacitive.get_heights()
        contacts = self._calculate_capacitive_contacts(status, heights)

        return {
            "ok": True,
            "available": True,
            "simulation": False,
            "status": status,
            "heights": heights,
            "contacts": contacts,
            "contact_count": sum(1 for value in contacts.values() if value),
            "message": "Última lectura de sensores capacitivos obtenida.",
        }

    def _calculate_capacitive_contacts(
        self,
        status: Dict[str, Any],
        heights: Dict[str, Any]
    ) -> Dict[str, bool]:
        """
        Calcula si cada sensor supera su umbral de contacto.
        """
        contacts = {}

        for sensor, value in status.items():
            threshold = heights.get(sensor)

            contacts[sensor] = (
                value is not None
                and threshold is not None
                and value >= threshold
            )

        return contacts

    # -------------------------------------------------------------------------
    # ESTADO
    # -------------------------------------------------------------------------

    def refresh_hand_status(self) -> Dict[str, Any]:
        """
        Fuerza una lectura del estado actual de la mano.
        """
        if self.simulation:
            return {
                "ok": True,
                "hand_status": {
                    "status": "simulated",
                    "ring": 700,
                    "middle": 3000,
                    "index": 3000,
                    "thumb0": 3000,
                    "thumb1": 3000,
                },
                "hand_target": {
                    "command": None,
                    "ring": 700,
                    "middle": 3000,
                    "index": 3000,
                    "thumb0": 3000,
                    "thumb1": 3000,
                },
            }

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
        if self.simulation:
            capacitive_status = self.refresh_capacitive_status()

            return {
                "ok": True,
                "mode": self.mode.value,
                "last_position_mapped": self.last_position_mapped,
                "hand_status": {
                    "status": "simulated"
                },
                "hand_target": {
                    "command": None
                },
                "object_status": {
                    "object": "cup",
                    "detection_quality": 87.5
                },
                "object_best_status": {
                    "object": "cup",
                    "detection_quality": 87.5
                },
                "capacitive_status": capacitive_status,
            }

        hand_status = None
        hand_target = None
        object_status = None
        object_best_status = None
        capacitive_status = None

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

        try:
            capacitive_status = self.get_capacitive_status()
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
            "capacitive_status": capacitive_status,
        }

    # -------------------------------------------------------------------------
    # CIERRE DEL SISTEMA
    # -------------------------------------------------------------------------

    def shutdown(self) -> Dict[str, Any]:
        """
        Libera recursos del sistema.
        """
        if self.simulation:
            return {
                "ok": True,
                "message": "Simulación: sistema detenido y recursos liberados."
            }

        try:
            self.stop_hand()
        except Exception:
            pass

        try:
            if self.hand is not None:
                self.hand.close_i2c()
        except Exception:
            pass

        try:
            if self.capacitive is not None:
                self.capacitive.close_i2c()
        except Exception:
            pass

        return {
            "ok": True,
            "message": "Sistema detenido y recursos liberados."
        }

    # -------------------------------------------------------------------------
    # ESTADO DEL SISTEMA
    # -------------------------------------------------------------------------

    def get_system_info(self) -> Dict[str, Any]:
        """
        Devuelve información general del sistema para depuración y para la app.
        """
        return {
            "ok": True,
            "simulation": self.simulation,
            "mode": self.mode.value,
            "config_path": self.config_path,
            "last_position_mapped": self.last_position_mapped,
            "hand_available": self.hand is not None,
            "camera_available": self.object_rec is not None,
            "voice_available": self.speech is not None,
            "capacitive_available": self.capacitive_available,
        }

    # -------------------------------------------------------------------------
    # OBTENER FRAME DE CÁMARA PARA MOSTRAR EN PANTALLA
    # -------------------------------------------------------------------------

    def get_camera_frame(self, draw: bool = False):
        """
        Devuelve un frame JPEG actual de la cámara.
        """
        if self.simulation or self.object_rec is None:
            return None

        return self.object_rec.get_current_frame_jpeg(draw=draw)

    # -------------------------------------------------------------------------
    # MODO VOICE PARA MOVER LA MANO
    # -------------------------------------------------------------------------

    def wake_up_speech(self) -> None:
        """
        Despierta el microcontrolador de voz mediante el pin GPIO configurado.
        """
        if self.simulation or self.speech is None or self.pin_wkup_speech is None:
            return

        GPIO.output(self.pin_wkup_speech, GPIO.HIGH)
        time.sleep(0.05)
        GPIO.output(self.pin_wkup_speech, GPIO.LOW)

    def read_voice_command(self) -> Dict[str, Any]:
        """
        Despierta el módulo de voz y lee el último comando reconocido.
        """
        if self.simulation:
            return {
                "ok": True,
                "mode": self.mode.value,
                "command": None,
                "detection_quality": None,
                "message": "Simulación: lectura de voz simulada."
            }

        if self.speech is None:
            return {
                "ok": False,
                "message": "El subsistema de voz no está inicializado."
            }

        try:
            self.wake_up_speech()
            time.sleep(0.2)
            self.speech.update()

            status = self.speech.get_status()
            command = status.get("command")
            detection_quality = status.get("detection_quality")

            return {
                "ok": True,
                "mode": self.mode.value,
                "command": command,
                "detection_quality": detection_quality,
                "message": "Comando de voz leído correctamente."
            }

        except Exception as e:
            return {
                "ok": False,
                "message": f"Error al leer voz: {str(e)}"
            }

    def execute_voice_command(self) -> Dict[str, Any]:
        """
        Lee un comando de voz y, si está mapeado, mueve la mano a la posición correspondiente.
        """
        result = self.read_voice_command()

        if not result.get("ok"):
            return result

        command = result.get("command")

        if command is None:
            return {
                "ok": False,
                "command": None,
                "message": "No se recibió ningún comando de voz."
            }

        position_id = self.VOICE_POSITION_MAPPING.get(command)

        if position_id is None:
            return {
                "ok": False,
                "command": command,
                "message": f"El comando de voz {command} no está mapeado a ninguna posición."
            }

        self.last_position_mapped = position_id

        if self.simulation:
            return {
                "ok": True,
                "command": command,
                "position_id": position_id,
                "message": f"Simulación: comando de voz {command} mapeado a posición {position_id}."
            }

        self.hand.move_position(position_id)

        return {
            "ok": True,
            "command": command,
            "position_id": position_id,
            "message": f"Comando de voz {command} ejecutado. Mano movida a posición {position_id}."
        }
    
    def _play_sound_async(self, sound_name: str) -> None:
        """
        Reproduce un sonido del sistema en segundo plano para no bloquear la API.
        sound_name puede ser: hand, voice, camera
        """

        if self.simulation:
            print(f"Simulación: se reproduciría sonido de modo {sound_name}.")
            return

        if not getattr(self, "speaker_available", False) or self.speaker is None:
            print("Altavoz no disponible. No se reproduce sonido.")
            return

        def runner():
            try:
                print(f"Reproduciendo sonido de modo: {sound_name}")

                if sound_name == "hand":
                    self.speaker.play_hand_sound()
                elif sound_name == "voice":
                    self.speaker.play_voice_sound()
                elif sound_name == "camera":
                    self.speaker.play_camera_sound()
                else:
                    print(f"Sonido desconocido: {sound_name}")

            except Exception as e:
                print(f"Error reproduciendo sonido {sound_name}: {e}")

        threading.Thread(target=runner, daemon=True).start()