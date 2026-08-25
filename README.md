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

## Propuesta del proyecto (Wiki)

La propuesta formal y detallada del videojuego —introducción, objetivo, alcance, descripción de las
mecánicas y aportes— se mantiene como documento vivo en la Wiki:

**[Propuesta del Proyecto](https://github.com/joaquinmuzzi/psr-duelo-libgdx/wiki/Propuesta-del-Proyecto)**

Índice general de la Wiki: <https://github.com/joaquinmuzzi/psr-duelo-libgdx/wiki>

## Compilación y ejecución

### Requisitos previos

- **JDK 17 o superior.** Verificalo con `java -version`.
- **Git**, para clonar el repositorio.
- No hace falta instalar Gradle: el proyecto incluye el *Gradle Wrapper*, que descarga la versión
  correcta la primera vez que se ejecuta.

### Clonar el repositorio

```bash
git clone https://github.com/joaquinmuzzi/psr-duelo-libgdx.git
cd psr-duelo-libgdx
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

**Primera pre-entrega — configuración inicial y estructura del proyecto.**

- [x] Proyecto libGDX generado con gdx-liftoff (módulos `core` + `lwjgl3`).
- [x] Compilable y ejecutable en escritorio.
- [x] Repositorio Git con `.gitignore` para proyectos libGDX.
- [x] `README.md` y `CHANGELOG.md`.
- [x] Wiki activada con la propuesta del proyecto.
- [ ] Mecánicas de combate (posiciones de espada, estocadas, bloqueos, desarme).
- [ ] Sistema de movilidad (desplazamiento, salto, rodado).
- [ ] Estructura de partida (mejor de 5 rondas, temporizador, muerte súbita).
- [ ] Arenas y selección de mapas.
- [ ] Multijugador en red local (host / cliente).
- [ ] Menú principal, ajustes y personalización del personaje.

El registro detallado de cambios está en [CHANGELOG.md](CHANGELOG.md).
