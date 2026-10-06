# Cómo contribuir a Techno Mortem Arena

Gracias por sumarte al proyecto. Este documento explica cómo subir cambios al repositorio, qué formato
usar en los commits y cómo declarar el uso de asistentes de IA.

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

## Cuándo usar pull request y cuándo commit directo

**Usá pull request (rama + PR) cuando:**

- Cambia la lógica compartida que afecta a todos: física, colisiones, pantallas, formatos de archivo
  como `niveles/mapas.txt`.
- Es una funcionalidad grande o de varios archivos.
- Otra persona está trabajando en la misma área y necesita ver los cambios antes.
- Querés que alguien pruebe o apruebe el cambio antes de que llegue a `main`.
- Es un cambio que se entrega al profesor y conviene dejar registro de la revisión.

**Podés hacer commit directo a `main` cuando:**

- Es un arreglo chico y claro, como un typo, un texto o un valor de configuración.
- Es documentación sin lógica, como `README.md` o `CHANGELOG.md`.
- Estás solo en el área, el cambio es acotado y lo probaste.
- Es un cambio de herramientas de build que no toca el juego.

Ante la duda, usá pull request.

### Cómo abrir un pull request

1. Creá una rama a partir de `main`: `git checkout -b feature/nombre-corto`.
2. Hacé los commits en esa rama y subila: `git push -u origin feature/nombre-corto`.
3. Abrí el PR contra `main` del repositorio original, con una descripción que diga qué cambia, cómo
   se probó y qué conviene revisar.
4. Esperá la revisión antes de hacer merge. No mergees tu propio PR si hay otra persona disponible
   para revisarlo.

## Formato de los commits

El mensaje tiene un título corto con un tipo, seguido de dos puntos:

- `Feat:` funcionalidad nueva.
- `Fix:` corrección de un error.
- `Docs:` documentación.
- `Chore:` tareas de mantenimiento, build o configuración.

El título va en imperativo y describe el cambio, no el proceso. Por ejemplo:
`Feat: Seleccion de mapa y arena con agujero`.

Si el commit necesita explicación, dejá una línea en blanco y escribí el cuerpo en prosa. Explicá el
porqué, no lo que ya se ve en el diff.

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
