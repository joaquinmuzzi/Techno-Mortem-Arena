# Changelog

Todos los cambios significativos de **Techno Mortem Arena** se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto adhiere a
[Versionado Semántico](https://semver.org/lang/es/).

## [No publicado]

### Por hacer

- Mecánicas de combate: posiciones de espada (cadera, pecho, cabeza), estocadas, bloqueos y desarme.
- Lanzamiento de espada y modo cuerpo a cuerpo (puños y patadas).
- Sistema de movilidad: desplazamiento lateral, salto y rodado.
- Estructura de partida al mejor de 5 rondas, con temporizador y muerte súbita.
- Arenas cerradas y selección de mapa (fijo o aleatorio por ronda).
- Multijugador en red local: creación de partida (host) y búsqueda / unión (cliente).
- Menú principal, ajustes de audio y controles, y personalización visual del personaje.

## [0.1.0] - 2026-08-25

Primera pre-entrega: configuración inicial del proyecto y del repositorio.

### Añadido

- Proyecto libGDX 1.14.2 generado con gdx-liftoff 1.14.2.1, con los módulos `core` y `lwjgl3` y la
  plantilla *Classic*.
- Paquete base `com.et35.technomortemarena` y clase principal `TechnoMortemArena`.
- Lanzador de escritorio `Lwjgl3Launcher` con el backend LWJGL3, en resolución 640x480.
- Gradle Wrapper (Gradle 9.6.1) para compilar sin instalar Gradle.
- Archivo `.gitignore` para proyectos libGDX: excluye `build/`, `.gradle/`, `local.properties`, los
  metadatos de Eclipse / IntelliJ / NetBeans y los archivos generados por el sistema operativo.
- `README.md` con el nombre del proyecto, los integrantes del grupo, la descripción del videojuego,
  las tecnologías y plataformas objetivo, el enlace a la Wiki y las instrucciones de compilación y
  ejecución.
- Este `CHANGELOG.md`.
- Wiki del repositorio con la propuesta formal del proyecto como documento vivo.

### Cambiado

- Título de la ventana del juego: de `TechnoMortemArena` (nombre técnico del módulo) a
  `Techno Mortem Arena`.

[No publicado]: https://github.com/joaquinmuzzi/psr-duelo-libgdx/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/joaquinmuzzi/psr-duelo-libgdx/releases/tag/v0.1.0
