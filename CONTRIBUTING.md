# Cómo contribuir a Techno Mortem Arena

Este documento explica cómo reportar problemas, preparar el entorno, subir cambios, qué formato
usar en los commits y cómo declarar el uso de asistentes de IA. Al participar aceptás seguir el
[Código de conducta](CODE_OF_CONDUCT.md).

## Formas de contribuir

- **Reportar un error** con el formulario de
  [reporte de error](https://github.com/joaquinmuzzi/Techno-Mortem-Arena/issues/new?template=bug_report.yml).
- **Sugerir una mejora** con el formulario de
  [sugerencia](https://github.com/joaquinmuzzi/Techno-Mortem-Arena/issues/new?template=feature_request.yml).
- **Mandar un pull request.** Para algo más grande que un arreglo chico, abrí primero un issue o una
  tarea en ClickUp para acordar el enfoque antes de invertir tiempo.

Los problemas de seguridad no se reportan en issues públicos: mirá [SECURITY.md](SECURITY.md).

## Entorno de desarrollo

Necesitás **JDK 17 o superior** y Git. No hace falta instalar Gradle: el repositorio trae el Gradle
Wrapper.

```bash
./gradlew lwjgl3:run      # compila y abre el juego
```

En Windows usá `gradlew.bat` en lugar de `./gradlew`. La guía de instalación completa, con Eclipse,
está en el [README](README.md).

## Verificaciones

Son las mismas que corre el CI (ver `.github/workflows/`). Corrélas antes de abrir un pull request:

```bash
./gradlew build      # compila todos los módulos
./gradlew test       # tests unitarios
```

Los tests están en `core/src/test/java` y usan JUnit 5. Cubren la lógica que se puede probar sin
abrir una ventana: la grilla de la arena y sus bloques, las alturas de la espada y los mapas del
editor. Si tu cambio toca esa lógica, agregá o actualizá tests. Si arreglás un error, agregá un test
que lo reproduzca cuando se pueda.

Compilar y pasar los tests **no alcanza** para lo que ve el jugador: si cambiás una mecánica,
una pantalla o el editor, probalo jugando la parte afectada y contá en la descripción cómo lo
probaste.

## Estructura del proyecto

```
core/        lógica del juego compartida por todas las plataformas
  src/main/  código (ver "Estructura del código" en el README)
  src/test/  tests unitarios (JUnit 5)
lwjgl3/      plataforma de escritorio y lanzador
assets/      imágenes, sonidos y fuentes
.github/     workflows de CI, plantillas de issues y de PR, Dependabot
CHANGELOG.md qué cambió en cada versión
```

## Estilo de código

El código y los comentarios están en español, y los comentarios explican *por qué* el código hace
algo, no qué hace. Respetá el `.editorconfig` (4 espacios en Java, 2 en Gradle, finales de línea LF).
Mantené el código parecido al que lo rodea.

## Pull requests

Todo cambio llega a `main` por pull request, así corren el CI y los controles del título y de la
descripción.

1. Creá una rama a partir de `main`: `git checkout -b feature/nombre-corto`.
2. Hacé los commits en esa rama y corré las verificaciones de arriba.
3. Subila (`git push -u origin feature/nombre-corto`) y abrí el PR contra `main`.
4. El **título debe seguir Conventional Commits** (por ejemplo `fix(editor): evita guardar mapas sin
   nombre`): el CI lo controla y el PR se mergea con *squash*, así que el título pasa a ser el
   mensaje del commit en `main`.
5. En la descripción explicá qué cambia, por qué y cómo lo probaste, y terminala con la línea
   `Assisted-by:`. La plantilla trae las secciones.
6. El CI corre `build`, `tests`, `conventional title` y `ai disclosure`. Tienen que estar en verde.
   Pedí revisión a alguien del equipo cuando el cambio toque lógica compartida (física, colisiones,
   pantallas, formato de `niveles/mapas.txt`) o cuando otra persona trabaje en la misma área. Un
   cambio chico de documentación lo podés mergear vos cuando el CI esté en verde.
7. Si el cambio altera lo que ve el jugador o cómo se arma el proyecto, agregá una línea en
   *No publicado* de [CHANGELOG.md](CHANGELOG.md) (sigue
   [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)).

Mantené los pull requests chicos y enfocados: un arreglo, una funcionalidad o un refactor, no los
tres juntos. Si un cambio necesita un refactor antes, mandalo en su propio pull request.

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

Podés usar herramientas de IA (Claude Code, Codex, Copilot, Cursor...), pero tenés que decirlo:

- **Cada commit hecho con un asistente de IA termina con un trailer** que nombra la herramienta y
  el modelo:

  ```
  Assisted-by: AGENT_NAME:MODEL_VERSION
  ```

  Por ejemplo `Assisted-by: Claude Code:claude-sonnet-5-5`. Escribí una línea por asistente si usaste
  más de uno. Los editores, los linters y los formateadores no cuentan.
- **La descripción del pull request termina con la misma línea**, o con `Assisted-by: none` si no
  se usó IA. Los PR se mergean con *squash* y su descripción pasa a ser el mensaje del commit en
  `main`, así que ahí queda registrado. El CI verifica que la línea esté.
- **Sos responsable del cambio**, lo haya escrito quien lo haya escrito: leé y entendé cada línea,
  corré las verificaciones vos mismo y decí en la descripción qué corriste.

No agregues otras marcas de herramientas (por ejemplo "Generated with ...") en las descripciones.
Los pull requests que no se probaron, o que se mandan en masa a muchos proyectos a la vez, se
cierran.

## Qué no subir

- Archivos generados por el build: `build/`, `.gradle/`.
- Archivos de configuración de tu IDE: `.project`, `.classpath`, `.settings/`.
- Niveles o mapas de prueba locales, como `niveles/`, salvo que se acuerde subirlos para el proyecto.
- Credenciales, tokens o datos personales.

## Licencia

El repositorio todavía no declara una licencia para el código. Las dependencias de terceros tienen
las suyas: ver [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
