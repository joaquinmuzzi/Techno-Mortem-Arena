# Changelog

Todos los cambios significativos de **Techno Mortem Arena** se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto adhiere a
[Versionado Semántico](https://semver.org/lang/es/).

## [No publicado]

### Por hacer

- Estocadas, bloqueos por coincidencia de altura y desarme.
- Lanzamiento de espada y modo cuerpo a cuerpo (puños y patadas).
- Rodado para esquivar ataques.
- Estructura de partida al mejor de 5 rondas, con temporizador y muerte súbita.
- Varias arenas y selección de mapa (fijo o aleatorio por ronda).
- Multijugador en red local: creación de partida (host) y búsqueda / unión (cliente).
- Menú principal, ajustes de audio y controles, y personalización visual del personaje.

## [0.2.0] - 2026-08-27

Primera base jugable: entorno con entidades controlables.

### Añadido

- Sprites placeholder en `assets/sprites/` (`jugador.png`, `espada.png`, `piso.png`), dibujados en
  blanco para poder colorearlos en tiempo de ejecución.
- `RecursosGraficos`: carga y liberación centralizada de texturas, con filtro `Nearest` para
  mantener nítido el arte de píxeles.
- `mundo.Arena`: escenario cerrado de 960x540 con piso a 64 px y límites laterales, que implementa el
  "espacio cerrado y delimitado" de la propuesta.
- `entidades.AlturaEspada`: enumeración con las tres alturas de guardia (cadera, pecho, cabeza) y las
  operaciones para subirla y bajarla.
- `entrada.Controles`: mapeo de teclas por jugador, inyectado en `Jugador` para que una sola clase
  sirva a los dos duelistas.
- `entidades.Jugador`: entidad controlable con desplazamiento lateral, salto, gravedad, colisión con
  el piso, orientación y guardia en tres alturas.
- `pantallas.PantallaArena`: ronda de duelo con `FitViewport` para que las distancias sean idénticas
  en cualquier resolución, y acotado del delta para que la física no se rompa al congelarse el bucle.

### Cambiado

- `TechnoMortemArena` pasa de `ApplicationAdapter` a `Game`, para poder alternar entre pantallas
  (menú, selección de mapa, arena) más adelante.

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

[No publicado]: https://github.com/joaquinmuzzi/psr-duelo-libgdx/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/joaquinmuzzi/psr-duelo-libgdx/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/joaquinmuzzi/psr-duelo-libgdx/releases/tag/v0.1.0
