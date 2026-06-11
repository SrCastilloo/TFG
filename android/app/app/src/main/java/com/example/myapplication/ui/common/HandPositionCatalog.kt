package com.example.myapplication.ui.common

private val positionDescriptions = mapOf(
    0 to "Mano abierta",
    1 to "Señalar o pulsar",
    2 to "Agarrar un cilindro",
    3 to "Agarrar una esfera",
    4 to "Agarre palmar",
    5 to "Agarre de punta",
    6 to "Agarre lateral",
    7 to "Agarre de gancho",
    8 to "Agarre trípode",
    9 to "Gesto de paz"
)

fun getHandPositionDescription(position: Int): String {
    return positionDescriptions[position] ?: "Configuración guardada"
}

fun getHandPositionTitle(position: Int): String {
    return "Posición $position: ${getHandPositionDescription(position)}"
}