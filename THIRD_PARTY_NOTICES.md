# Avisos de terceros

Techno Mortem Arena usa software de terceros que tiene sus propias licencias. Este resumen sirve
para revisarlas antes de reutilizar o redistribuir el juego.

> **Esto no es asesoramiento legal.** La información sale de la documentación de cada proyecto.
> Las licencias pueden cambiar: consultá siempre la fuente original.

## Dependencias

| Componente | Uso | Licencia |
|---|---|---|
| [libGDX](https://libgdx.com/) | Framework del juego (gráficos, entrada, archivos) | [Apache License 2.0](https://github.com/libgdx/libgdx/blob/master/LICENSE) |
| [LWJGL 3](https://www.lwjgl.org/) | Backend de escritorio de libGDX (ventana, OpenGL, audio) | [BSD de 3 cláusulas](https://www.lwjgl.org/license) |
| [gdx-liftoff](https://github.com/libgdx/gdx-liftoff) | Generó la estructura inicial del proyecto | [Apache License 2.0](https://github.com/libgdx/gdx-liftoff/blob/master/LICENSE) |
| [Gradle](https://gradle.org/) (Gradle Wrapper) | Compilación | [Apache License 2.0](https://github.com/gradle/gradle/blob/master/LICENSE) |

Las versiones exactas están en `gradle.properties` y en los archivos `build.gradle`. Las
dependencias indirectas que trae cada componente conservan su propia licencia.

## Recursos gráficos

Los sprites de `assets/sprites/` son *placeholders* hechos por el equipo para probar las mecánicas
y no dependen de terceros. `assets/libgdx.png` es el logo de libGDX que incluye la plantilla del
proyecto.

## Licencia del código

El repositorio todavía no declara una licencia propia para el código. Hasta que el equipo elija
una, todos los derechos quedan reservados por sus autores.
