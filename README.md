# Techno Mortem Arena

Videojuego 2D de duelos de esgrima con multijugador en red local, desarrollado en Java con
[libGDX](https://libgdx.com/).

Proyecto Final de **Programación sobre Redes / Desarrollo de Sistemas**
Escuela Técnica N° 35 D.E. 18 "Ing. Eduardo Latzina" — Automotores / Computación
Ciclo lectivo 2026.

## Integrantes del grupo

| Integrante | GitHub |
|---|---|
| Felipe Igarzábal | [@felipeig2](https://github.com/felipeig2) |
| Mateo Juarez | [@juarezmateo](https://github.com/juarezmateo) |
| Joaquín Muzzi | [@joaquinmuzzi](https://github.com/joaquinmuzzi) |
| Santino Portaluppi | [@santinop145](https://github.com/santinop145) |
| Kevin Yavi | [@kevinyavi](https://github.com/kevinyavi) |

## Descripción del videojuego

**Techno Mortem Arena** es un juego de duelos 1 contra 1 inspirado en las mecánicas de combate de
*Nidhogg*, adaptadas y simplificadas para centrarse en el enfrentamiento directo a muerte entre dos
jugadores.

El combate se basa en el manejo de la espada en tres alturas (cadera, pecho y cabeza): las estocadas
a la misma altura que la guardia del rival se bloquean, mientras que las estocadas a una altura
distinta resultan letales. A eso se suman desarmes, lanzamiento de la espada, combate cuerpo a cuerpo
(puños y patadas) y un sistema de movilidad con desplazamiento lateral, saltos y rodados.

A diferencia de *Nidhogg*, los escenarios no son un avance territorial lineal sino **arenas cerradas**
al estilo de los juegos de pelea tradicionales. Las partidas se juegan al mejor de 5 rondas, con un
límite de 1 minuto por ronda y una mecánica de **muerte súbita** que fuerza el duelo en el centro de
la arena cuando se agota el tiempo. El multijugador funciona en red local: un jugador crea la partida
como anfitrión (con nombre de sala y contraseña opcional) y el otro la encuentra y se une como cliente.

La propuesta completa y detallada está en la Wiki del proyecto (ver más abajo).

## Tecnologías principales

- **Lenguaje:** Java (nivel de compatibilidad de código fuente: Java 8; se requiere un JDK 17 o
  superior para compilar y ejecutar).
- **Framework:** [libGDX](https://libgdx.com/) 1.14.2, generado con
  [gdx-liftoff](https://github.com/libgdx/gdx-liftoff) 1.14.2.1 (plantilla *Classic*).
- **Build system:** Gradle 9.6.1 (mediante el Gradle Wrapper incluido en el repositorio).
- **IDE de referencia:** Eclipse IDE (importado como *Existing Gradle Project*).
- **Redes:** sockets de Java sobre red local (LAN), con arquitectura cliente–servidor. A implementarse
  en las etapas siguientes del proyecto.

### Plataformas objetivo

- **Escritorio (Windows / Linux / macOS)** — plataforma principal y única de esta entrega, mediante el
  backend LWJGL3.
- Web y móvil **no** forman parte del alcance del proyecto: el multijugador en red local y el esquema
  de controles están pensados para escritorio.

### Módulos del proyecto

- `core/` — lógica de la aplicación compartida por todas las plataformas.
- `lwjgl3/` — plataforma de escritorio (backend LWJGL3). En documentación antigua de libGDX este
  módulo se llamaba `desktop`.
- `assets/` — recursos del juego (imágenes, sonidos, fuentes).

### Estructura del código

```
core/src/main/java/com/et35/technomortemarena/
├── TechnoMortemArena.java     Aplicación (Game): carga assets, abre el menú y alterna F11
├── RecursosGraficos.java      Carga y liberación centralizada de texturas
├── RecursosAudio.java         Carga y liberación centralizada de los efectos de sonido
├── mundo/
│   ├── Arena.java             Escenario cerrado en grilla (180 x 101 celdas) y fábricas de mapas
│   ├── Bloque.java            Base de los bloques interactivos
│   ├── Trampolin.java         Bloque que rebota
│   ├── CintaTransportadora.java  Bloque que empuja hacia un lado
│   └── Pincho.java            Bloque letal al tocarlo
├── entidades/
│   ├── Jugador.java           Entidad controlable: física, salto, apuntado, estocada y apilado
│   ├── TipoArma.java          Espada, pistola y escopeta: daño al tirarlas y balas
│   ├── Proyectil.java         Balas, perdigones y armas tiradas (la espada rebota)
│   ├── ArmasEnJuego.java      Proyectiles y armas que aparecen en el mapa para agarrar
│   ├── EstadoJugador.java     Lo necesario para dibujar a un jugador (viaja por la red)
│   └── Hoja.java              La espada como caja rotada: choque exacto (SAT) y barrido de tajos
├── entrada/
│   ├── Controles.java         Mapeo de teclas de cada duelista
│   ├── FuenteEntrada.java     De dónde lee un jugador lo que hace (teclado o red)
│   ├── EntradaJugador.java    Lo que un jugador quiere hacer en un fotograma
│   └── EntradaLocal.java      Teclado (mover, saltar) y mouse (apuntar, estocar) locales
├── red/                       Multijugador por UDP (ver "Multijugador")
│   ├── Protocolo.java         Cabecera, tipos de paquete y constantes
│   ├── ServidorSala.java      Lado del anfitrión: recibe entradas y manda el estado
│   ├── ClienteSala.java       Lado del cliente: se une, baja el mapa y recibe el estado
│   ├── EntradaRemota.java     Entrada de un jugador que llega por la red
│   ├── CodecMapa.java         Filas del mapa a bytes (con repeticiones comprimidas)
│   └── CodecEstado.java       Estado de un jugador a bytes
├── partida/
│   ├── MarcadorPartida.java   Marcador al mejor de 5 rondas y ganador de la partida
│   ├── RondasPartida.java     Ronda, espera entre rondas, edición y victoria (lógica pura)
│   ├── PiezaNivel.java        Los 8 objetos que se colocan entre rondas
│   ├── ColocadorPiezas.java   Reglas para colocar: grilla, choques y zonas de los spawns
│   ├── EdicionNivel.java      Sorteo, elección, colocación y autocolocado (anfitrión)
│   └── EstadoPartida.java     Estado de la partida que viaja por la red
├── niveles/
│   ├── MapaGuardado.java      Mapa con nombre creado en el editor
│   └── RepositorioMapas.java  Guardado y lectura de mapas
└── pantallas/
    ├── PantallaMenu.java          Menú principal (Modo de prueba / Multijugador / Editor de niveles / Salir)
    ├── PantallaMultijugador.java  Crear una sala o unirse a la de otro
    ├── PantallaSeleccionMapa.java Elección del mapa: predefinidos y "Mis mapas" del editor (con scroll)
    ├── PantallaArena.java         Modo de prueba, anfitrión o cliente: rondas, HUD, marcador y pausa
    ├── CamaraDuelo.java           Cámara que sigue a los duelistas y se aleja cuando se separan
    ├── PanelEdicion.java          Tarjetas y vista previa de la edición entre rondas
    ├── PantallaEditor.java        Editor de niveles (herramientas en un panel lateral con menú hamburguesa)
    ├── BarraDeDesplazamiento.java Barra de scroll vertical para listas
    └── Ui.java                    Interfaz común (botones, texto, rectángulos)
```

## Mapa y cámara

El mapa mide 180 x 101 celdas, el doble de ancho y de alto que lo que entra en pantalla con la cámara
cerca. La cámara sigue a los dos duelistas y se aleja cuando se separan, hasta mostrar el mapa entero;
nunca muestra lo que hay fuera del mapa. Durante la edición entre rondas muestra el mapa entero. En el
editor de niveles, la rueda del mouse acerca y aleja la grilla, las flechas o WASD (o arrastrar con el
botón del medio) la mueven y `F` muestra el mapa entero.

## Controles

| Acción | Control |
|---|---|
| Mover izquierda / derecha | `A` / `D` |
| Saltar | `W` |
| Rodar | `Shift` izquierdo (en la dirección en que corrés, o hacia donde mirás si estás quieto) |
| Apuntar la espada | Mover el mouse: la espada gira hacia el cursor |
| Estocada (espada) / disparar (pistola o escopeta) / puñetazo (sin arma) | Clic izquierdo |
| Tirar el arma hacia el cursor | Clic derecho |

**Armas.** Cada duelista tiene 3 corazones, que se pierden por sextos: la espada (en la mano o tirada)
saca un corazón entero, una bala de pistola un tercio, y un perdigón de escopeta o un puñetazo un sexto.
Las balas caen un poco con la gravedad: de lejos hay que apuntar más arriba. Con clic derecho se tira lo
que se tiene en la mano hacia el cursor (más despacio que una estocada: es un último recurso). El arma
tirada cae en arco, rebota poco y, al frenarse apoyada sobre un bloque, queda en el mapa con las balas que
tenía; un arma de fuego sin balas, o que se va del mapa, desaparece. Las armas del suelo brillan y se
agarran pasando por encima, pero solo con la mano vacía: para cambiar de arma, primero hay que tirar la
que se tiene. Sin arma, el
clic pega un puñetazo de corto alcance que empuja al rival y, una de cada tres veces, le hace soltar el
arma. Con la espada también se puede desarmar: si la punta de tu hoja (su último tercio) pega en la base
de la del rival (su primer tercio) mientras la movés, su espada sale volando hacia arriba y cae al mapa.
Cada 8 s aparece una pistola (6 balas) o una escopeta (3 disparos de 5 perdigones en un abanico con algo de dispersión al azar, alcance medio), la que
haya menos en el suelo, en un lugar al azar lejos de los spawns. Además, los bloques spawner de pistolas
y de escopetas (de 3x3, se ponen en el editor y también se pueden elegir entre rondas) hacen aparecer su
arma encima cada 5 s, una sola a la vez. En la esquina de arriba de cada jugador se ve el arma que tiene
en la mano y, debajo, sus balas. Al empezar cada ronda todos vuelven a tener la espada.

Al rodar (solo desde el piso, 0,35 s y con una recarga de 0,9 s) el cuerpo se achica a la mitad de
alto y va más rápido que corriendo, pero no es invulnerable: la espada rival lo hiere igual. Si hay un
techo bajo, sigue agachado hasta tener lugar para pararse.

La espada es una caja rotada y lo que se ve es lo que choca: cualquier contacto de la hoja con el
cuerpo del rival lo hiere, y el choque entre dos hojas empuja a los dos. La estocada adelanta la hoja
un instante (más alcance) y tiene una recarga corta. Un tajo rápido hiere a quien cruza en el camino,
no solo a quien queda debajo de la hoja al terminar. Al iniciar la ronda se muestra un cartel con los
controles.

**Modo de prueba** (botón del menú principal): un jugador con mouse contra un blanco quieto y sin
espada, que reaparece unos segundos después de caer. Sirve para probar mapas (también desde el editor)
y, más adelante, armas. El 1 contra 1 se juega por Multijugador.

Controles generales: `ESC` pausa la partida y `F11` alterna la pantalla completa.

## Multijugador

Cada instancia del juego controla **un solo jugador**, con el mismo teclado y mouse de siempre
(`A`/`D`, `W`, mouse y clic), y varias instancias se conectan a una misma sala. Hoy la sala admite
dos jugadores. La comunicación es por **UDP**.

Para probarlo en una sola máquina, abrí el juego dos veces:

1. En la primera: `MULTIJUGADOR` → `CREAR SALA` → elegir el mapa. Queda esperando al otro jugador.
2. En la segunda: `MULTIJUGADOR` → dejar `127.0.0.1:7777` → `UNIRSE A UNA SALA`.

El campo de texto acepta `ip:puerto`. Para jugar entre dos computadoras de la misma red, el
anfitrión comparte su IP local y el puerto (7777 por defecto; es el mismo que se escribe al crear
la sala). El firewall de Windows puede preguntar si permite a Java recibir conexiones: para probar
en la misma máquina no hace falta permitirlo.

La partida es **al mejor de 5 rondas** (gana quien llega a 3): cuando alguien cae se anota la ronda,
hay una pausa breve con el resultado y la siguiente arranca con los dos de vuelta en sus lugares. En
empate la ronda se repite sin sumar. El HUD muestra las vidas de cada uno y el marcador, y al final
aparece el cartel de victoria con la revancha (la pide el anfitrión).

**Edición del nivel entre rondas** (al estilo *Ultimate Chicken Horse*): después del resultado, quien
perdió la ronda recibe 3 objetos al azar y tiene 20 segundos para elegir uno (clic en la tarjeta o
teclas `1`, `2` y `3`) y colocarlo con un clic en el mapa. La vista previa se ve verde donde se puede
y roja donde no: se puede colocar encima de lo que ya hay, pero no cerca de los spawns ni del
centro, donde la muerte súbita pone a los dos (las zonas rojas). Mientras uno coloca, al otro se le oscurece el mapa: ve lo que elige el rival, pero no es su turno. Si se acaba el tiempo, el objeto elegido se coloca donde estaba el
cursor (o en el lugar válido más cercano) y, si no eligió ninguno, uno de los tres al azar en un lugar
al azar. Las piezas se acumulan durante la partida y la revancha vuelve al mapa original. En empate no
hay edición.

| Objeto | Medidas (largo x alto, en celdas) |
|---|---|
| Fila de pinchos | 6 x 2 |
| Trampolín | 4 x 2 |
| Bloque | 8 x 8 |
| Rectángulo | 10 x 4 |
| Esquina | L de 10 de alto y 4 de largo abajo, brazos de 2 |
| Cinta izquierda / derecha | 6 x 2 |
| Pinchos con cintas | 16 x 2: cinta de 6 hacia la derecha, 4 de pinchos, cinta de 6 hacia la izquierda |
| Spawner de pistolas / escopetas | 3 x 3: hace aparecer su arma encima cada 5 s |

El anfitrión simula todo el juego y le manda el estado al cliente, que solo manda sus teclas y
dibuja lo que recibe. `R` reinicia la ronda solo en el anfitrión, y en red `ESC` abre el menú
de pausa sin frenar la partida. Si el anfitrión sale, el cliente vuelve a la pantalla de
multijugador con el motivo; si el cliente se va, el anfitrión vuelve a esperar a otro.

Todavía no hay descubrimiento de salas ni juego por internet (requiere NAT o un servidor): ver
`CHANGELOG.md` y la tarea de ClickUp "Red".

## Propuesta del proyecto (Wiki)

La propuesta formal y detallada del videojuego —introducción, objetivo, alcance, descripción de las
mecánicas y aportes— se mantiene como documento vivo en la Wiki:

**[Propuesta del Proyecto](https://github.com/joaquinmuzzi/Techno-Mortem-Arena/wiki/Propuesta-del-Proyecto)**

Índice general de la Wiki: <https://github.com/joaquinmuzzi/Techno-Mortem-Arena/wiki>

## Compilación y ejecución

### Requisitos previos

- **JDK 17 o superior.** Verificalo con `java -version`.
- **Git**, para clonar el repositorio.
- No hace falta instalar Gradle: el proyecto incluye el *Gradle Wrapper*, que descarga la versión
  correcta la primera vez que se ejecuta.

### Clonar el repositorio

```bash
git clone https://github.com/joaquinmuzzi/Techno-Mortem-Arena.git
cd Techno-Mortem-Arena
```

### Ejecutar el juego desde la terminal

En Linux o macOS:

```bash
./gradlew lwjgl3:run
```

En Windows (CMD o PowerShell):

```bat
gradlew.bat lwjgl3:run
```

La primera ejecución tarda varios minutos porque descarga Gradle y las dependencias de libGDX.

### Generar un ejecutable (.jar)

```bash
./gradlew lwjgl3:dist
```

El `.jar` autocontenido queda en `lwjgl3/build/libs/`. Se ejecuta con:

```bash
java -jar lwjgl3/build/libs/TechnoMortemArena-1.14.2.1.jar
```

### Importar en Eclipse

1. `File` > `Import...`
2. Elegir `Gradle` > `Existing Gradle Project`.
3. Seleccionar la carpeta raíz del repositorio clonado y completar el asistente.
4. Esperar a que desaparezca el mensaje *"importing Gradle Project"* de la barra inferior: hasta
   entonces la importación no terminó.
5. Ejecutar `lwjgl3/src/main/java/com/et35/technomortemarena/lwjgl3/Lwjgl3Launcher.java` con
   `Run As` > `Java Application`.

> Si las imágenes no cargan, configurar el *Working Directory* de la *Run Configuration* para que
> apunte a la carpeta `assets/` del proyecto.

### Tareas de Gradle útiles

| Tarea | Qué hace |
|---|---|
| `lwjgl3:run` | Compila y ejecuta el juego en escritorio. |
| `lwjgl3:jar` | Genera el `.jar` ejecutable en `lwjgl3/build/libs`. |
| `lwjgl3:dist` | Genera el `.jar` con las dependencias incluidas. |
| `build` | Compila las fuentes y los archivos de todos los módulos. |
| `clean` | Borra las carpetas `build/`. |
| `eclipse` | Genera los metadatos de proyecto de Eclipse. |
| `test` | Ejecuta los tests unitarios (si hay). |

Las tareas específicas de un módulo se invocan con el prefijo del módulo: por ejemplo,
`core:clean` borra la carpeta `build/` sólo del módulo `core`.

## Estado actual del proyecto

**Versión 0.4.0: duelo local con bloques interactivos y editor de niveles.**

- [x] Proyecto libGDX generado con gdx-liftoff (módulos `core` + `lwjgl3`).
- [x] Compilable y ejecutable en escritorio.
- [x] Repositorio Git con `.gitignore` para proyectos libGDX.
- [x] `README.md` y `CHANGELOG.md`.
- [x] Wiki activada con la propuesta del proyecto.
- [x] Arena cerrada con piso y límites laterales.
- [x] Entidades con gravedad y salto.
- [x] Combate apuntado con el mouse: espada como caja rotada con choque exacto, estocada con clic y modo de prueba con blanco.
- [x] Menú principal y selección manual de mapa.
- [x] Bloques interactivos: piso, trampolín, cinta transportadora y pincho.
- [x] Editor de niveles (grilla, pincel, spawns, guardar/cargar, probar mapa).
- [x] Pausa con ESC, apilado entre jugadores y cartel de controles.
- [x] Ventana a resolución nativa y pantalla completa con F11.
- [ ] Desarme, lanzamiento de espada y modo cuerpo a cuerpo.
- [ ] Rodado para esquivar.
- [x] Estructura de partida al mejor de 5 rondas, con HUD de vidas y marcador (en Multijugador).
- [ ] Temporizador por ronda y muerte súbita.
- [ ] Selección de mapa aleatorio por ronda.
- [x] Multijugador por UDP en red local: crear sala y unirse (2 jugadores, una instancia por jugador).
- [ ] Multijugador por internet (NAT, relay o servidor dedicado) y descubrimiento de salas.
- [ ] Pantalla de ajustes (audio y controles) y personalización del personaje.
- [ ] Animaciones del personaje.

### Assets

Los sprites de `assets/sprites/` son **placeholders** generados para poder probar las mecánicas sin
esperar al arte definitivo. Están dibujados en blanco a propósito: `Jugador` los colorea en tiempo de
ejecución con `SpriteBatch.setColor()`, así un mismo archivo sirve para los dos duelistas.

Para reemplazarlos por arte propio basta sobrescribir el `.png` respetando el nombre; si cambian las
medidas, hay que ajustar `Jugador.ANCHO` y `Jugador.ALTO`.

Los efectos de sonido de la ronda en `assets/sounds/` son `Running.mp3` (pisada corta al correr),
`Bloody Sword Hit Sound Effect.mp3` (el golpe de la espada contra el cuerpo del rival) y
`Landing.wav` (el aterrizaje de un salto). `RecursosAudio` los carga una sola vez como `Sound` (se
reproducen desde memoria, sin cortes) y `PantallaArena` los dispara según lo que informa cada
`Jugador`.

A esos se suman los dos sonidos de interfaz, `Boton.wav` (pasar el cursor por encima) y
`Boton Seleccionado.mp3` (hacer clic). Los dispara `pantallas.Ui` desde el propio dibujo del botón, así
que cada pantalla solo tiene que habilitarlos con `Ui.setAudio`; el editor de niveles no lo hace y por
eso queda en silencio.

A esos se suman los dos sonidos de interfaz, `Boton.wav` (pasar el cursor por encima) y
`Boton Seleccionado.mp3` (hacer clic). Los dispara `pantallas.Ui` desde el propio dibujo del botón, así
que cada pantalla solo tiene que habilitarlos con `Ui.setAudio`; el editor de niveles no lo hace y por
eso queda en silencio.

El registro detallado de cambios está en [CHANGELOG.md](CHANGELOG.md).

## Pruebas y contribuciones

```bash
./gradlew test    # tests unitarios (JUnit 6, en core/src/test; requiere JDK 17)
./gradlew build   # compila todos los módulos
```

El CI de GitHub Actions corre estas mismas tareas en cada pull request. Las reglas para contribuir
están en [CONTRIBUTING.md](CONTRIBUTING.md), la política de seguridad en [SECURITY.md](SECURITY.md)
y el código de conducta en [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). Las licencias de las
dependencias están en [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
