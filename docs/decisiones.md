# Decisiones tomadas durante el desarrollo

## Separación entre código heredado y código nuevo

Desde el principio decidí intentar separar lo máximo posible el código heredado del TFG anterior del código nuevo que estoy desarrollando yo.

La idea es que el código heredado se quede principalmente en `raspberry/hardware/` y `raspberry/legacy/`, mientras que mi trabajo nuevo esté sobre todo en:

- `raspberry/core/`
- `raspberry/api/`
- `docs/`

He tomado esta decisión para que quede más claro qué partes vienen del trabajo anterior y cuáles he ido desarrollando yo para mi TFG.

---

## Uso de FastAPI para el backend

He decidido montar el backend con FastAPI porque me permite exponer una API HTTP de forma sencilla y rápida, además de darme automáticamente una documentación interactiva en `/docs`.

Esto me viene muy bien porque:

- puedo probar los endpoints fácilmente
- puedo depurar el backend sin la app Android
- más adelante la app podrá consumir esta API directamente

---

## Uso de un controlador central

He creado una clase `HandSystemController` para centralizar la lógica principal del sistema.

He preferido hacerlo así porque no quería que las rutas de la API hablasen directamente con los ficheros de hardware o con la lógica de bajo nivel.

Con esta decisión consigo que la API use una capa intermedia más limpia y que luego sea más fácil cambiar cosas sin romper todas las rutas.

---

## Trabajo en modo simulación

Como ahora mismo no tengo acceso a la mano real, he decidido implementar un modo simulación.

Esto me permite seguir avanzando en el backend sin depender del hardware físico.

Gracias a eso he podido:

- arrancar la API
- probar todos los endpoints
- depurar errores de nombres y rutas
- avanzar en la estructura del proyecto

La idea es que más adelante pueda pasar de simulación a modo real sin tener que rehacer toda la API.

---

## Añadir el endpoint `/system/info`

He añadido el endpoint `GET /system/info` porque me parecía útil tener una forma rápida de consultar el estado general del backend.

Este endpoint me permite saber si el sistema está en simulación, el modo actual en el que está trabajando y otros datos útiles de depuración.

Me parece una decisión buena porque también le puede venir bien a la futura app Android para saber en qué estado está la Raspberry.

---

## Añadir el endpoint `/hand/positions`

He añadido el endpoint `GET /hand/positions` para poder consultar desde la API qué posiciones predefinidas de la mano existen en el `config.ini`.

He tomado esta decisión porque no quiero depender de acordarme manualmente de los números de posición ni meterlos fijos en la futura app Android.

Así, la app podrá consultar directamente qué posiciones hay disponibles.

---

## Recuperar el contenido de `config.ini`

En un momento del desarrollo detecté que `raspberry/config/config.ini` estaba vacío.

Eso explicaba varios errores, porque muchas partes del sistema heredado dependen de ese fichero.

Decidí copiar en él el contenido heredado del TFG anterior, ya que es una configuración necesaria para el funcionamiento del sistema.

Me pareció la decisión correcta porque ese fichero no era algo opcional, sino parte importante de la base heredada del proyecto.

---

## Adaptar `objectRec.py`

Mi intención general era no tocar el código heredado si no era necesario.

Aun así, en el caso de `raspberry/hardware/objectRec.py` sí hice una adaptación, porque tenía rutas absolutas muy rígidas que dependían de una estructura concreta de carpetas en otra Raspberry.

Decidí dejar esa parte adaptada para que el sistema sea más configurable y menos dependiente de rutas fijas.

Sé que aquí sí he tocado código heredado, así que lo dejo documentado para que quede claro que ha sido una adaptación puntual y justificada.

---

## Mantener la documentación al día

He decidido ir documentando el proyecto mientras avanzo, en vez de dejar toda la documentación para el final.

Me parece mejor hacerlo así porque:

- no se me olvidan las decisiones tomadas
- tengo más claro en qué punto está el proyecto
- me será más fácil defender luego lo que he hecho