Proyecto Android — Mueblería UMG (Kotlin)
========================================

Requisitos
----------
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17 (el IDE suele incluirlo)

Primer arranque
---------------
1. Copiar "local.properties.example" a "local.properties" y editar sdk.dir con la ruta de tu Android SDK.
2. Abrir esta carpeta ("ProyectoBD-Android") en Android Studio: File > Open.
3. Si pide generar Gradle Wrapper, aceptar; o usar el menú Gradle > wrapper.
4. Ajustar en app/build.gradle.kts la constante API_BASE_URL (debe terminar en /) cuando tengáis la API.
5. Ejecutar la app en un emulador o dispositivo (Run).

Documentación
-------------
- docs/INTEGRACION_BD.txt — Cómo encaja Oracle, procedimientos y la API respecto al enunciado.
- database/NOTA_SCRIPTS_ORACLE.txt — Dónde conviene versionar los scripts SQL del equipo.

Nota: El emulador accede al PC host con la IP 10.0.2.2 (no usar "localhost" como URL del servidor si el backend corre en tu máquina).
