# Actividad 2.2 — Threads y Sensores (IEI-093)

Aplicación Android desarrollada en **Java** que integra **hilos de segundo plano**, **sensores**
como entrada de datos y **AsyncTask**, tomando como base la Actividad 2.1 (intents explícitos e
implícitos) tal como pide la pauta.

- Asignatura: IEI-093 — Programación Android
- Actividad: Unidad 2, Semana 7, Actividad 2.2 — Threads y Sensores
- Autor: Samuel Bugueño Vega (Nek) — Analista Programador, IP-CFT Santo Tomás, sede Ovalle

## 1. Qué hace la aplicación

Pantalla principal (`MainActivity`):

1. **Intents (lo de la actividad anterior, sigue integrado)**
   - Intent **explícito** hacia `AResultado`: envía el nombre escrito y muestra el valor recibido.
   - Intent **implícito** a la cámara del sistema (`MediaStore.ACTION_IMAGE_CAPTURE`).
   - Intent **implícito** para compartir con otras apps (`ACTION_SEND`).
2. **Threads** — botón *DESCARGAR IMAGEN (THREAD)*: descarga una imagen de internet dentro de un
   hilo propio llamado `HiloDescarga` y, recién al terminar, publica el resultado en la interfaz con
   `ImageView.post()`. Si el equipo no tiene internet, dibuja una imagen de respaldo en el propio
   celular, de modo que la demostración del hilo funciona igual.
3. **AsyncTask** — botón *PROCESAR EN ESCALA DE GRISES (ASYNCTASK)*: convierte la imagen pixel a
   pixel informando el avance con `publishProgress()` / `onProgressUpdate()`, y al terminar muestra
   el resultado y el tiempo que tardó (`onPostExecute`).
4. **Sensores** — botón *VER SENSORES* (`ASensores`): usa el **vector de rotación**
   (`TYPE_ROTATION_VECTOR`, el ejemplo 2 de la pauta) y además el **acelerómetro**. Muestra los
   valores en vivo, la orientación calculada (azimut, pitch, roll) y la lista de todos los sensores
   del equipo.

## 2. Requisitos de la pauta y dónde se cumplen

| Requisito de la pauta | Dónde está en el código |
| --- | --- |
| Implementar Threads para operaciones en segundo plano | `MainActivity.descargarEnHiloPropio()` + `descargarImagen()` |
| Integrar sensores para capturar datos de entrada | `ASensores.java` (vector de rotación y acelerómetro) |
| Utilizar AsyncTask para operaciones asincrónicas | `MainActivity.TareaGris` (`doInBackground`, `publishProgress`, `onPostExecute`) |
| Realizar commits regulares en el repositorio Git | historial de commits de este repositorio |
| App funcional con Threads y sensores | APK `app-debug.apk` ejecutada en el celular |
| Elementos de la actividad anterior integrados | intents explícitos e implícitos en `MainActivity` y `AResultado` |

## 3. Cómo compilarla y ejecutarla

Requisitos: Android Studio (o JDK 17 + SDK 37) y el celular con **depuración USB** activada.

```bat
gradlew.bat testDebugUnitTest    :: pruebas unitarias de la logica (corren en la JVM)
gradlew.bat assembleDebug        :: genera app\build\outputs\apk\debug\app-debug.apk
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

También se puede abrir la carpeta del proyecto en Android Studio, conectar el celular por USB y
presionar **Run**.

Configuración del proyecto: `compileSdk 37`, `minSdk 30`, `targetSdk 37`, Java 11,
AGP 9.3.3 y Gradle 9.5.

## 4. Pruebas unitarias

La lógica que no depende de Android está separada en dos clases (`EscalaGris` y `Orientacion`) para
poder probarla en la JVM sin dispositivo:

```bat
gradlew.bat testDebugUnitTest
```

Resultado: **11 pruebas, 0 fallas** (`EscalaGrisTest`: 6, `OrientacionTest`: 5).

## 5. Preguntas de cierre

**1) ¿Qué nuevos conocimientos y habilidades has adquirido en el manejo de subprocesos y captura de
datos de sensores?**

Que la interfaz gráfica solo se puede tocar desde el hilo principal: por eso el hilo de descarga no
modifica la `ImageView` directamente, sino que publica el resultado con `post()`. Que un sensor no
se "consulta", sino que entrega eventos: hay que registrarse, recibir los valores en
`onSensorChanged()` y desregistrarse en `onPause()` para no gastar batería. También aprendí a
organizar el trabajo pesado con `AsyncTask` (el cálculo en `doInBackground` y los cambios de pantalla
en `onProgressUpdate` y `onPostExecute`) y a obtener la orientación del equipo a partir de la matriz
de rotación del vector de rotación.

**2) ¿Cómo podrías utilizar los Threads y los sensores en otras aplicaciones móviles?**

Los Threads sirven para todo lo que tarda y no debe congelar la pantalla: descargar o subir
archivos, sincronizar datos, guardar en la base de datos o procesar imágenes. Los sensores permiten
otras formas de entrada: podómetro y control de actividad física con el acelerómetro, juegos que se
controlan inclinando el equipo con el vector de rotación, brillo automático, detección de golpes o
caídas, y realidad aumentada combinando la cámara con la orientación. Uniendo ambos, se pueden hacer
apps como una libreta de entrenamiento que registre los pasos en segundo plano y los muestre en vivo.

**3) ¿Qué desafíos encontraste en el proceso y cómo los superaste?**

- *Actualizar la pantalla desde un hilo de segundo plano*: la app se cerraba por tocar las vistas
  fuera del hilo principal; lo resolví con `post()` sobre la `ImageView`.
- *Que la app no se congelara al procesar la imagen*: separé el cálculo en `doInBackground()` y
  dejé solo los cambios visuales en `onProgressUpdate()` y `onPostExecute()`, con una barra de
  progreso que informa el avance.
- *Consumo de batería de los sensores*: al principio quedaban registrados al salir de la pantalla;
  ahora se registran en `onResume()` y se desregistran en `onPause()`.
- *Probar en la sala sin internet*: agregué una imagen de respaldo generada en el equipo para que el
  hilo de descarga se pueda demostrar igual.
- *`AsyncTask` está deprecada desde Android 11*: se mantiene porque la pauta la pide, pero en el
  código quedó documentado que hoy lo recomendado es `ExecutorService`, corrutinas o `WorkManager`.

## 6. Notas técnicas

- `AsyncTask` figura como deprecada pero es parte del aprendizaje esperado de la actividad.
- La descarga usa `HttpURLConnection` con tiempos de espera de 5 segundos; ante cualquier error,
  cae en la imagen de respaldo.
- El permiso `INTERNET` está declarado en el `AndroidManifest.xml`; los sensores se declararon como
  no obligatorios (`uses-feature ... required="false"`) para que la app avise en vez de cerrarse si
  el equipo no los trae.
