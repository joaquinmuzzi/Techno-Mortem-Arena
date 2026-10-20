package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;

/**
 * Duelista: se mueve, salta y apunta su espada hacia un punto del mundo (el mouse).
 *
 * <p>Concentra estado (posicion, velocidad, angulo de la espada) y comportamiento (moverse, saltar,
 * estocar, dibujarse). Lo que el jugador quiere hacer llega desde afuera en una {@link FuenteEntrada}
 * (teclado y mouse locales, o paquetes de red), asi la misma clase sirve para un duelista local, uno
 * remoto y un blanco de prueba.
 *
 * <p>La espada es una {@link Hoja}: una caja rotada que apunta al punto del mouse desde el hombro. Lo
 * que se dibuja es lo que choca. Cualquier contacto de la hoja con el cuerpo del rival lo hiere; el
 * clic lanza una estocada, que adelanta la hoja y le da mas alcance un instante.
 *
 * <p>Todas las medidas y velocidades se escriben en la escala base y se multiplican por
 * {@link Arena#ESCALA}: para agrandar o achicar el juego se cambia un solo numero.
 */
public class Jugador {

    private static final float ESCALA = Arena.ESCALA;

    public static final Color TINTE_UNO = new Color(0.42f, 0.78f, 1f, 1f);
    public static final Color TINTE_DOS = new Color(1f, 0.48f, 0.42f, 1f);

    /** Ancho de la caja de colision, en pixeles. Coincide con el ancho del sprite. */
    public static final float ANCHO = 48f * ESCALA;

    /** Alto de la caja de colision, en pixeles. */
    public static final float ALTO = 96f * ESCALA;

    private static final float VELOCIDAD = 450f * ESCALA;
    public static final float IMPULSO_SALTO = 780f * ESCALA;
    private static final float GRAVEDAD = -2200f * ESCALA;

    /** Por segundo: cuanto del empuje se pierde en cada segundo. Fija la distancia total del empuje. */
    private static final float RITMO_EMPUJE = 4f;
    /** Medio segundo caminando: sale del alcance de la espada dentro de la invulnerabilidad. */
    private static final float EMPUJE_GOLPE = 0.5f * VELOCIDAD;
    /** Choque entre espadas: un empujon corto que no saca al jugador del todo. */
    private static final float EMPUJE_CHOQUE = 0.2f * VELOCIDAD;

    /** Segundos que dura una estocada, de que sale la hoja hasta que vuelve. */
    private static final float DURACION_ESTOCADA = 0.14f;
    /** Segundos desde que arranca una estocada hasta que se puede lanzar otra. */
    private static final float RECARGA_ESTOCADA = 0.08f;
    /** Altura del hombro (el pivote de la espada) sobre los pies. */
    private static final float ALTURA_HOMBRO = 56f * ESCALA;
    /** Cuanto se corre el hombro hacia el lado al que mira el jugador, desde el centro del cuerpo. */
    private static final float DESPLAZAMIENTO_HOMBRO = 10f * ESCALA;

    private final FuenteEntrada fuente;
    private final EntradaJugador entrada = new EntradaJugador();
    private final Color tinte;
    /** Vidas con las que arranca cada ronda: ver {@link #reiniciar}. */
    private final int vidaInicial;
    private final Vector2 posicion = new Vector2();
    private final Vector2 velocidad = new Vector2();
    private int vida;

    private boolean mirandoDerecha;
    private boolean enElPiso;
    private boolean vivo = true;
    /** Un blanco de prueba no tiene espada: ni hiere, ni choca con otras hojas, ni se dibuja. */
    private boolean desarmado;

    /** Hacia donde apunta la espada, en radianes (0 es hacia la derecha, positivo hacia arriba). */
    private float angulo;
    private float anguloObjetivo;
    /** El angulo al empezar el fotograma, para barrer el tajo de un fotograma al siguiente. */
    private float anguloPrevio;
    /** Segundos que le quedan a la estocada en curso; 0 si no hay ninguna. */
    private float tiempoEstocada;
    private float bloqueadoTimer;
    private float recargaEstocada;
    /** Cuanto esta adelantada la hoja, de 0 (quieta) a 1 (en lo mas fuerte de la estocada). */
    private float extension;
    private final Hoja hoja = new Hoja();
    private final Hoja hojaDeBarrido = new Hoja();
    private final Rectangle limitesPropios = new Rectangle();
    private final Rectangle limitesAjenos = new Rectangle();

    /** Entrada horizontal del fotograma: se guarda para saber si corre sin volver a leer el teclado. */
    private boolean entradaIzquierda;
    private boolean entradaDerecha;

    /** Verdadero mientras el jugador corre por el piso (hay entrada horizontal y esta apoyado). */
    private boolean corriendo;
    /** Verdadero mientras el jugador esta en el aire despues de haber dejado el piso. */
    private boolean enVuelo;
    /** Verdadero solo en el fotograma en que toca el piso tras haber estado en el aire. */
    private boolean acabaDeAterrizar;
    /** Verdadero solo en el fotograma en que su espada toca el cuerpo del rival. */
    private boolean acabaDeGolpear;
    /** Cuantas veces ocurrieron: viajan por la red para que los clientes disparen los sonidos. */
    private int contadorGolpes;
    private int contadorAterrizajes;

    /** Bloque sobre el que esta parado, si lo hay. Sirve para avisar solo al aterrizar. */
    private Bloque bloqueBajoPies;
    /** Verdadero mientras esta parado sobre la cabeza del oponente. */
    private boolean montadoSobreOponente;

    private float tiempoRestanteFlash;

    /** Velocidad horizontal del empuje por golpe o choque. Se frena sola en el tiempo. */
    private float empujeX;

    public static final float ANCHO_ESPADA = Hoja.LARGO;
    public static final float ALTO_ESPADA = Hoja.GROSOR;

    private static final float ANCHO_BRAZO = 22f * ESCALA;
    private static final float ALTO_BRAZO = 8f * ESCALA;

    /**
     * @param x posicion horizontal inicial, en pixeles desde el borde izquierdo de la arena
     * @param y posicion vertical inicial, en pixeles desde el borde inferior de la arena
     * @param mirandoDerecha hacia donde apunta la espada al empezar la ronda
     * @param tinte color con el que se pinta el sprite blanco, para distinguir a los duelistas
     */
    public Jugador(FuenteEntrada fuente, float x, float y, boolean mirandoDerecha, Color tinte, int vida) {
        this.fuente = fuente;
        this.tinte = tinte;
        this.mirandoDerecha = mirandoDerecha;
        this.posicion.set(x, y);
        this.vidaInicial = vida;
        this.vida = vida;
        this.anguloObjetivo = mirandoDerecha ? 0f : MathUtils.PI;
        this.angulo = mirandoDerecha ? 0f : MathUtils.PI;
        this.anguloPrevio = angulo;
        actualizarHoja();
    }

    /**
     * Vuelve al estado inicial para empezar una ronda nueva en el mismo mapa: posicion de spawn,
     * vida completa, la espada mirando hacia adelante y sin efectos pendientes (flash, empuje,
     * estocada). Los contadores de golpes y aterrizajes no se tocan: solo suben, y los clientes de
     * una partida en red los comparan para disparar los sonidos.
     *
     * <p>La partida al mejor de 5 resetea a los duelistas en vez de reconstruir la pantalla, asi
     * el marcador y el numero de ronda sobreviven de un duelo al siguiente.
     *
     * @param x posicion horizontal del spawn, en pixeles desde el borde izquierdo de la arena
     * @param y posicion vertical del spawn, en pixeles desde el borde inferior de la arena
     * @param mirandoDerecha hacia donde apunta la espada al empezar la ronda
     */
    public void reiniciar(float x, float y, boolean mirandoDerecha) {
        posicion.set(x, y);
        velocidad.setZero();
        vida = vidaInicial;
        this.mirandoDerecha = mirandoDerecha;
        enElPiso = false;
        vivo = true;
        angulo = mirandoDerecha ? 0f : MathUtils.PI;
        anguloObjetivo = mirandoDerecha ? 0f : MathUtils.PI;
        anguloPrevio = angulo;
        tiempoEstocada = 0f;
        recargaEstocada = 0f;
        extension = 0f;
        entradaIzquierda = false;
        entradaDerecha = false;
        corriendo = false;
        enVuelo = false;
        acabaDeAterrizar = false;
        acabaDeGolpear = false;
        bloqueBajoPies = null;
        montadoSobreOponente = false;
        tiempoRestanteFlash = 0f;
        empujeX = 0f;
        actualizarHoja();
    }

    /**
     * Avanza la simulacion del jugador un fotograma.
     *
     * <p>El orden importa: primero se lee la entrada, despues se integra la fisica (con los bloques del
     * escenario) y al final se resuelven las colisiones con el oponente.
     *
     * @param delta segundos transcurridos desde el fotograma anterior
     */
    public void actualizar(float delta, Arena arena, Jugador oponente) {
        acabaDeAterrizar = false;
        acabaDeGolpear = false;
        boolean estabaEnPiso = enElPiso;
        anguloPrevio = angulo;
        if (vivo) {
            leerEntrada();
        }
        actualizarEstocada(delta);
        integrarFisica(delta, arena, oponente);
        // La hoja se coloca despues de mover al jugador, asi sigue al hombro y al cursor del fotograma.
        apuntar();
        actualizarHoja();
        resolverColisiones(arena, oponente);
        actualizarPiso(estabaEnPiso);
        if (tiempoRestanteFlash > 0f) {
            tiempoRestanteFlash -= delta;
        }
        if (bloqueadoTimer > 0f) {
        	bloqueadoTimer -= delta;
        }
        
        if(angulo < anguloObjetivo) {
        	angulo = Math.min(angulo + (0.2f - (bloqueadoTimer > 0f ? 0.15f : 0f)), anguloObjetivo);
        }else if(angulo > anguloObjetivo) {
        	angulo = Math.max(angulo - (0.2f - (bloqueadoTimer > 0f ? 0.15f : 0f)), anguloObjetivo);
        }
    }

    /** Mata al jugador al instante. Lo usan los bloques mortales, como los pinchos. */
    public void morir() {
        vivo = false;
    }

    /**
     * Actualiza las banderas de correr y aterrizar comparando el apoyo en el piso de este fotograma
     * con el anterior.
     *
     * <p>El salto y el trampolin dejan al jugador en el aire ({@code enElPiso} en falso); cuando
     * vuelve a tocar el piso se avisa una sola vez para que suene el aterrizaje. Al empezar la ronda
     * el jugador nunca estuvo apoyado, asi que la primera caida al piso no suena.
     *
     * @param estabaEnPiso si el jugador estaba apoyado antes de la fisica de este fotograma
     */
    private void actualizarPiso(boolean estabaEnPiso) {
        if (estabaEnPiso && !enElPiso) {
            enVuelo = true;
        }
        if (enVuelo && !estabaEnPiso && enElPiso) {
            acabaDeAterrizar = true;
            contadorAterrizajes++;
            enVuelo = false;
        }
        corriendo = vivo && enElPiso && (entradaIzquierda != entradaDerecha);
    }

    /**
     * Impulsa al jugador hacia arriba con la velocidad vertical dada. Lo usan los bloques que rebotan, como
     * el trampolin: lo sacan del piso sin que cuente como un salto propio.
     */
    public void impulsar(float velocidadVertical) {
        velocidad.y = velocidadVertical;
        enElPiso = false;
        montadoSobreOponente = false;
        bloqueBajoPies = null;
    }

    /** Posicion del hombro, el pivote alrededor del que gira la espada. */
    private float pivoteX() {
        return posicion.x + ANCHO / 2f + (mirandoDerecha ? DESPLAZAMIENTO_HOMBRO : -DESPLAZAMIENTO_HOMBRO);
    }

    private float pivoteY() {
        return posicion.y + ALTURA_HOMBRO;
    }

    private void actualizarHoja() {
        hoja.colocar(pivoteX(), pivoteY(), angulo, extension * Hoja.ALCANCE_ESTOCADA);
    }

    /**
     * Gira la espada hacia el punto apuntado y hace que el jugador mire hacia ese lado. Si la entrada no
     * trae un punto (un blanco de prueba, o un jugador ya muerto), la espada se queda como estaba.
     */
    private void apuntar() {
        if (!vivo || !entrada.hayPuntero) {
            return;
        }
        mirandoDerecha = entrada.apuntaX >= posicion.x + ANCHO / 2f;
        float dx = entrada.apuntaX - pivoteX();
        float dy = entrada.apuntaY - pivoteY();
        if (dx * dx + dy * dy > 1f) {
            anguloObjetivo = (float) Math.atan2(dy, dx);
        }
    }

    /**
     * Lanza una estocada si se pidio una y la anterior ya termino y recargo. La hoja sale y vuelve
     * siguiendo una curva suave: {@code extension} sube de 0 a 1 y baja a 0 en {@link #DURACION_ESTOCADA}.
     */
    private void actualizarEstocada(float delta) {
        if (recargaEstocada > 0f) {
            recargaEstocada -= delta;
        }
        if (tiempoEstocada > 0f) {
            tiempoEstocada = Math.max(0f, tiempoEstocada - delta);
        }
        if (vivo && entrada.estocada && tiempoEstocada <= 0f && recargaEstocada <= 0f) {
            tiempoEstocada = DURACION_ESTOCADA * (bloqueadoTimer > 0f ? 1.2f : 1f);
            recargaEstocada = RECARGA_ESTOCADA * (bloqueadoTimer > 0f ? 6f : 1f);
        }
        extension = tiempoEstocada > 0f
            ? (float) Math.sin(Math.PI * (1f - tiempoEstocada / DURACION_ESTOCADA))
            : 0f;
    }

    private void leerEntrada() {
        fuente.leer(entrada);
        entradaIzquierda = entrada.izquierda;
        entradaDerecha = entrada.derecha;

        if (entradaIzquierda && !entradaDerecha) {
            velocidad.x += -(VELOCIDAD*(bloqueadoTimer > 0f ? 0.5f : 1f) + velocidad.x);
        } else if (entradaDerecha && !entradaIzquierda) {
            velocidad.x += VELOCIDAD*(bloqueadoTimer > 0f ? 0.5f : 1f) - velocidad.x;
        }

        if (enElPiso && entrada.saltar) {
            impulsar(IMPULSO_SALTO);
        }
    }

    private void integrarFisica(float delta, Arena arena, Jugador oponente) {
        // Multiplicar por delta hace que el movimiento sea independiente de los fotogramas por
        // segundo: la misma velocidad recorre la misma distancia en una maquina lenta y en una
        // rapida. Sin delta, el juego correria mas rapido en mejores placas de video.
        boolean montadoAhora = montadoSobreOponente && solapaEnX(oponente.cuerpo());
        montadoSobreOponente = montadoAhora;
        Bloque bloqueAnterior = bloqueBajoPies;
        bloqueBajoPies = null;
        if (!montadoAhora) {
            velocidad.y += GRAVEDAD * delta;
        }
        if (enElPiso && bloqueAnterior != null) {
            posicion.x += bloqueAnterior.getVelocidadHorizontal() * delta;
        }

        // Se resuelve eje por eje: primero X y despues Y, asi cada choque se corrige por separado.
        posicion.x += (velocidad.x + empujeX) * delta;
        empujeX *= (float) Math.exp(-RITMO_EMPUJE * delta);
        resolverBloquesEnX(arena);

        float yAnterior = posicion.y;
        if (montadoAhora) {
            // Parado sobre el oponente: sigue su cabeza y hereda su velocidad vertical, asi el salto
            // del de abajo se lleva tambien al de arriba.
            Rectangle cabeza = oponente.cuerpo();
            posicion.y = cabeza.y + cabeza.height;
            velocidad.y = oponente.velocidad.y;
            enElPiso = true;
        } else {
            posicion.y += velocidad.y * delta;
            enElPiso = false;
            resolverBloquesEnY(arena, yAnterior, bloqueAnterior);
            resolverAterrizajeEn(oponente, yAnterior);
        }

        if(velocidad.x > 0f) {
        	velocidad.x = Math.max(velocidad.x - 100f * ESCALA, 0);
        }else if(velocidad.x < 0f){
        	velocidad.x = Math.min(velocidad.x + 100f * ESCALA, 0);
        }
    }

    private boolean solapaEnX(Rectangle caja) {
        return posicion.x < caja.x + caja.width && posicion.x + ANCHO > caja.x;
    }

    /**
     * Saca al jugador de los bloques en horizontal, empujandolo hacia el lado mas cercano.
     * La velocidad horizontal se anula al chocar, para que un golpe no siga empujando contra la pared.
     */
    private void resolverBloquesEnX(Arena arena) {
        for (Bloque bloque : arena.getBloques()) {
            Rectangle caja = bloque.getArea();
            if (!cuerpo().overlaps(caja)) {
                continue;
            }
            bloque.alTocar(this);
            float salidaIzquierda = caja.x - ANCHO;
            float salidaDerecha = caja.x + caja.width;
            if (posicion.x - salidaIzquierda <= salidaDerecha - posicion.x) {
                posicion.x = salidaIzquierda;
            } else {
                posicion.x = salidaDerecha;
            }
            velocidad.x = 0f;
            empujeX = 0f;
        }
    }

    /**
     * Resuelve el choque vertical con los bloques segun de donde venia el jugador: si estaba por encima
     * del bloque aterriza sobre el, si estaba por debajo choca la cabeza contra su cara inferior.
     * Al aterrizar avisa al bloque, pero solo si es la primera vez que lo pisa.
     */
    private void resolverBloquesEnY(Arena arena, float yAnterior, Bloque bloqueAnterior) {
        for (Bloque bloque : arena.getBloques()) {
            Rectangle caja = bloque.getArea();
            if (!cuerpo().overlaps(caja)) {
                continue;
            }
            bloque.alTocar(this);
            boolean estabaArriba = yAnterior >= caja.y + caja.height;
            boolean estabaAbajo = yAnterior + ALTO <= caja.y;
            if (estabaArriba || (!estabaAbajo && velocidad.y <= 0f)) {
                posicion.y = caja.y + caja.height;
                velocidad.y = 0f;
                enElPiso = true;
                bloqueBajoPies = bloque;
                if (bloque != bloqueAnterior) {
                    bloque.alAterrizar(this);
                }
            } else {
                posicion.y = caja.y - ALTO;
                velocidad.y = 0f;
            }
        }
    }

    /**
     * Si el oponente esta debajo y el jugador cae sobre su cabeza, el jugador queda parado encima. Solo
     * funciona como piso: subiendo por debajo del oponente no hay choque, asi no lo bloquea.
     */
    private void resolverAterrizajeEn(Jugador oponente, float yAnterior) {
        Rectangle cabezaOponente = oponente.cuerpo();
        if (!cuerpo().overlaps(cabezaOponente)) {
            return;
        }
        if (yAnterior >= cabezaOponente.y + cabezaOponente.height) {
            posicion.y = cabezaOponente.y + cabezaOponente.height;
            velocidad.y = 0f;
            enElPiso = true;
            montadoSobreOponente = true;
        }
    }

    /**
     * Empuja al jugador una distancia fija en la direccion dada. La velocidad inicial sale de
     * {@code distancia * RITMO_EMPUJE}: integrada en el tiempo con el decaimiento de
     * {@link #integrarFisica} suma exactamente {@code distancia}, sin importar cuanto se superpusieron
     * las cajas.
     */
    private void empujar(float direccion, float distancia) {
        empujeX = direccion * distancia * RITMO_EMPUJE;
    }

    private Rectangle cuerpo() {
        return new Rectangle(posicion.x, posicion.y, ANCHO, ALTO);
    }

    private void resolverColisiones(Arena arena, Jugador oponente) {
        posicion.x = arena.limitarX(posicion.x, ANCHO);
        Hoja hojaOponente = oponente.hoja;

        Rectangle miCaja = cuerpo();
        Rectangle cajaOponente = oponente.cuerpo();
        if (miCaja.overlaps(cajaOponente)) {
            float miY0 = posicion.y, miY1 = posicion.y + ALTO;
            float suY0 = oponente.posicion.y, suY1 = oponente.posicion.y + ALTO;
            boolean seCruzanEnY = miY0 < suY1 && suY0 < miY1;
            float miX0 = posicion.x, miX1 = posicion.x + ANCHO;
            float suX0 = oponente.posicion.x, suX1 = oponente.posicion.x + ANCHO;
            float penetracionX = Math.min(miX1, suX1) - Math.max(miX0, suX0);
            float penetracionY = Math.min(miY1, suY1) - Math.max(miY0, suY0);
            // Solo se empuja de costado si la superposicion mas chica es horizontal. Si es vertical
            // (uno parado sobre la cabeza del otro), el apilado lo resuelve el aterrizaje.
            if (seCruzanEnY && penetracionX > 0f && penetracionX <= penetracionY) {
                separarDe(oponente, penetracionX, arena);
            }
        }

        // La hoja del rival empuja a mi cuerpo: no se puede atravesar una espada parado.
        if (!oponente.desarmado && hojaOponente.solapa(miCaja)) {
            float penetracionX = penetracionEnX(hojaOponente.limites(limitesAjenos), miCaja);
            if (penetracionX > 0f) {
                separarDe(oponente, penetracionX, arena);
            }
        }

        // Choque de hojas: los dos se empujan para separarse.
        if (!desarmado && !oponente.desarmado && hoja.solapa(hojaOponente)) {
            float penetracionX = penetracionEnX(hoja.limites(limitesPropios), hojaOponente.limites(limitesAjenos));
            if (penetracionX > 0f) {
                float direccion = separarDe(oponente, penetracionX, arena);
                empujar(direccion, EMPUJE_CHOQUE);
                bloqueadoTimer = 1f;
            }
        }

        // Cualquier contacto de mi hoja con el cuerpo del rival lo hiere, y si gire rapido tambien cuenta
        // lo que la hoja barrio entre este fotograma y el anterior.
        if (!desarmado && !(oponente.tiempoRestanteFlash > 0f) && vivo && golpea(cajaOponente)) {
            oponente.activarFlash(1f, this);
            acabaDeGolpear = true;
            contadorGolpes++;
        }
        if (posicion.y < -ALTO) {
            vivo = false;
        }
    }

    /**
     * Corre a este jugador la mitad de la penetracion, lejos del oponente, sin sacarlo de la arena.
     *
     * @return la direccion en que se corrio (-1 izquierda, 1 derecha)
     */
    private float separarDe(Jugador oponente, float penetracionX, Arena arena) {
        float miCentro = posicion.x + ANCHO / 2f;
        float suCentro = oponente.posicion.x + ANCHO / 2f;
        float direccion = (miCentro < suCentro) ? -1f : 1f;
        posicion.x += direccion * (penetracionX / 2f);
        posicion.x = arena.limitarX(posicion.x, ANCHO); // por si el empujon te saca por la pared
        return direccion;
    }

    private static float penetracionEnX(Rectangle a, Rectangle b) {
        return Math.min(a.x + a.width, b.x + b.width) - Math.max(a.x, b.x);
    }

    /** Verdadero si la hoja toca el cuerpo, ahora o en algun punto del giro desde el fotograma anterior. */
    private boolean golpea(Rectangle cuerpoAjeno) {
        if (hoja.solapa(cuerpoAjeno)) {
            return true;
        }
        int pasos = Hoja.pasosDeBarrido(anguloPrevio, angulo);
        float avance = extension * Hoja.ALCANCE_ESTOCADA;
        for (int i = 1; i < pasos; i++) {
            hojaDeBarrido.colocar(pivoteX(), pivoteY(), Hoja.anguloDeBarrido(anguloPrevio, angulo, i, pasos), avance);
            if (hojaDeBarrido.solapa(cuerpoAjeno)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dibuja al duelista y su espada.
     *
     * <p>Debe llamarse entre {@code batch.begin()} y {@code batch.end()}.
     *
     * <p>El sprite se dibuja en blanco y se colorea con {@code batch.setColor()}: un unico archivo
     * de arte sirve para los dos jugadores. Al terminar hay que restaurar el color a blanco, porque
     * el tinte queda activo y afectaria a todo lo que se dibuje despues.
     */
    public void dibujar(SpriteBatch batch, RecursosGraficos recursos) {
    	Color colorActual;
    	if (!vivo) {
    	    colorActual = Color.DARK_GRAY;
    	} else if (tiempoRestanteFlash > 0f && ((int) (tiempoRestanteFlash*20) % 2 == 0)) {
    	    colorActual = Color.YELLOW;
    	} else {
    	    colorActual = tinte;
    	}
    	batch.setColor(colorActual);
        dibujarSprite(batch, recursos.jugador, posicion.x, posicion.y, ANCHO, ALTO);
        if (!desarmado) {
            dibujarBrazo(batch, recursos);
            dibujarEspada(batch, recursos);
        }
        batch.setColor(Color.WHITE);
    }

    private void activarFlash(float duracion, Jugador atacante) {
        tiempoRestanteFlash = duracion; // duración del flash, ajustable
        recibirImpacto(atacante);
    }

    /** Verdadero mientras el duelista siga en pie: al morir empieza la cuenta de la ronda. */
    public boolean isVivo() {
        return vivo;
    }

    private void recibirImpacto(Jugador atacante) {
    	float miCentro = posicion.x + ANCHO / 2f;
	    float suCentro = atacante.getPosicion().x + ANCHO / 2f;
	    float direccion = (miCentro < suCentro) ? -1f : 1f;
	    empujar(direccion, EMPUJE_GOLPE);
	    velocidad.y += 400f * ESCALA;
    	if(vida > 1) {
    		vida -= 1;
    	}
    	else {
    		vida = 0;
    		vivo = false;
    	}
    }

    /** Dibuja el antebrazo y la espada: salen de la base de la hoja y giran con ella hacia el cursor. */
    private void dibujarBrazo(SpriteBatch batch, RecursosGraficos recursos) {
        dibujarGirado(batch, recursos.brazo, ANCHO_BRAZO, ALTO_BRAZO);
    }

    private void dibujarEspada(SpriteBatch batch, RecursosGraficos recursos) {
        dibujarGirado(batch, recursos.espada, ANCHO_ESPADA, ALTO_ESPADA);
    }

    /**
     * Dibuja un sprite que mira hacia la derecha, girado {@code angulo} alrededor de su extremo izquierdo,
     * que se apoya en la base de la hoja. Al apuntar hacia la izquierda se espeja en vertical para que
     * no quede cabeza abajo.
     */
    private void dibujarGirado(SpriteBatch batch, Texture textura, float ancho, float alto) {
        boolean haciaLaIzquierda = Math.cos(angulo) < 0.0;
        batch.draw(textura, hoja.getBaseX(), hoja.getBaseY() - alto / 2f,
            0f, alto / 2f, ancho, alto, 1f, 1f, angulo * MathUtils.radiansToDegrees,
            0, 0, textura.getWidth(), textura.getHeight(), false, haciaLaIzquierda);
    }

    /**
     * Dibuja una textura espejada en horizontal cuando el jugador mira a la izquierda.
     *
     * <p>Esta sobrecarga de {@code draw} recibe la region de origen y dos banderas de espejado. Es
     * la forma de reutilizar un sprite que solo esta dibujado hacia un lado.
     */
    private void dibujarSprite(SpriteBatch batch, Texture textura,
                               float x, float y, float ancho, float alto) {
        batch.draw(textura, x, y, ancho, alto,
            0, 0, textura.getWidth(), textura.getHeight(),
            !mirandoDerecha, false);
    }

    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }

    /** Verdadero mientras el jugador corre por el piso: hay entrada horizontal y esta apoyado. */
    public boolean estaCorriendo() {
        return corriendo;
    }

    /** Verdadero solo en el fotograma en que la espada toca el cuerpo del rival y le hace dano. */
    public boolean acabaDeGolpear() {
        return acabaDeGolpear;
    }

    /** Verdadero solo en el fotograma en que toca el piso tras haber estado en el aire. */
    public boolean acabaDeAterrizar() {
        return acabaDeAterrizar;
    }

    /** Copia en {@code destino} todo lo necesario para dibujar a este jugador. */
    public void exportar(EstadoJugador destino) {
        destino.x = posicion.x;
        destino.y = posicion.y;
        destino.vida = vida;
        destino.mirandoDerecha = mirandoDerecha;
        destino.vivo = vivo;
        destino.corriendo = corriendo;
        destino.anguloEspada = angulo;
        destino.anguloObjetivo = anguloObjetivo;
        destino.extension = extension;
        destino.tiempoRestanteFlash = tiempoRestanteFlash;
        destino.contadorGolpes = contadorGolpes;
        destino.contadorAterrizajes = contadorAterrizajes;
    }

    /**
     * Pisa el estado de este jugador con el que mando el anfitrion. Lo usa el cliente: su copia no
     * simula nada, solo se dibuja.
     */
    public void importar(EstadoJugador origen) {
        posicion.set(origen.x, origen.y);
        vida = origen.vida;
        mirandoDerecha = origen.mirandoDerecha;
        vivo = origen.vivo;
        corriendo = origen.corriendo;
        angulo = origen.anguloEspada;
        anguloObjetivo = origen.anguloObjetivo;
        extension = origen.extension;
        tiempoRestanteFlash = origen.tiempoRestanteFlash;
        contadorGolpes = origen.contadorGolpes;
        contadorAterrizajes = origen.contadorAterrizajes;
        actualizarHoja();
    }

    /** Cantidad de golpes que dio desde que empezo; sube de a uno en cada golpe. */
    public int getContadorGolpes() {
        return contadorGolpes;
    }

    /** Cantidad de aterrizajes desde que empezo; sube de a uno en cada aterrizaje. */
    public int getContadorAterrizajes() {
        return contadorAterrizajes;
    }

    public int getVida() {
        return vida;
    }

    /** Quita o devuelve la espada. El blanco del modo de prueba esta desarmado. */
    public void setDesarmado(boolean desarmado) {
        this.desarmado = desarmado;
    }

    /** Hacia donde apunta la espada, en radianes. */
    public float getAngulo() {
        return angulo;
    }

    /** Cuanto esta adelantada la hoja por la estocada: 0 quieta, 1 en lo mas fuerte. */
    public float getExtension() {
        return extension;
    }

    /** La hoja de la espada tal como esta ahora; es la misma forma que se dibuja y que choca. */
    public Hoja getHoja() {
        return hoja;
    }

    /** Copia defensiva: devolver el {@code Vector2} interno permitiria moverlo desde afuera. */
    public Vector2 getPosicion() {
        return new Vector2(posicion);
    }
}
