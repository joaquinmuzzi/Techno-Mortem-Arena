# Changelog

Todos los cambios significativos de **Techno Mortem Arena** se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto adhiere a
[Versionado Semántico](https://semver.org/lang/es/).

## [No publicado]

### Añadido

- Combate apuntado con el mouse. La espada gira hacia el cursor desde el hombro y el clic izquierdo
  lanza una estocada (la hoja avanza un instante, con una recarga corta). `entidades.Hoja` es la hoja
  como caja rotada, con choque exacto por el teorema del eje de separación contra el cuerpo y contra
  otras hojas. Cualquier contacto de la hoja con el cuerpo del rival lo hiere y el choque de hojas
  empuja a los dos. Un tajo rápido se barre en pasos de unos 11 grados, así hiere a quien cruza en el
  camino y no solo a quien queda debajo de la hoja al final del fotograma.
- `entrada.EntradaLocal`: teclado (mover, saltar) y mouse (apuntar, estocar) locales. El cursor se
  pasa a coordenadas del mundo con el viewport de la arena.
- Modo de prueba: un blanco quieto, sin espada, que reaparece 2 segundos después de caer.
- Tests del combate sin ventana (`JugadorCombateTest`, `HojaTest`): apuntado exacto, hiere solo al
  apuntar al rival, alcance de la estocada y su recarga, invulnerabilidad de 1 segundo, barrido de
  tajos rápidos, choque de hojas, blanco inerte y estado exportado e importado.
- Multijugador por UDP: cada instancia del juego controla un solo jugador y se conecta a una sala
  (hoy de dos jugadores). El anfitrión simula toda la partida y le manda el estado al cliente, que
  manda sus teclas y dibuja lo recibido. Paquete `red` (`Protocolo`, `ServidorSala`, `ClienteSala`,
  `EntradaRemota`, `CodecMapa`, `CodecEstado`) y pantalla `PantallaMultijugador` (crear sala o
  unirse con `ip:puerto`), con un botón nuevo en el menú principal. El cliente baja el mapa del
  anfitrión. Las acciones de una sola vez viajan como contadores y los paquetes viejos se
  descartan, así la pérdida o el desorden de UDP no rompe la partida. La entrada de red lleva un
  token por sesión y solo vale desde la dirección del cliente.
- `entrada.FuenteEntrada` y `EntradaJugador`, y `entidades.EstadoJugador`:
  `Jugador` ya no lee el teclado directamente, y puede exportar e importar su estado.
- `Arena.getFilas()`: las filas de texto de las que salió el mapa.
- Tests de la red: codecs, entrada remota y la sala completa sobre sockets reales por loopback
  (apretón de manos, mapa, entradas, estado, sala llena, tokens, timeouts y paquetes basura).
- Workflows de GitHub Actions: `CI` (compila todos los módulos y corre los tests con JDK 17) y
  `PR title` (título Conventional Commits y línea `Assisted-by:` en la descripción).
- Dependabot mensual y agrupado para las GitHub Actions y las dependencias de Gradle.
- Tests unitarios con JUnit 5 en `core/src/test`: grilla de la arena y sus bloques y mapas del editor.
- Plantillas de pull request y de issues (error y sugerencia), `SECURITY.md`, `CODE_OF_CONDUCT.md` y
  `THIRD_PARTY_NOTICES.md`.

### Cambiado

- Protocolo de red versión 2: la entrada lleva el punto del mouse (dos enteros de 16 bits) y un
  contador de estocadas, y el estado lleva el ángulo y la extensión de la espada. No es compatible
  con la versión 1: las dos instancias tienen que ser de la misma versión del juego.
- El botón "Jugar" del menú pasa a ser "Modo de prueba". El modo local de dos jugadores en un teclado
  se reemplaza por un jugador con mouse contra un blanco; el 1 contra 1 es por Multijugador.
- `PantallaArena` tiene tres modos (de prueba, anfitrión y cliente) y `PantallaSeleccionMapa` acepta qué
  hacer con el mapa elegido.
- `CONTRIBUTING.md`: todo cambio llega por pull request con squash, se documentan las
  verificaciones y las reglas para asistentes de IA.

### Quitado

- Las tres alturas de guardia (`AlturaEspada`) y sus teclas (`E`/`Q`, `O`/`L`), la estocada con tecla
  sostenida y el Jugador 2 con teclado (flechas, `K`).
- `entrada.EntradaTeclado`, reemplazada por `EntradaLocal`.

### Por hacer

- Estocadas, bloqueos por coincidencia de altura y desarme.
- Lanzamiento de espada y modo cuerpo a cuerpo (puños y patadas).
- Rodado para esquivar ataques.
- Estructura de partida al mejor de 5 rondas, con temporizador y muerte súbita.
- Más arenas y selección de mapa aleatorio por ronda.
- Multijugador por internet (NAT, relay o servidor dedicado), descubrimiento de salas en la red local
  y más de dos jugadores por sala.
- Ajustes de audio y controles, y personalización visual del personaje.

## [0.4.1] - 2026-10-19

Primeros efectos de sonido: pisadas al correr, golpes y aterrizajes.

### Añadido

- `assets/sounds/`: tres efectos de sonido en MP3.
  - `Running On Metal Grate.mp3`: pisadas al correr sobre la rejilla.
  - `Bloody Sword Hit Sound Effect.mp3`: golpe de la espada contra el cuerpo del rival.
  - `Landing-Jump.mp3`: aterrizaje despues de un salto o una caida.
- `RecursosAudio`: carga y liberacion centralizada de los efectos de sonido, con el mismo criterio
  que `RecursosGraficos` (se cargan una sola vez, no dentro del bucle de dibujo).
- Sonido de correr en bucle mientras cualquiera de los dos jugadores corre, que se detiene al soltar
  la tecla y al pausar o salir de la ronda.
- Sonido de golpe cuando la estocada de un jugador toca el cuerpo del rival (y no su espada), para
  los dos jugadores.
- Sonido de aterrizaje cuando un jugador toca el piso despues de haber estado en el aire.

### Cambiado

- `TechnoMortemArena`: carga `RecursosAudio` junto con los graficos y lo libera al cerrar.
- `entidades.Jugador`: expone `estaCorriendo()`, `acabaDeGolpear()` y `acabaDeAterrizar()`. Distingue
  salir del piso y volver a tocarlo, para avisar del aterrizaje una sola vez (la primera caida al
  empezar la ronda no suena).
- `pantallas.PantallaArena`: reproduce los efectos segun lo que informa cada `Jugador` y apaga el
  bucle de correr al pausar.
- `pantallas.PantallaMenu`, `PantallaSeleccionMapa` y `PantallaEditor`: reciben y pasan
  `RecursosAudio` para que llegue a la ronda.
- `README.md`: documenta los sonidos de `assets/sounds/` y `RecursosAudio` en la estructura del codigo.

## [0.4.0] - 2026-10-18

Bloques interactivos, editor de niveles, pausa y cartel de controles al empezar la ronda.

### Añadido

- `pantallas.PantallaArena`: cartel de controles al iniciar la ronda. Muestra los mandos del
  Jugador 1 arriba a la izquierda y los del Jugador 2 arriba a la derecha, y se desvanece a los
  pocos segundos para que nadie arranque perdido sin tapar la arena.
- Bloques interactivos: `mundo.Bloque` como base, con ganchos para aterrizar, rozar y velocidad
  horizontal, y los tipos `mundo.Trampolin` (rebota), `mundo.CintaTransportadora` (empuja hacia
  la izquierda o la derecha) y `mundo.Pincho` (mata al tocarlo).
- `pantallas.PantallaEditor`: editor de niveles con grilla, pincel de 1x1 a 4x4, spawns de cada
  jugador, guardado y carga de mapas por nombre, y prueba del mapa (R vuelve al editor).
- `niveles.MapaGuardado` y `niveles.RepositorioMapas`: guardado y lectura de mapas con nombre en
  un único archivo.
- `pantallas.Ui`: interfaz común (rectángulos, botones y texto) reutilizada por los menús, la
  pausa y el editor.
- Pausa en partida con ESC: seguir, volver al menú principal, a la selección de mapa o salir.
- `TechnoMortemArena`: la tecla F11 alterna la pantalla completa.

### Cambiado

- `mundo.Arena`: la arena se describe con una grilla de 90 columnas y los bloques dejan de ser
  solo piso. Se agregan las fábricas `conPlataformas()`, `conTrampolin()` y `conCintasYPinchos()`.
- `entidades.Jugador`: aterrizaje y roce con los bloques, apilado entre jugadores (el de arriba
  hereda la velocidad del de abajo) y empuje por golpe como distancia fija.
- `entidades.AlturaEspada`: los desplazamientos de guardia se escalan con `Arena.ESCALA`.
- `pantallas.PantallaMenu` y `pantallas.PantallaSeleccionMapa`: usan la interfaz común `Ui`; el
  menú principal suma el botón "Editor de niveles".
- Lanzador de escritorio: la ventana se abre a la resolución del monitor (hasta 1920x1080) en vez
  de 800x600.

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

[No publicado]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.4.1...HEAD
[0.4.1]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.2...v0.3.0
[0.2.2]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/releases/tag/v0.1.0
