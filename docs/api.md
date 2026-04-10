# API de mi TFG

## Qué es esta API

He creado esta API para que, más adelante, la app Android pueda comunicarse con la Raspberry Pi por Wi-Fi.

La idea es que la app no hable directamente con la mano ni con los ficheros internos del proyecto, sino con esta API. Luego, la API será la que use mi controlador (`HandSystemController`) para hacer las acciones necesarias.

Ahora mismo la API está funcionando en **modo simulación**, o sea, todavía no controla la mano real, pero sí me permite probar toda la estructura del backend.

---

## Cómo la arranco

Desde la carpeta `raspberry/`, con el entorno virtual activado, la arranco así:

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Luego puedo abrir en el navegador:

```text
http://127.0.0.1:8000/docs
```

y ahí veo la documentación automática de FastAPI y puedo probar todos los endpoints.

---

## Estructura general

La API usa como base una instancia global de `HandSystemController`, que se guarda en `app.state.controller`.

De esa forma, todas las rutas pueden acceder al mismo controlador.

---

## Endpoints disponibles

### `GET /`

Este endpoint lo uso para comprobar rápidamente que la API está levantada y funcionando.

Devuelve un JSON simple con un mensaje de estado.

#### Respuesta esperada

```json
{
  "ok": true,
  "message": "API de la mano robótica funcionando"
}
```

---

### `GET /health/`

Este endpoint lo uso también para comprobar que el backend está operativo.

Devuelve un mensaje simple indicando que todo está funcionando.

La idea es tener una comprobación rápida de salud del sistema.

---

### `GET /status/`

Este endpoint lo uso para obtener un resumen general del estado del sistema.

En simulación devuelve cosas como:

- modo actual
- última posición usada
- estado simulado de la mano
- estado simulado del objeto detectado

La idea es tener una vista general del sistema en un solo endpoint.

---

### `POST /status/refresh-hand`

Este endpoint lo uso para refrescar el estado de la mano.

En simulación devuelve un estado simulado de la mano y del objetivo actual.

La diferencia con `/status/` es que este está más pensado para pedir una actualización de la mano.

---

### `POST /modes/hand`

Este endpoint lo uso para cambiar el modo actual del sistema a **modo mano**.

Devuelve una respuesta con:

- `ok`
- `mode`
- un mensaje indicando que el modo mano está activado

Sirve para decirle al backend en qué modo quiero que trabaje.

---

### `POST /modes/voice`

Este endpoint lo uso para cambiar el sistema a **modo voz**.

Devuelve una confirmación de que el modo voz se ha activado.

---

### `POST /modes/camera`

Este endpoint lo uso para cambiar el sistema a **modo cámara**.

Devuelve una confirmación de que el modo cámara se ha activado.

---

### `POST /hand/open`

Este endpoint lo uso para enviar una orden de apertura de la mano.

En simulación devuelve una respuesta indicando que la orden se ha enviado correctamente y también el comando usado.

Más adelante, cuando tenga la mano real, este endpoint servirá para abrir la mano de verdad.

---

### `POST /hand/stop`

Este endpoint lo uso para parar el movimiento de la mano.

En simulación devuelve una confirmación de que la orden de parada se ha enviado.

---

### `POST /hand/position/{position_id}`

Este endpoint lo uso para mover la mano a una posición predefinida.

#### Parámetro

- `position_id`: número de posición al que quiero mover la mano

#### Qué devuelve

- confirmación
- posición usada

Este endpoint está pensado para mandar la mano a una posición concreta, por ejemplo una asociada a un tipo de agarre.

---

### `POST /hand/manual`

Este endpoint lo uso para mandar un movimiento manual de la mano.

Recibe un JSON con un diccionario llamado `command`.

#### Ejemplo

```json
{
  "command": {
    "ring": "open",
    "index": "close",
    "thumb0": "open"
  }
}
```

Devuelve una respuesta indicando que la orden manual se ha enviado.

Este endpoint me permite controlar algunos dedos de forma más directa.

---

### `POST /camera/detect`

Este endpoint lo uso para lanzar una detección de objeto.

En simulación devuelve un objeto fijo de prueba, por ejemplo:

- `"cup"`
- calidad `87.5`

Sirve para probar el flujo de detección sin necesidad de cámara real.

---

### `POST /camera/detect-and-move`

Este endpoint lo uso para hacer dos cosas seguidas:

1. detectar un objeto
2. mover la mano a la posición asociada a ese objeto

#### Qué devuelve

- objeto detectado
- calidad
- posición objetivo
- mensaje de confirmación

