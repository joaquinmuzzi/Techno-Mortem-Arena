# Changelog

Todos los cambios significativos de **Techno Mortem Arena** se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto adhiere a
[Versionado Semántico](https://semver.org/lang/es/).

## [No publicado]

### Añadido

- Música de combate (`assets/music/combate.mp3`, "Glorious Morning"): suena en bucle durante la partida
  (modo de prueba y en red) y sigue sin cortarse al reiniciar. En el menú suena la del menú y en el editor
  ninguna. La opción MÚSICA de Opciones apaga las dos. `RecursosAudio.pedirMusica` reemplaza a
  `pedirMusicaMenu`: cada pantalla dice qué música quiere (`MENU`, `COMBATE` o `NINGUNA`).

## [0.5.0] - 2026-11-05

Armas de fuego y espada láser, mejoras que caen del cielo, mapa el doble de grande, fuentes pixeladas
y efectos de sonido de las armas.

### Añadido

- Música del menú (`assets/music/menu.mp3`): suena en bucle en el menú, la elección de mapa, el multijugador y
  las opciones sin cortarse al pasar de una a otra, y se apaga en la partida y en el editor.
- Pantalla de opciones (`pantallas.PantallaOpciones`, botón OPCIONES del menú principal): prende y apaga la
  música y los efectos de sonido. Por defecto suena todo; lo elegido se guarda en las preferencias de libGDX
  (`OpcionesAudio`) y se mantiene al cerrar el juego. Con los efectos apagados no suenan ni los botones ni los
  golpes, los pasos o los aterrizajes.
- Fuentes nuevas, pixeladas y sin suavizado (`assets/fuentes/`, hechas con `herramientas/hornear_fuentes.py`):
  Orbitron en negrita para los títulos y los botones, y Share Tech Mono para el resto. `pantallas.Ui` elige
  el tamaño horneado más cercano a cada escala. Reemplazan a la fuente que traía libGDX, cuyos bordes
  suavizados se veían como un halo gris al agrandarla.
- Título del menú nuevo: "TECHNO MORTEM" con MORTEM en rojo y, abajo, "ARENA" en azul con las letras
  separadas.

- Rifle láser (`TipoArma.RIFLE_LASER`, `sprites/rifle_laser.png`): 20 rayos largos y muy rápidos, sin caída,
  de un sexto de corazón cada uno; no respetan la invulnerabilidad después de un golpe y no se reflejan.
- Lanzacohetes (`TipoArma.LANZACOHETES`, `sprites/lanzacohetes.png` y `sprites/cohete.png`): un cohete medio
  lento con poca caída que explota al chocar contra un bloque o un cuerpo. La explosión se ve crecer y
  apagarse, saca un corazón a todos los que tengan el cuerpo a menos de 4 celdas (también al que lo tiró) y
  rompe los bloques de la celda del impacto y las ocho de alrededor (`Arena.sinCeldas`), menos las paredes de
  los costados y la fila de abajo de todo. Los bloques vuelven al empezar la ronda siguiente.
- Espada láser roja (`TipoArma.ESPADA_LASER`): pega un corazón y un tercio, no se puede desarmar (ni con la
  hoja ni con un puñetazo), atraviesa a la espada común sin chocar ni empujar y, tirada, le afecta menos la
  gravedad. Es muy rara: una de cada 12 armas que caen solas, y nunca dos a la vez.
- Reflejar con la hoja: las balas, los perdigones y los cohetes que tocan la hoja de una espada rebotan
  espejados según su ángulo (la velocidad a lo largo de la hoja se conserva y la perpendicular se da vuelta)
  y pasan a ser de quien los reflejó. Con la espada común pierden un 20 % de velocidad y se desvían un poco
  al azar; con la láser rebotan perfecto.
- Las armas que caen solas ahora se reparten entre pistola, escopeta, rifle láser y lanzacohetes (la que haya
  menos en el suelo). En la interfaz, las balas se achican para que entren las 20 del rifle. Protocolo de red
  versión 10 (no es compatible con la 9): viajan los proyectiles nuevos, la explosión y las celdas rotas.
- Mejoras (`entidades.PowerUp`): cada 12 s cae del cielo una (como mucho 2 en el mapa) y la agarra quien pasa
  por encima, tenga o no un arma. Duran 10 s: VELOCIDAD corre y rueda un 40 % más rápido, SALTO salta un
  35 % más alto y PUNTERIA dispara más seguido y casi no abre los perdigones de la escopeta. Cada una tiene
  su ícono (`sprites/powerup_*.png`) y en la esquina de cada jugador se ve cuáles tiene y cuánto les queda.
  Se borran al empezar cada ronda. Protocolo de red versión 9 (no es compatible con la 8).
- Mapa el doble de grande: 180 x 101 celdas (antes 90 x 50), con las celdas del mismo tamaño. Los mapas
  predefinidos mantienen el tamaño de sus elementos y se reparten en el mapa más grande (escaleras en las dos
  puntas, tres trampolines con su plataforma) y los spawns por defecto quedan a la misma distancia entre sí
  que antes, cerca del centro.
- `pantallas.CamaraDuelo`: la cámara sigue a los dos duelistas y se aleja cuando se separan, hasta mostrar el
  mapa entero, sin mostrar nunca lo que hay fuera; durante la edición entre rondas muestra el mapa entero.
- Editor de niveles con cámara propia: la rueda acerca y aleja hacia el cursor, las flechas o WASD y el botón
  del medio la mueven y `F` muestra el mapa entero. Las celdas se ven siempre cuadradas y la grilla es nueva:
  fondo del mapa distinto de lo de afuera, líneas finas por celda, una línea guía cada 10 celdas, la del
  centro marcada y el borde del mapa.
- Editor de niveles: las herramientas pasan a un panel lateral con barra de desplazamiento, que se abre y se
  cierra con el botón hamburguesa de la barra de arriba (o la tecla `TAB`). Cada herramienta muestra su color
  y su tecla; la barra de arriba queda en una sola fila con las acciones y la herramienta elegida, y la
  grilla gana espacio.
- `pantallas.BarraDeDesplazamiento`: barra vertical reutilizable (manija que se arrastra, clic en la pista
  para pasar de página), la usan la lista de "Mis mapas" y el panel del editor.
- Bloques spawner de armas (`mundo.SpawnerArma`, `P` y `E` en la grilla): en el editor, las herramientas
  "8 PISTOLAS" y "9 ESCOPETAS" ponen un bloque sólido de 3x3 que hace aparecer su arma encima cada 5 s, una
  sola a la vez (si ya hay una encima, espera a que la agarren). También están entre los objetos que se
  sortean para colocar entre rondas. Viajan por la red con el mapa.
- Interfaz de arma: en la esquina de arriba de cada jugador, el arma que tiene en la mano sobre un fondo con
  brillo y, debajo, las balas como rectángulos verticales (llenos los que quedan), en el estilo de las vidas.
  Reemplaza el texto "BALAS n". Mientras se ve el cartel de controles, la del Jugador 1 va debajo.
- Desarme con la espada: si el último tercio de tu hoja pega en el primer tercio de la del rival mientras
  la movés (giro rápido o estocada), su espada sale volando hacia arriba sin herir a nadie y cae al mapa.
  `entidades.Hoja.puntoDeContacto` calcula dónde se tocan las dos hojas (los puntos más cercanos entre sus
  ejes), en lugar de partir cada hoja en tres cajas.
- Las armas de fuego tiradas o que se caen de la mano rebotan y quedan en el mapa con las balas que tenían.
  Sin balas, o si se van del mapa, desaparecen.
- Sprites propios para la pistola, la escopeta y el halo de las armas que se pueden agarrar
  (`sprites/pistola.png`, `sprites/escopeta.png`, `sprites/brillo.png`).
- Escopeta (`entidades.TipoArma.ESCOPETA`): 3 disparos de 5 perdigones en abanico, de corto alcance, que
  sacan un sexto de corazón cada uno. En el mapa aparecen pistolas o escopetas al azar.
- Puñetazo: sin arma, el clic izquierdo pega con la mano. Saca un sexto de corazón (menos que una bala),
  empuja al rival y una de cada tres veces le hace soltar el arma, que cae a sus pies para que la agarre
  cualquiera.
- Menú principal: los dos duelistas (del color de cada jugador) en guardia a los costados de los botones,
  con las espadas apuntándose y un vaivén suave, parados sobre una franja de piso.
- Armas. La vida pasa a contarse en tercios de corazón (3 corazones = 9 tercios): la espada saca un
  corazón y una bala un tercio, y el HUD llena cada corazón por tercios. Con clic derecho se tira el arma
  hacia el cursor (`entidades.Proyectil`): vuela, cae y hiere si toca al rival; a los 1,5 s aparece una
  espada nueva en la mano. `entidades.ArmasEnJuego` hace aparecer una pistola cada 8 s en un lugar al
  azar sobre un bloque (como mucho 2, fuera de las zonas de los spawns); se agarra pasando por encima y
  reemplaza lo que se tenía. La pistola (`entidades.TipoArma`) trae 6 balas, dispara con el clic
  izquierdo y sin balas vuelve a la espada. Funciona en el modo de prueba y en red.
- Efectos de sonido de las armas: `pistol-shot.mp3` y `shotgun-blast.mp3` al disparar la pistola y la
  escopeta, `sword-equipped.mp3` al recoger la espada, `Weapon-Equip.mp3` al recoger un arma de fuego y
  `sword-falling.mp3` cada vez que la espada tirada toca el piso, también en los rebotes. La espada con
  la que arranca la ronda no suena porque no se recoge del suelo. Los eventos viajan por la red, así el
  cliente también escucha lo que hace el otro jugador.
- Tests JUnit 5: `ArmasTest` (tercios, tirar, reaparición, pistola, balas, aparición y recogida) y los de
  las armas en el protocolo.
- Edición del nivel entre rondas (en Multijugador, al estilo *Ultimate Chicken Horse*): tras el resultado
  de una ronda con ganador, el perdedor recibe 3 objetos al azar de los 8 de `partida.PiezaNivel` (fila de
  pinchos, trampolín, bloque, rectángulo, esquina, cinta izquierda, cinta derecha y pinchos con cintas),
  elige uno y lo coloca en 20 s. Si se acaba el tiempo se coloca solo: el elegido donde estaba el cursor
  (o en el lugar válido más cercano), o uno al azar en un lugar al azar si no había elegido. No se puede
  pisar lo que ya hay ni colocar a menos de 4 celdas de los spawns o del centro de la muerte súbita.
  `partida.ColocadorPiezas` tiene las reglas, `partida.EdicionNivel` la lleva del lado del anfitrión y
  `pantallas.PanelEdicion` dibuja las tarjetas, la vista previa (verde o roja) y las zonas prohibidas.
  La revancha vuelve al mapa original.
- Tests JUnit 5: `EdicionNivelTest` (formas, sorteo, reglas, autocolocado, fase de edición) y los de la
  edición en el protocolo y por loopback.
- Rodar con `Shift` izquierdo: una rodada corta (0,35 s, recarga de 0,9 s) en la dirección del movimiento
  o, si el jugador está quieto, hacia donde mira. Mientras rueda la caja de colisión mide la mitad y se
  va más rápido, pero no es invulnerable. Solo se puede desde el piso, y si encima hay un bloque sigue
  agachado hasta tener lugar para pararse. `entrada.EntradaJugador.rodar` y `entidades.Jugador.estaRodando()`.
- Estructura de partida al mejor de 5 rondas (por PR de @juarezmateo, integrada con el combate con mouse
  y la red). `partida.MarcadorPartida` lleva el conteo de rondas ganadas y termina la partida cuando
  alguien llega a 3; `partida.RondasPartida` es la lógica pura de la ronda, la espera entre rondas y la
  victoria. Cuando un duelista cae se anota la ronda, hay una pausa breve con el resultado y la siguiente
  arranca con los dos de vuelta en sus spawns. En empate (cayeron los dos en el mismo fotograma) la ronda
  se vuelve a jugar sin sumar. Se juega en Multijugador: el anfitrión lleva la cuenta y el estado de la
  partida (`partida.EstadoPartida`) viaja al cliente junto con el de los jugadores.
- Cartel de victoria con el ganador de la partida, el marcador final y botones de revancha (la pide el
  anfitrión), salir de la sala o volver al menú principal.
- HUD durante la ronda: las vidas restantes de cada duelista (del color de cada uno, apagadas cuando se
  pierden) a los costados del centro, y arriba al centro el número de ronda y el marcador. Las vidas van
  al lado del centro y no en las esquinas para no pisar el cartel de controles.
- `pantallas.PantallaSeleccionMapa`: sección "MIS MAPAS" con los mapas guardados por el editor, que se
  juegan igual que los predefinidos (también al crear una sala). La lista se desplaza con la rueda del
  mouse cuando hay más mapas que filas en pantalla.
- Pausa con botón "REINICIAR RONDA" (modo de prueba y anfitrión): reinicia la ronda actual sin tocar el
  marcador de la partida.
- `entidades.Jugador`: `reiniciar()` para empezar una ronda nueva en el mismo mapa (spawn, vidas completas
  y sin efectos pendientes), y los accesores `isVivo()` y `getVida()` para el HUD y el fin de ronda.
- Tests JUnit 5: `MarcadorPartidaTest`, `RondasPartidaTest`, `JugadorTest` y los del estado de la partida
  en el protocolo.
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
- Efectos de sonido de interfaz: `sounds/Boton.wav` suena al pasar el cursor por encima de un botón y
  `sounds/Boton Seleccionado.mp3` al hacer clic. Los dispara `pantallas.Ui` desde el propio dibujo del
  botón, así que las pantallas solo habilitan el audio con `Ui.setAudio`. Suenan en el menú, la
  selección de mapa, el multijugador, la pausa y los carteles de la arena; el editor de niveles queda
  en silencio a propósito.

### Cambiado

- Protocolo de red versión 11: el estado de cada jugador suma los contadores de disparos de pistola y de
  escopeta y los de armas recogidas (espada y armas de fuego), y el de las armas fuera de las manos suma el
  de espadas que tocan el piso. No es compatible con la versión 10.
- Botones: fondo apenas marcado y una raya abajo, azul con el mouse encima y gris si no.
- En los mapas que no marcan los spawns, cada duelista aparece a 15 celdas de su pared, parado sobre lo que
  haya ahí (en la escalera, sobre el escalón bajo).
- Las armas que aparecen solas ya no aparecen de golpe en el piso: caen desde arriba del mapa sobre el lugar
  elegido y se agarran recién cuando se apoyan (en el primer bloque que encuentran). Si caen por un agujero,
  desaparecen. El arma que se le cae a un duelista en el aire también cae hasta el piso. Los spawners de
  bloque siguen poniendo su arma directamente encima.
- Protocolo de red versión 8: el saludo lleva dos máscaras de 64 bits (hasta 128 filas de mapa), el largo de
  cada fila codificada va en dos bytes y las columnas de la edición se leen sin signo (hay más de 127). No
  es compatible con la versión 7. Los mapas guardados con el tamaño anterior ya no se cargan.
- Edición entre rondas: las piezas se pueden colocar encima de lo que ya hay (piso, bloques u otras piezas,
  que quedan reemplazados; los huecos de la forma, como el de la esquina, no borran nada). Las zonas
  prohibidas son las de los spawns y la del centro, donde la muerte súbita pone a los dos duelistas.
- Quien mira la edición del rival ve el mapa oscurecido, para que se entienda que no es su turno; las
  tarjetas y la vista previa de lo que elige y coloca el rival van encima y se ven claras.
- Las vidas del Jugador 1 (azul) se vacían de izquierda a derecha, en espejo de las del Jugador 2: antes cada
  corazón a medias se vaciaba del lado del centro.
- Las armas se agarran solo con la mano vacía (la espada también cuenta): pasar por encima con algo en la
  mano no hace nada. Para cambiar de arma, primero hay que tirar la que se tiene.
- Los perdigones de la escopeta no respetan ni dejan invulnerabilidad: cada perdigón que toca saca su parte,
  aunque el rival venga de recibir otro golpe (antes la invulnerabilidad de un espadazo, un puñetazo o una
  bala podía comerse varios perdigones de cerca).
- Escopeta: los perdigones de un mismo disparo ya no se frenan con la invulnerabilidad que deja el primero
  (cada disparo tiene su número y los demás perdigones de ese disparo la atraviesan), así un escopetazo de
  cerca pega con los cinco. El abanico es más cerrado (unos 17 grados), cada perdigón sale corrido un poco
  al azar en ángulo y velocidad, y llegan más lejos (0,55 s de vuelo, antes 0,35 s).
- La espada en la mano se dibuja en blanco, como las armas de fuego, en lugar de teñirse con el color del
  jugador.
- El arma tirada rebota mucho menos (conserva un 25 % por pique) y, si le pega al rival, cae casi recta a
  sus pies. Solo se queda quieta apoyada sobre un bloque: ya no se congela en el aire después de un
  rebote alto. Se tira más despacio, para que sea un último recurso y no mejor que la estocada.
- La pistola y la escopeta aparecen alternadas (la que haya menos en el suelo): antes, con dos pistolas en el
  piso no aparecía más nada y la escopeta podía no salir nunca.
- La vida se cuenta en sextos de corazón (antes en tercios) para que el puñetazo y los perdigones hagan
  menos que una bala.
- Las balas caen un poco con la gravedad, así es más difícil acertar de lejos.
- La espada tirada cae más, rebota en el piso y las paredes y queda en el mapa como arma para agarrar: ya
  no reaparece sola en la mano. Solo la levanta quien tiene las manos vacías; quien agarra un arma de fuego
  con la espada en la mano la deja en el piso. Un arma de fuego sin balas deja las manos vacías (antes
  volvía la espada).
- Protocolo de red versión 7: la vida en sextos, la escopeta, los perdigones y la espada en el suelo. No es
  compatible con la versión 6.
- Retardo de la espada: la hoja ya no salta al ángulo del cursor, gira hacia él como mucho 12 radianes por
  segundo (equivale a 0,2 por fotograma a 60 FPS), siempre por el arco más corto y con el mismo retardo a
  cualquier cantidad de fotogramas por segundo. Al chocar dos hojas los duelistas quedan bloqueados 1 s: se
  mueven, giran (3 rad/s) y estocan más lento. La estocada dura 0,14 s con 0,08 s de recarga y llega un poco
  más lejos.
- Protocolo de red versión 6: la entrada del cliente suma un contador de tirar el arma; el estado de cada
  jugador, el arma en la mano y las balas (y la vida va en tercios); y el paquete de estado, los
  proyectiles y las armas en el suelo. No es compatible con la versión 5.
- Protocolo de red versión 5: la entrada del cliente suma el objeto elegido y un contador de colocar, y
  el estado de la partida la edición (editor, opciones, elección, cursor, tiempo y piezas colocadas).
  No es compatible con la versión 4.
- `red.ClienteSala` copia todo el estado de la partida que llega: antes no copiaba los tiempos de la ronda
  y de la muerte súbita, y el cliente mostraba siempre los valores iniciales.
- Protocolo de red versión 4: la entrada del cliente suma un contador de rodar y el estado del jugador
  una bandera de "rodando". No es compatible con la versión 3.
- Protocolo de red versión 3: el estado que manda el anfitrión incluye el de la partida (etapa, ronda,
  marcador y ganador de la ronda). No es compatible con la versión 2.
- `pantallas.PantallaArena`: corrige el dibujado con recursos ya liberados al salir de la pausa hacia el
  menú principal o la selección de mapa (antes, el mismo fotograma seguía dibujando con el `SpriteBatch`
  recién disposeado). Al morir, la vida del duelista queda en cero, para que el HUD muestre el estado real.
  ESC solo pausa mientras la ronda está en juego.
- Protocolo de red versión 2: la entrada lleva el punto del mouse (dos enteros de 16 bits) y un
  contador de estocadas, y el estado lleva el ángulo y la extensión de la espada. No es compatible
  con la versión 1: las dos instancias tienen que ser de la misma versión del juego.
- El botón "Jugar" del menú pasa a ser "Modo de prueba". El modo local de dos jugadores en un teclado
  se reemplaza por un jugador con mouse contra un blanco; el 1 contra 1 es por Multijugador.
- `PantallaArena` tiene tres modos (de prueba, anfitrión y cliente) y `PantallaSeleccionMapa` acepta qué
  hacer con el mapa elegido.
- `CONTRIBUTING.md`: todo cambio llega por pull request con squash, se documentan las
  verificaciones y las reglas para asistentes de IA.
- `RecursosAudio` carga y libera los dos sonidos de interfaz junto con los efectos de la ronda.
- Se reemplazó la pisada de correr (`Running On Metal Grate.mp3`, 19.8 s) por `Running.mp3`, una
  pisada corta de 0.34 s, y el aterrizaje (`Landing-Jump.mp3`) por `Landing.wav`. Así el bucle de
  correr no arrastra un fragmento largo y se nota cada pisada.
- JUnit pasa de 5.11.4 a 6.1.3. Como JUnit 6 exige Java 17, solo los tests se compilan con
  `--release 17` (`core/build.gradle`); el bytecode del juego sigue siendo Java 8.

### Corregido

- Elegir mapa: el rótulo "PREDEFINIDOS" quedaba tapado por el botón "1 - ARENA CLASICA"; el título y los
  rótulos de las columnas subieron.
- Red: cuando un jugador se iba o llegaba a la sala, las armas del suelo y las que volaban quedaban en la
  partida nueva hasta el primer reinicio de ronda. Ahora la partida arranca sin armas (lo mismo en la
  revancha).
- Selección de mapa, "Mis mapas": con más de 5 mapas guardados, las filas se caían fuera de la pantalla
  apenas aparecían (el rectángulo de cada fila se usaba también como punto de partida de la lista, y
  terminaba en la última fila). Además, la lista se movía con el movimiento del mouse y no con la rueda.
  Ahora entran 7 mapas a la vez con texto más chico, y el resto se ve con la rueda o la barra de la derecha.
- Rodar en red: `red.ClienteSala` no copiaba la bandera `rodando` del estado que llega, así que en el
  cliente nadie se veía agachado al rodar (en el anfitrión sí).
- Reloj de la ronda: en el modo de prueba y en el anfitrión mientras espera rival no hay rondas, y el
  reloj quedaba clavado en 60 s. Ahora solo se muestra durante un duelo en red.

### Quitado

- Las tres alturas de guardia (`AlturaEspada`) y sus teclas (`E`/`Q`, `O`/`L`), la estocada con tecla
  sostenida y el Jugador 2 con teclado (flechas, `K`).
- `entrada.EntradaTeclado`, reemplazada por `EntradaLocal`.

### Por hacer

- Estocadas, bloqueos por coincidencia de altura y desarme.
- Lanzamiento de espada y modo cuerpo a cuerpo (puños y patadas).
- Rodado para esquivar ataques.
- Temporizador por ronda y muerte súbita, dentro de la partida al mejor de 5 ya implementada.
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

[No publicado]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.4.1...v0.5.0
[0.4.1]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.2...v0.3.0
[0.2.2]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/joaquinmuzzi/Techno-Mortem-Arena/releases/tag/v0.1.0
