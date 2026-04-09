from enum import Enum


class SystemMode(Enum):
    INIT = "init"
    HAND = "hand"
    VOICE = "voice"
    CAMERA = "camera"