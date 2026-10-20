package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.EntradaTeclado;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;

/**
 * Duelista controlado por un jugador humano.
 *
 * <p>Concentra estado (posicion, velocidad, altura de la guardia) y comportamiento (moverse, saltar,
 * dibujarse). Las teclas llegan desde afuera en un objeto {@link Controles}, asi la misma clase
 * sirve para los dos duelistas sin duplicar codigo.
 *
 * <p>Todas las medidas y velocidades se escriben en la escala base y se multiplican por
 * {@link Arena#ESCALA}, asi el duelista crece junto con la arena.
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

    private final FuenteEntrada fuente;
    private final EntradaJugador entrada = new EntradaJugador();
    private final Color tinte;
    private final Vector2 posicion = new Vector2();
    private final Vector2 velocidad = new Vector2();
    private int vida;

    private AlturaEspada alturaEspada = AlturaEspada.PECHO;
    private boolean mirandoDerecha;
    private boolean enElPiso;
    private boolean estocada;
    private boolean vivo = true;

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

    private float anguloEspada = 0f;
    private float anguloObjetivo = 0f;
    private float tiempoParaNuevoObjetivo = 0f;

    private float tiempoParaRecuperarAtaque = 0f;

    private static final float VELOCIDAD_ANGULO = 6f;

    private float tiempoRestanteFlash;

    /** Velocidad horizontal del empuje por golpe o choque. Se frena sola en el tiempo. */
    private float empujeX;

    public static final float ANCHO_ESPADA = 56f * ESCALA;
    public static final float ALTO_ESPADA = 10f * ESCALA;

    private static final float ANCHO_BRAZO = 22f * ESCALA;
    private static final float ALTO_BRAZO = 8f * ESCALA;

    /**
     * @param x posicion horizontal inicial, en pixeles desde el borde izquierdo de la arena
     * @param y posicion vertical inicial, en pixeles desde el borde inferior de la arena
     * @param mirandoDerecha hacia donde apunta la espada al empezar la ronda
     * @param tinte color con el que se pinta el sprite blanco, para distinguir a los duelistas
     */
    public Jugador(Controles controles, float x, float y, boolean mirandoDerecha, Color tinte, int vida) {
        this(new EntradaTeclado(controles), x, y, mirandoDerecha, tinte, vida);
    }

    /** Igual que el anterior, pero la entrada puede venir de cualquier {@link FuenteEntrada}, como la red. */
    public Jugador(FuenteEntrada fuente, float x, float y, boolean mirandoDerecha, Color tinte, int vida) {
        this.fuente = fuente;
        this.tinte = tinte;
        this.mirandoDerecha = mirandoDerecha;
        this.posicion.set(x, y);
        this.vida = vida;
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
    	if(vivo) {
    		leerEntrada();
    	}
        integrarFisica(delta, arena, oponente);
        resolverColisiones(arena, oponente);
        actualizarPiso(estabaEnPiso);
        if(tiempoRestanteFlash > 0f) {
        	tiempoRestanteFlash -= delta;
        }

        if(tiempoParaRecuperarAtaque > 0f) {
        	tiempoParaRecuperarAtaque -= delta;
        }
        tiempoParaNuevoObjetivo -= delta;
        if (tiempoParaNuevoObjetivo <= 0f) {
            anguloObjetivo = (float) (Math.random() * 10f - 5f); // nuevo objetivo, por ej. entre -10 y 10 grados
            tiempoParaNuevoObjetivo = 0.4f; // elige un objetivo nuevo cada 0.3s, no cada fotograma
        }
        anguloEspada += (anguloObjetivo - anguloEspada) * VELOCIDAD_ANGULO * delta;
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

    public Rectangle getCajaEspada() {
        float y = posicion.y + alturaEspada.getDesplazamientoY();
        float extension = estocada ? 24f * ESCALA : 0f;
        float x = mirandoDerecha
            ? posicion.x + ANCHO - 14f * ESCALA + extension
            : posicion.x - ANCHO_ESPADA + 14f * ESCALA - extension;
        return new Rectangle(x, y, ANCHO_ESPADA, ALTO_ESPADA);
    }

    private void leerEntrada() {
        fuente.leer(entrada);
        entradaIzquierda = entrada.izquierda;
        entradaDerecha = entrada.derecha;
        estocada = entrada.estocada;

        if (entradaIzquierda && !entradaDerecha) {
            velocidad.x += -(VELOCIDAD+velocidad.x);
            mirandoDerecha = false;
        } else if (entradaDerecha && !entradaIzquierda) {
            velocidad.x += VELOCIDAD-velocidad.x;
            mirandoDerecha = true;
        }

        if (enElPiso && entrada.saltar) {
            impulsar(IMPULSO_SALTO);
        }
        if (entrada.subirEspada) {
            alturaEspada = alturaEspada.subir();
        }
        if (entrada.bajarEspada) {
            alturaEspada = alturaEspada.bajar();
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
        oponente.getPosicion();
        Rectangle cajaEspadaOponente = oponente.getCajaEspada();

        Rectangle miCaja = new Rectangle(posicion.x, posicion.y, ANCHO, ALTO);
        Rectangle cajaOponente = new Rectangle(oponente.getPosicion().x, oponente.getPosicion().y, ANCHO, ALTO);
        if (miCaja.overlaps(cajaOponente)) {
        	float miY0 = posicion.y,             miY1 = posicion.y + ALTO;
        	float suY0 = oponente.getPosicion().y, suY1 = oponente.getPosicion().y + ALTO;
        	boolean seCruzanEnY = miY0 < suY1 && suY0 < miY1;
        	float miX0 = posicion.x,               miX1 = posicion.x + ANCHO;
        	float suX0 = oponente.getPosicion().x, suX1 = oponente.getPosicion().x + ANCHO;
        	float penetracionX = Math.min(miX1, suX1) - Math.max(miX0, suX0);
        	float penetracionY = Math.min(miY1, suY1) - Math.max(miY0, suY0);
        	// Solo se empuja de costado si la superposicion mas chica es horizontal. Si es vertical
        	// (uno parado sobre la cabeza del otro), el apilado lo resuelve el aterrizaje.
        	if (seCruzanEnY && penetracionX > 0f && penetracionX <= penetracionY) {
        	    float miCentro = posicion.x + ANCHO / 2f;
        	    float suCentro = oponente.getPosicion().x + ANCHO / 2f;
        	    float direccion = (miCentro < suCentro) ? -1f : 1f;
        	    posicion.x += direccion * (penetracionX / 2f);
        	    posicion.x = arena.limitarX(posicion.x, ANCHO); // por si el empujón te saca por la pared
        	}
        }
        if(cajaEspadaOponente.overlaps(miCaja)) {
        	float miY0 = posicion.y,             miY1 = posicion.y + ALTO;
        	float suY0 = cajaEspadaOponente.y, suY1 = cajaEspadaOponente.y + cajaEspadaOponente.height;
        	float miX0 = posicion.x,               miX1 = posicion.x + ANCHO;
        	float suX0 = cajaEspadaOponente.x, suX1 = cajaEspadaOponente.x + cajaEspadaOponente.width;
        	float penetracionX = Math.min(miX1, suX1) - Math.max(miX0, suX0);
        	if (penetracionX > 0f) {
        	    float miCentro = posicion.x + ANCHO / 2f;
        	    float suCentro = oponente.getPosicion().x + ANCHO / 2f;
        	    float direccion = (miCentro < suCentro) ? -1f : 1f;
        	    posicion.x += direccion * (penetracionX / 2f);
        	    posicion.x = arena.limitarX(posicion.x, ANCHO); // por si el empujón te saca por la pared
        	}
        }
        if(cajaEspadaOponente.overlaps(getCajaEspada())) {
        	Rectangle miEspada = getCajaEspada();

        	float miY0 = miEspada.y,             miY1 = miEspada.y + miEspada.height;
        	float suY0 = cajaEspadaOponente.y, suY1 = cajaEspadaOponente.y + cajaEspadaOponente.height;
        	float miX0 = miEspada.x,               miX1 = miEspada.x + miEspada.width;
        	float suX0 = cajaEspadaOponente.x, suX1 = cajaEspadaOponente.x + cajaEspadaOponente.width;
        	float penetracionX = Math.min(miX1, suX1) - Math.max(miX0, suX0);
        	if (penetracionX > 0f) {
        	    float miCentro = posicion.x + ANCHO / 2f;
        	    float suCentro = oponente.getPosicion().x + ANCHO / 2f;
        	    float direccion = (miCentro < suCentro) ? -1f : 1f;
        	    posicion.x += direccion * (penetracionX / 2f);
        	    posicion.x = arena.limitarX(posicion.x, ANCHO); // por si el empujón te saca por la pared
        	    empujar(direccion, EMPUJE_CHOQUE);
        	}
        }
        if(cajaEspadaOponente.overlaps(getCajaEspada()) && !(tiempoParaRecuperarAtaque > 0f)) {
        	Rectangle miEspada = getCajaEspada();

        	float miY0 = miEspada.y,             miY1 = miEspada.y + miEspada.height;
        	float suY0 = cajaEspadaOponente.y, suY1 = cajaEspadaOponente.y + cajaEspadaOponente.height;
        	float miX0 = miEspada.x,               miX1 = miEspada.x + miEspada.width;
        	float suX0 = cajaEspadaOponente.x, suX1 = cajaEspadaOponente.x + cajaEspadaOponente.width;
        	float penetracionX = Math.min(miX1, suX1) - Math.max(miX0, suX0);
        	if (penetracionX > 0f) {
        		anguloObjetivo = (float) (Math.random() * 60f - 30f);
        	    tiempoParaNuevoObjetivo = 0.1f;
        	    tiempoParaRecuperarAtaque = 0.5f;
        	}
        }
        if(!(oponente.tiempoRestanteFlash > 0f) && vivo && cajaOponente.overlaps(getCajaEspada())) {
        	oponente.activarFlash(1f, this);
        	acabaDeGolpear = true;
        	contadorGolpes++;
        }
        if (posicion.y < -ALTO) {
            vivo = false;
        }
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
        dibujarBrazo(batch, recursos);
        dibujarEspada(batch, recursos);
        batch.setColor(Color.WHITE);
    }

    private void activarFlash(float duracion, Jugador atacante) {
        tiempoRestanteFlash = duracion; // duración del flash, ajustable
        recibirImpacto(atacante);
    }

    private boolean isVivo() {
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
    		vivo = false;
    	}
    }

    /**
     * Dibuja el antebrazo que sostiene la espada.
     *
     * <p>Usa exactamente la misma formula de posicion horizontal que {@link #dibujarEspada}, con
     * {@link #ANCHO_BRAZO} en vez del ancho de la espada: nace pegado al mismo borde del cuerpo y
     * se estira durante la estocada, asi que sigue a la espada automaticamente cuando cambia la
     * altura de guardia o se ataca, sin necesitar sincronizarlo a mano.
     */
    private void dibujarBrazo(SpriteBatch batch, RecursosGraficos recursos) {
        int atacar = estocada ? 1 : 0;
        float y = posicion.y + alturaEspada.getDesplazamientoY() - (ALTO_BRAZO - ALTO_ESPADA) / 2f;
        float x = mirandoDerecha
            ? posicion.x + ANCHO - 14f * ESCALA + (24f * ESCALA * atacar)
            : posicion.x - ANCHO_BRAZO + 14f * ESCALA - (24f * ESCALA * atacar);
        dibujarSprite(batch, recursos.brazo, x, y, ANCHO_BRAZO, ALTO_BRAZO);
    }

    private void dibujarEspada(SpriteBatch batch, RecursosGraficos recursos) {
        int atacar = estocada ? 1 : 0;
        float y = posicion.y + alturaEspada.getDesplazamientoY();
        float x = mirandoDerecha
            ? posicion.x + ANCHO - 14f * ESCALA + (24f * ESCALA * atacar)
            : posicion.x - ANCHO_ESPADA + 14f * ESCALA - (24f * ESCALA * atacar);

        // El pivote es el borde pegado al cuerpo: x=0 si mira a la derecha (la guarda
        // queda ahi), x=ANCHO_ESPADA si mira a la izquierda (ahi es donde el flip deja la guarda).
        float originX = mirandoDerecha ? 0f : ANCHO_ESPADA;
        float originY = ALTO_ESPADA / 2f;
        batch.draw(recursos.espada, x, y, originX, originY, ANCHO_ESPADA, ALTO_ESPADA,
            1f, 1f, anguloEspada,
            0, 0, recursos.espada.getWidth(), recursos.espada.getHeight(),
            !mirandoDerecha, false);
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

    public AlturaEspada getAlturaEspada() {
        return alturaEspada;
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
        destino.alturaEspada = alturaEspada;
        destino.mirandoDerecha = mirandoDerecha;
        destino.estocada = estocada;
        destino.vivo = vivo;
        destino.corriendo = corriendo;
        destino.anguloEspada = anguloEspada;
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
        alturaEspada = origen.alturaEspada;
        mirandoDerecha = origen.mirandoDerecha;
        estocada = origen.estocada;
        vivo = origen.vivo;
        corriendo = origen.corriendo;
        anguloEspada = origen.anguloEspada;
        tiempoRestanteFlash = origen.tiempoRestanteFlash;
        contadorGolpes = origen.contadorGolpes;
        contadorAterrizajes = origen.contadorAterrizajes;
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

    /** Copia defensiva: devolver el {@code Vector2} interno permitiria moverlo desde afuera. */
    public Vector2 getPosicion() {
        return new Vector2(posicion);
    }
}
