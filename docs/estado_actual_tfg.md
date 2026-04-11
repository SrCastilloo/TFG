
# Estructura conceptual del proyecto

## `hardware/`
Aquí está el **código heredado** que  controla cosas reales del sistema:

- motores
- cámara
- voz
- EMG
- sensores capacitivos
- LEDs RGB
- altavoz
- comunicación I2C

Esta carpeta representa la **capa de acceso al hardware real**.

## `core/`
Aquí está la **capa nueva** que se ha empezado a construir para mi TFG actual.

Su objetivo es no trabajar directamente con todos los archivos de hardware, sino tener una capa intermedia más entendible para mí.

---

# Ficheros actuales

## 1. `core/state.py`

### Qué es
Define los **modos del sistema**.

### Contenido
- `INIT`
- `HAND`
- `VOICE`
- `CAMERA`

### Para qué sirve
Permite representar de forma ordenada el estado actual del sistema.

En lugar de usar textos sueltos como `"hand"` o `"camera"` por todo el código, se centralizan en un único sitio.

### Idea clave
Este fichero define los **modos oficiales** del sistema.

---

## 2. `core/controller.py`

### Qué es
Es la **pieza principal nueva** que se ha creado hasta ahora.

Contiene la clase:

- `HandSystemController`

### Para qué sirve
Actúa como **controlador principal del backend nuevo**.

Su función es ofrecer una capa más limpia con el objetivo de que:
- la API use esta clase
-  la app Android se comunique indirectamente con el hardware a través de ella

### Qué hace actualmente

#### a) Gestiona el modo actual
Tiene métodos para cambiar entre:

- `set_mode_hand()`
- `set_mode_voice()`
- `set_mode_camera()`

#### b) Controla la mano
Tiene métodos para:

- abrir la mano (`open_hand()`)
- parar la mano (`stop_hand()`)
- mover la mano a una posición fija (`move_to_position()`)
- mover la mano manualmente (`move_manual()`)

#### c) Usa la cámara
Tiene métodos para:

- detectar el mejor objeto (`detect_best_object()`)
- detectar un objeto y mover la mano según ese objeto (`detect_object_and_move()`)

#### d) Da estado del sistema
Tiene métodos para:

- refrescar el estado real de la mano (`refresh_hand_status()`)
- devolver un resumen del estado (`get_status()`)

#### e) Cierra recursos
Tiene un método:

- `shutdown()`

que intenta parar la mano y cerrar I2C de forma limpia.

---

# Ficheros heredados de `hardware/`

---

## 3. `hardware/i2cdevice.py`

### Qué es
Es la clase más básica de la comunicación con hardware.

### Para qué sirve
Encapsula la comunicación I2C usando `smbus2`.

### Qué ofrece
- `write_bytes()`
- `read_bytes()`
- `close()`

### Idea clave
Es la capa más baja del sistema: las demás clases de hardware se apoyan en ella para hablar por I2C.

---

## 4. `hardware/handControl.py`

### Qué es
Es la clase más importante para controlar los **motores de la mano**.

### Para qué sirve
Permite controlar la mano sin tener que hablar directamente con el microcontrolador de motores.


#### a) Carga configuración desde `config.ini`
Lee:
- dirección I2C
- comandos
- número de dedos
- valores mínimos y máximos
- posiciones predefinidas

#### b) Mantiene estado actual y objetivo
- `__status`: estado real actual
- `__target`: estado objetivo al que se quiere mover la mano

#### c) Permite controlar la mano
Métodos principales:
- `update_status()`
- `move_open_close()`
- `move_position()`
- `stop_movement()`

### Funcionalidad
Esta clase es la **API real heredada** para mover la mano.

El backend se apoyará en `HandControl`.

---

## 5. `hardware/objectRec.py`

### Qué es
Es la clase que gestiona la **detección de objetos con la cámara**.

### Para qué sirve
Permite detectar objetos mediante OpenCV usando la cámara de la Raspberry.


#### a) Carga un modelo de detección
Utiliza:
- `coco.names`
- `ssd_mobilenet...pbtxt`
- `frozen_inference_graph.pb`

#### b) Abre la cámara
Usa `cv2.VideoCapture(0)`.

#### c) Detecta objetos
Tiene métodos para detectar objetos y devolver el mejor encontrado.

#### d) Mantiene estado
- estado actual detectado
- mejor objeto detectado
- calidad de detección

### Método más importante
- `detect_object()`

Este método ejecuta detección durante un tiempo y se queda con el mejor objeto detectado.

Es la base para la parte de:
- visión artificial
- adaptación del movimiento de la mano al objeto

Esta clase ya resuelve la parte de qué objeto estoy viendo.

---

## 6. `hardware/capacitiveControl.py`

### Qué es
Es la clase del subsistema de **sensores capacitivos de tacto**.

### Para qué sirve
Lee el estado de los sensores capacitivos situados en la mano.

### Qué hace
- carga configuración desde `config.ini`
- lee datos por I2C
- interpreta los valores de:
  - `pinky`
  - `ring`
  - `middle`
  - `index`
  - `thumb`
  - `palm`

### También carga
Los umbrales o alturas de referencia de cada sensor.

Todavía no está conectada al `controller.py`, pero será útil para:
- agarre adaptativo
- parar dedos cuando se detecte demasiada presión

Es la base del tacto de la mano.

---

## 7. `hardware/cmdDevice.py`

### Qué es
Es la clase del subsistema de **reconocimiento por voz**.

### Para qué sirve
Lee por I2C los comandos reconocidos por el microcontrolador de voz.

### Qué hace
- carga comandos aceptados desde `config.ini`
- lee mensajes I2C
- interpreta:
  - comando reconocido
  - calidad de la detección

### Método importante
- `update()`

### Cambio realizado
Se corrigió la validación del rango de calidad de detección, porque la condición heredada estaba mal planteada.

Aún no se usa en `controller.py`, pero será útil cuando se conecte el modo voz al backend nuevo.

Es la base del control por voz del sistema.

---

## 8. `hardware/emgDevice.py`

### Qué es
Es la clase del subsistema **EMG**.

### Para qué sirve
Gestiona la detección de comandos EMG y el envío de órdenes relacionadas con motores vibradores.

### Qué hace

#### a) Lee estado EMG
- comando detectado
- calidad

#### b) Mantiene objetivos
- iniciar reconocimiento EMG
- activar/desactivar motores vibradores

#### c) Envía órdenes por I2C
Puede mandar mensajes al subsistema EMG.

### Cambio realizado
Se corrigió también la validación del rango de calidad.

Todavía no está integrada en el controlador nuevo, pero será importante si más adelante se quiere aprovechar EMG o feedback táctil.

Es la base del control por señales musculares y de la vibración asociada.

---

## 9. `hardware/rgbControl.py`

### Qué es
Es la clase que controla los **LEDs RGB**.

### Para qué sirve
Permite:
- cambiar color
- cambiar brillo
- apagar LEDs

### Métodos principales
- `set_color()`
- `set_brightness()`
- `get_color()`
- `get_brightness()`
- `clear()`

No es prioritario ahora mismo, pero sirve para feedback visual o parte lúdica.

Es una mejora visual del sistema, no una pieza crítica del backend actual.

---

## 10. `hardware/speakerControl.py`

### Qué es
Es la clase que controla el **altavoz**.

### Para qué sirve
Reproducir audios de los modos del sistema:

- modo mano
- modo voz
- modo cámara

### Métodos principales
- `play_hand_sound()`
- `play_voice_sound()`
- `play_camera_sound()`

No se está usando todavía en la capa nueva, pero sirve como feedback sonoro.

Es una parte auxiliar del sistema, útil para mejorar experiencia de usuario.

---


**11-04-2026**
He añadido dos endpoints nuevos que me están sirviendo bastante para depuración y para preparar la futura app Android:

- `GET /system/info`
- `GET /hand/positions`

### Endpoint `/system/info`

Este endpoint me devuelve información general del sistema, por ejemplo:

- si estoy en simulación o no
- el modo actual
- la ruta del `config.ini`
- si la mano y la cámara están disponibles

Esto me viene bien para saber rápidamente en qué estado está el backend.

### Endpoint `/hand/positions`

Este endpoint me devuelve las posiciones predefinidas de la mano que están guardadas en el `config.ini`.

Ahora mismo me devuelve correctamente las posiciones del 0 al 9.

---

## Problema detectado y corregido con `config.ini`

Durante el desarrollo me di cuenta de que el fichero `raspberry/config/config.ini` estaba vacío.

Eso provocaba que algunas partes de la API no funcionaran correctamente, por ejemplo el endpoint `/hand/positions`, porque el sistema no encontraba la sección `[positions]`.

Lo que hice fue copiar al `config.ini` el contenido heredado del TFG anterior, ya que ese fichero contiene configuraciones necesarias para varios subsistemas, como por ejemplo:

- GPIO
- speaker
- RGB
- EMG
- sensores capacitivos
- reconocimiento de voz
- reconocimiento de objetos
- control de la mano
- valores mínimos y máximos
- posiciones predefinidas

Después de eso, el endpoint `/hand/positions` pasó a funcionar correctamente.

---

## Adaptación realizada sobre código heredado

En general estoy intentando mantener separado mi código nuevo del código heredado del TFG anterior.

Aun así, sí he realizado una adaptación concreta en `raspberry/hardware/objectRec.py`, porque tenía rutas absolutas metidas directamente en el código y eso hacía que dependiera de una estructura muy concreta de carpetas en la Raspberry anterior.

Lo he dejado adaptado para que esa parte sea más configurable y no dependa de rutas fijas tan rígidas.

---

