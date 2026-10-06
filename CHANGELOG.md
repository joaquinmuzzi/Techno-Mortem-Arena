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
- Más arenas y selección de mapa aleatorio por ronda.
- Multijugador en red local: creación de partida (host) y búsqueda / unión (cliente).
- Ajustes de audio y controles, y personalización visual del personaje.

## [0.3.0] - 2026-10-06

Primera versión con selección de mapa y una arena con agujero.

### Añadido

- `pantallas.PantallaSeleccionMapa`: menú entre el menú principal y la ronda para elegir el mapa.
- `mundo.Arena.conAgujero()`: arena con un agujero de 192 px en el piso centrado; los jugadores que
  caen por él mueren.
- `mundo.Arena.hayAgujeroEn(x)`: indica si una coordenada horizontal cae sobre el agujero.

### Cambiado

- "Jugar" en el menú principal lleva a la selección de mapa, en vez de entrar directo a la arena.
- `PantallaArena` recibe la `Arena` por parámetro, y el reinicio con R conserva el mapa elegido.
- `entidades.Jugador` no aterriza sobre el agujero y queda muerto al caer por debajo de la pantalla.

## [0.2.2] - 2026-09-04

Retroceso físico y menú principal con brazo dedicado.

### Añadido

- `assets/sprites/brazo.png`: antebrazo separado del cuerpo que sigue la altura de guardia y la
  estocada.
- Rotación de la espada con un vaivén aleatorio suavizado.
- `pantallas.PantallaMenu`: menú principal con título y botones Jugar / Salir, en placeholder.

### Cambiado

- Retroceso físico al recibir un golpe: empuja la velocidad del jugador en vez de mover su posición.
- Corrección de la desaceleración horizontal: la rama de velocidad negativa usaba `Math.max` y no
  frenaba al jugador.
- Se quitó el brazo que venía dibujado dentro de `jugador.png`, para no duplicarlo con el nuevo.

## [0.2.1] - 2026-08-31

Primer combate: colisiones entre jugadores, estocada y sistema de vidas.

### Añadido

- Colisión entre jugadores: empuje cuerpo a cuerpo y espada contra cuerpo, que solo se aplica en el
  eje X para poder saltar sobre el rival.
- Tecla de estocada (`F` para el jugador uno, `K` para el dos), que extiende el alcance de la espada.
- Sistema de vidas: tres por jugador, con parpadeo de invulnerabilidad al recibir un golpe. Al llegar
  a cero, el jugador queda muerto.
- Reinicio de la ronda con la tecla `R`.

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

[No publicado]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.2...v0.3.0
[0.2.2]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/releases/tag/v0.1.0
