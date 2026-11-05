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

## Fuentes

Las fuentes de `assets/fuentes/` son versiones de mapa de bits, sin suavizado, de dos fuentes de
[Google Fonts](https://github.com/google/fonts), hechas con `herramientas/hornear_fuentes.py`:

| Fuente | Uso | Licencia |
|---|---|---|
| [Orbitron](https://github.com/theleagueof/orbitron) (The Orbitron Project Authors) | Títulos y botones (`titulo-NN`) | [SIL Open Font License 1.1](assets/fuentes/OFL-Orbitron.txt) |
| [Share Tech Mono](https://fonts.google.com/specimen/Share+Tech+Mono) (Carrois Type Design) | El resto del texto (`mono-NN`) | [SIL Open Font License 1.1](assets/fuentes/OFL-ShareTechMono.txt) |

Como son versiones modificadas, los archivos no usan los nombres reservados de las fuentes.

## Licencia del código

El repositorio todavía no declara una licencia propia para el código. Hasta que el equipo elija
una, todos los derechos quedan reservados por sus autores.
