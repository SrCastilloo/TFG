from fastapi import APIRouter, Request
from time import sleep
import threading
import RPi.GPIO as GPIO

from hardware.cmdDevice import CmdDevice

router = APIRouter()

CONF_FILE = "config/config.ini"
PIN_WKUP_SPEECH = 10

VOICE_POSITION_MAPPING = {
    "0x11": 1,  # uno
    "0x19": 2,  # dos
    "0x1d": 3,  # tres
    "0x1f": 4,  # cuatro
    "0x1": 5,   # cinco
}

VOICE_LOCK = threading.Lock()


def read_voice_once():
    """
    Lee una orden de voz usando la secuencia correcta:
    wake HIGH 0.05s -> wake LOW -> esperar 5s -> leer I2C.
    """
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(PIN_WKUP_SPEECH, GPIO.OUT, initial=GPIO.LOW)

    speech = None

    try:
        print("Inicializando voz...")
        speech = CmdDevice("cmd_speech", CONF_FILE)

        print("Pulso wake de voz...")
        GPIO.output(PIN_WKUP_SPEECH, GPIO.HIGH)
        sleep(0.05)
        GPIO.output(PIN_WKUP_SPEECH, GPIO.LOW)

        print("Esperando reconocimiento de voz...")
        sleep(5)

        print("Leyendo voz por I2C...")
        speech.update()

        status = speech.get_status()
        command = status.get("command")
        quality = status.get("detection_quality")

        return {
            "ok": True,
            "command": command,
            "detection_quality": quality,
        }

    except Exception as e:
        return {
            "ok": False,
            "error": str(e),
        }

    finally:
        GPIO.output(PIN_WKUP_SPEECH, GPIO.LOW)

        if speech is not None:
            try:
                speech.close_i2c()
            except Exception:
                pass


@router.post("/detect")
def detect_voice():
    """
    Detecta una orden de voz, pero NO mueve la mano.
    """
    with VOICE_LOCK:
        result = read_voice_once()

    return result


@router.post("/detect-and-move")
def detect_voice_and_move(request: Request):
    """
    Detecta una orden de voz y mueve la mano si el comando está mapeado.
    """
    with VOICE_LOCK:
        voice_result = read_voice_once()

        if not voice_result["ok"]:
            return voice_result

        command = voice_result.get("command")

        if command not in VOICE_POSITION_MAPPING:
            return {
                "ok": True,
                "moved": False,
                "voice": voice_result,
                "message": f"Comando {command} recibido, pero no se mueve la mano.",
            }

        position_id = VOICE_POSITION_MAPPING[command]
        controller = request.app.state.controller
        move_result = controller.move_to_position(position_id)

        return {
            "ok": True,
            "moved": True,
            "voice": voice_result,
            "position_id": position_id,
            "move_result": move_result,
        }