Proyecto Android — Mueblería UMG (Kotlin)
========================================

Requisitos
----------
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17 (el IDE suele incluirlo)

Primer arranque
---------------
1. Copiar "local.properties.example" a "local.properties" y editar `sdk.dir` con la ruta de tu Android SDK.
2. Abrir esta carpeta ("ProyectoBD-Android") en Android Studio: File > Open.
3. Si pide generar Gradle Wrapper, aceptar; o usar el menú Gradle > wrapper.
4. Ajustar en app/build.gradle.kts la constante API_BASE_URL (debe terminar en /) cuando tengáis la API.
5. Ejecutar la app en un emulador o dispositivo (Run).

**`sdk.dir` y las barras invertidas (`\`) en Windows:** `local.properties` usa formato `.properties`: la barra invertida es carácter de **escape**, no un separador “directo”. Por eso, si usás rutas al estilo `C:\Users\...`, en el archivo cada separador real se escribe como **dos invertidas** `\\` (no son dos barras normales `//`). Ejemplo: `sdk.dir=C\:\\Users\\TuUsuario\\AppData\\Local\\Android\\Sdk`. **Alternativa equivalente:** usar sólo barras normales hacia adelante y evitar escapes, p. ej. `sdk.dir=C:/Users/TuUsuario/AppData/Local/Android/Sdk`.

Emulador (AVD) de referencia del equipo
---------------------------------------
Configuración acordada para desarrollo y pruebas en Android Studio (Device Manager → Create Device):

| Campo | Valor |
|-------|--------|
| Perfil de hardware | **Pixel 8** |
| Imagen del sistema | **API 34** |
| Variante | **Google Play** |
| ABI / imagen | **Intel x86_64 Atom** (system image; en PC Windows típico con CPU Intel/AMD x86_64) |

Ventajas: API estable (no pre-release), Play Store disponible en la imagen, buen equilibrio entre compatibilidad con `minSdk` del proyecto y herramientas actuales. Otros integrantes pueden crear un AVD equivalente para reproducir el mismo entorno.

**Gradle y JDK:** si Android Studio avisa de incompatibilidad, usar **JDK 17** como *Gradle JVM* (Gradle 8.x no debe usar JVM 21).

Nota de red (emulador)
----------------------
El emulador accede al PC host con la IP **10.0.2.2** (no usar `localhost` como host del servidor si el backend ASP.NET corre en tu máquina; en `API_BASE_URL` usar `http://10.0.2.2:PUERTO/...` según el puerto de IIS Express o Kestrel).


