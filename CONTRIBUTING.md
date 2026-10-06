# Cómo contribuir a Techno Mortem Arena

Este documento explica cómo subir cambios al repositorio, qué formato usar en los commits y cómo declarar el uso de asistentes de IA.

## Antes de empezar

1. Compilá el proyecto y revisá que el juego arranque:

   ```bash
   ./gradlew compileJava
   ./gradlew lwjgl3:run
   ```

2. Si tu cambio es visible para el jugador (mecánicas, pantallas, mapas), probalo jugando la parte
   afectada antes de subirlo. Compilar no alcanza para verificar que una mecánica funcione.

3. Si el cambio afecta al comportamiento del juego, agregá una línea en `CHANGELOG.md` en la sección
   `[No publicado]`.

### Cómo abrir un pull request

1. Creá una rama a partir de `main`: `git checkout -b feature/nombre-corto`.
2. Hacé los commits en esa rama y subila: `git push -u origin feature/nombre-corto`.
3. Abrí el PR contra `main` del repositorio original, con una descripción que diga qué cambia, cómo
   se probó y qué conviene revisar.
4. Esperá la revisión antes de hacer merge. No mergees tu propio PR si hay otra persona disponible
   para revisarlo.

## Formato de los commits

Seguimos el estándar de **Conventional Commits**. El mensaje se compone de un encabezado obligatorio y un cuerpo opcional.

### 1. Encabezado (Header)
Formato: `<tipo>: <descripción corta>`

Tipos principales:
- `feat:` nueva funcionalidad.
- `fix:` corrección de un error.
- `docs:` cambios solo en la documentación.
- `chore:` tareas de mantenimiento, dependencias o configuración sin modificar código de producción.
- `refactor:` cambio de código que no arregla un bug ni añade una función.
- `test:` añadir o corregir pruebas.

Reglas del encabezado:
- Usa el tipo siempre en **minúsculas**.
- Escribe la descripción en **imperativo** y en **minúsculas** (ej. `añade`, `corrige`, `permite`, no `añadido` ni `añadiendo`).
- Máximo 50–72 caracteres.
- **Ejemplo:** `feat: permite seleccion de mapa y arena con agujero`

### 2. Cuerpo (Body) — Opcional
Si el commit requiere explicación adicional:
- Deja **una línea en blanco** después del encabezado.
- Redacta en prosa explicando el **motivo** del cambio y el contexto (el *porqué*, no el *qué* o el *cómo*, que ya se ven en el diff).
- Envuelve las líneas a ~72 caracteres para facilitar la lectura en consola (`git log`).

---

### Ejemplo completo:

feat: permite seleccion de mapa y arena con agujero

Se añade la interfaz de seleccion de escenario previa a la partida. 
Esto permite probar la nueva mecanica de caida en agujeros sin 
depender de la carga por defecto.

## Contribuciones asistidas por IA

Si usaste un asistente de IA (como Claude) para escribir o revisar el código, agregá esta línea al final
del mensaje del commit:

```
Assisted-by: AGENT_NAME:MODEL_VERSION
```

Por ejemplo:

```
Assisted-by: Claude:claude-sonnet-5
```

La línea indica qué herramienta participó, no quita responsabilidad al autor. Quien hace el commit sigue
siendo responsable de entender el código, probarlo y explicarlo en la revisión.

## Qué no subir

- Archivos generados por el build: `build/`, `.gradle/`.
- Archivos de configuración de tu IDE: `.project`, `.classpath`, `.settings/`.
- Niveles o mapas de prueba locales, como `niveles/`, salvo que se acuerde subirlos para el proyecto.
- Credenciales, tokens o datos personales.
