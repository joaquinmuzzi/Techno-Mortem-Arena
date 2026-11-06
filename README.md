# Techno Mortem Arena

Videojuego 2D de duelos de esgrima, desarrollado en Java con [libGDX](https://libgdx.com/).

> **Rama `sin-red`:** esta versión no tiene la parte de red. No hay Multijugador: se juega el modo de
> prueba al mejor de 5 rondas contra un blanco quieto y con `TAB` se cambia de duelista (el azul o el
> rojo). El juego completo, con red, está en `main`.

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
la arena cuando se agota el tiempo.

La propuesta completa y detallada está en la Wiki del proyecto (ver más abajo).

## Tecnologías principales

- **Lenguaje:** Java (nivel de compatibilidad de código fuente: Java 8; se requiere un JDK 17 o
  superior para compilar y ejecutar).
- **Framework:** [libGDX](https://libgdx.com/) 1.14.2, generado con
  [gdx-liftoff](https://github.com/libgdx/gdx-liftoff) 1.14.2.1 (plantilla *Classic*).
- **Build system:** Gradle 9.6.1 (mediante el Gradle Wrapper incluido en el repositorio).
- **IDE de referencia:** Eclipse IDE (importado como *Existing Gradle Project*).

### Plataformas objetivo

- **Escritorio (Windows / Linux / macOS)** — plataforma principal y única de esta entrega, mediante el
  backend LWJGL3.
- Web y móvil **no** forman parte del alcance del proyecto: el esquema de controles está pensado para
  escritorio.

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
│   ├── TipoArma.java          Espadas y armas de fuego: daño al tirarlas, de la hoja y balas
│   ├── Proyectil.java         Balas, perdigones y armas tiradas (la espada rebota)
│   ├── ArmasEnJuego.java      Proyectiles y armas que aparecen en el mapa para agarrar
│   └── Hoja.java              La espada como caja rotada: choque exacto (SAT) y barrido de tajos
├── entrada/
│   ├── Controles.java         Mapeo de teclas de cada duelista
│   ├── FuenteEntrada.java     De dónde lee un jugador lo que hace (teclado o nada)
│   ├── EntradaJugador.java    Lo que un jugador quiere hacer en un fotograma
│   └── EntradaLocal.java      Teclado (mover, saltar) y mouse (apuntar, estocar) locales
├── partida/
│   ├── MarcadorPartida.java   Marcador al mejor de 5 rondas y ganador de la partida
│   └── RondasPartida.java     Ronda, espera entre rondas y victoria (lógica pura)
├── niveles/
│   ├── MapaGuardado.java      Mapa con nombre creado en el editor
│   └── RepositorioMapas.java  Guardado y lectura de mapas
└── pantallas/
    ├── PantallaMenu.java          Menú principal (Modo de prueba / Editor de niveles / Opciones / Salir)
    ├── PantallaSeleccionMapa.java Elección del mapa: predefinidos y "Mis mapas" del editor (con scroll)
    ├── PantallaArena.java         Modo de prueba: rondas, HUD, marcador, pausa y cambio de duelista (TAB)
    ├── CamaraDuelo.java           Cámara que sigue a los duelistas y se aleja cuando se separan
    ├── PantallaEditor.java        Editor de niveles (herramientas en un panel lateral con menú hamburguesa)
    ├── BarraDeDesplazamiento.java Barra de scroll vertical para listas
    └── Ui.java                    Interfaz común (botones, texto, rectángulos)
```

## Mapa y cámara

El mapa mide 180 x 101 celdas, el doble de ancho y de alto que lo que entra en pantalla con la cámara
cerca. La cámara sigue a los dos duelistas y se aleja cuando se separan, hasta mostrar el mapa entero;
nunca muestra lo que hay fuera del mapa. En el
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
Cada 8 s cae del cielo un arma de fuego, la que haya menos en el suelo, en un lugar al azar lejos de los
spawns: una pistola (6 balas), una escopeta (3 disparos de 5 perdigones en un abanico con algo de dispersión
al azar, alcance medio), un rifle láser (20 rayos largos y muy rápidos que no caen; cada uno saca un sexto y
no respeta la invulnerabilidad después de un golpe) o un lanzacohetes (1 cohete medio lento que explota al
chocar: saca un corazón a todos los que estén a menos de 4 celdas, también al que lo tiró, y rompe los
bloques de la celda del impacto y las de alrededor hasta que empieza la ronda siguiente; las paredes de los
costados y la fila de abajo de todo no se rompen). Muy de vez en cuando (una de cada 12) cae en cambio la
espada láser roja: pega un corazón y un tercio, no se puede desarmar ni soltar por un puñetazo, atraviesa a
la espada común sin chocar y, tirada, cae menos y llega más lejos. Las balas, los perdigones y los cohetes
que pegan en una hoja rebotan espejados según el ángulo de la hoja y pasan a ser de quien los reflejó: con
la espada común pierden algo de velocidad y se desvían un poco; con la láser rebotan perfecto. Los rayos
láser no se reflejan. Además, los bloques spawner de pistolas
y de escopetas (de 3x3, se ponen en el editor y también se pueden elegir entre rondas) hacen aparecer su
arma encima cada 5 s, una sola a la vez. Cada 12 s cae también una mejora que dura 10 s: velocidad (+40 %), salto (+35 %) o puntería (dispara más
seguido y la escopeta abre menos); la agarra quien pase por encima. En la esquina de arriba de cada jugador se ve el arma que tiene
en la mano y, debajo, sus balas. Al empezar cada ronda todos vuelven a tener la espada.

Al rodar (solo desde el piso, 0,35 s y con una recarga de 0,9 s) el cuerpo se achica a la mitad de
alto y va más rápido que corriendo, pero no es invulnerable: la espada rival lo hiere igual. Si hay un
techo bajo, sigue agachado hasta tener lugar para pararse.

La espada es una caja rotada y lo que se ve es lo que choca: cualquier contacto de la hoja con el
cuerpo del rival lo hiere, y el choque entre dos hojas empuja a los dos. La estocada adelanta la hoja
un instante (más alcance) y tiene una recarga corta. Un tajo rápido hiere a quien cruza en el camino,
no solo a quien queda debajo de la hoja al terminar. Al iniciar la ronda se muestra un cartel con los
controles.

**Modo de prueba** (botón del menú principal): una partida al mejor de 5 rondas, con el temporizador y
la muerte súbita, de un duelista manejado con el teclado y el mouse contra un blanco quieto.
Con `TAB` se cambia de duelista: el que se manejaba queda como blanco, quieto pero con el arma que tenía,
y el otro (el azul o el rojo) toma el teclado y el mouse. Sirve para probar mapas (también desde el editor) y armas.

Controles generales: `ESC` pausa la partida y `F11` alterna la pantalla completa.

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
- [x] Estructura de partida al mejor de 5 rondas, con HUD de vidas y marcador (en el modo de prueba).
- [ ] Temporizador por ronda y muerte súbita.
- [ ] Selección de mapa aleatorio por ronda.
- [ ] Pantalla de ajustes (audio y controles) y personalización del personaje.
- [ ] Animaciones del personaje.

### Assets

Los sprites de `assets/sprites/` son **placeholders** generados para poder probar las mecánicas sin
esperar al arte definitivo. Están dibujados en blanco a propósito: `Jugador` los colorea en tiempo de
ejecución con `SpriteBatch.setColor()`, así un mismo archivo sirve para los dos duelistas.

Para reemplazarlos por arte propio basta sobrescribir el `.png` respetando el nombre; si cambian las
medidas, hay que ajustar `Jugador.ANCHO` y `Jugador.ALTO`.

Los efectos de sonido de la ronda en `assets/sounds/` son `Running.mp3` (pisada corta al correr),
`Bloody Sword Hit Sound Effect.mp3` (el golpe de la espada contra el cuerpo del rival), `Landing.wav` (el
aterrizaje de un salto), `pistol-shot.mp3` y `shotgun-blast.mp3` (el disparo de la pistola y de la
escopeta), `sword-equipped.mp3` (al recoger la espada), `Weapon-Equip.mp3` (al recoger una pistola o una
escopeta) y `sword-falling.mp3` (cada vez que la espada tirada toca el piso, también en los rebotes).
`RecursosAudio` los carga una sola vez como `Sound` (se reproducen desde memoria, sin cortes) y
`PantallaArena` los dispara según lo que informan cada `Jugador` y `ArmasEnJuego`.

A esos se suman los dos sonidos de interfaz, `Boton.wav` (pasar el cursor por encima) y
`Boton Seleccionado.mp3` (hacer clic). Los dispara `pantallas.Ui` desde el propio dibujo del botón, así
que cada pantalla solo tiene que habilitarlos con `Ui.setAudio`; el editor de niveles no lo hace y por
eso queda en silencio.

La música del menú es `assets/music/menu.mp3`: `RecursosAudio` la carga como `Music` (se lee del
disco de a poco) y suena en las pantallas del menú. Durante la partida suena en cambio la música de
combate, `assets/music/combate.mp3`; en el editor no suena ninguna. Desde OPCIONES, en el menú principal, se apagan la
música o los efectos; la elección se guarda entre partidas. Los efectos se reproducen siempre con
`RecursosAudio.reproducir` y `repetir`, que no suenan si están apagados.

El texto de la interfaz usa dos fuentes pixeladas, sin suavizado, en `assets/fuentes/`: Orbitron
en negrita para los títulos y los botones, y Share Tech Mono para el resto (el HUD, la ayuda, las
listas). Están horneadas en varios tamaños con `herramientas/hornear_fuentes.py` (necesita Pillow) y
`pantallas.Ui` usa el más cercano a cada escala. Las licencias están en
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

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
