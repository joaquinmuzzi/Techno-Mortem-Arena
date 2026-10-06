package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entrada.Controles;
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

    private final Controles controles;
    private final Color tinte;
    private final Vector2 posicion = new Vector2();
    private final Vector2 velocidad = new Vector2();
    private int vida;

    private AlturaEspada alturaEspada = AlturaEspada.PECHO;
    private boolean mirandoDerecha;
    private boolean enElPiso;
    private boolean estocada;
    private boolean vivo = true;

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
        this.controles = controles;
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
    	if(vivo) {
    		leerEntrada();
    	}
        integrarFisica(delta, arena, oponente);
        resolverColisiones(arena, oponente);
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
        // isKeyPressed: verdadero mientras la tecla este hundida -> movimiento continuo.
        boolean izquierda = Gdx.input.isKeyPressed(controles.getIzquierda());
        boolean derecha = Gdx.input.isKeyPressed(controles.getDerecha());
        estocada = Gdx.input.isKeyPressed(controles.getEstocada());

        if (izquierda && !derecha) {
            velocidad.x += -(VELOCIDAD+velocidad.x);
            mirandoDerecha = false;
        } else if (derecha && !izquierda) {
            velocidad.x += VELOCIDAD-velocidad.x;
            mirandoDerecha = true;
        }

        // isKeyJustPressed: verdadero solo en el fotograma del pulsado -> una accion por pulsacion.
        // Con isKeyPressed, mantener la tecla saltaria en cada fotograma y cambiaria la guardia 60
        // veces por segundo.
        if (enElPiso && Gdx.input.isKeyJustPressed(controles.getSaltar())) {
            impulsar(IMPULSO_SALTO);
        }
        if (Gdx.input.isKeyJustPressed(controles.getSubirEspada())) {
            alturaEspada = alturaEspada.subir();
        }
        if (Gdx.input.isKeyJustPressed(controles.getBajarEspada())) {
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

    /** Copia defensiva: devolver el {@code Vector2} interno permitiria moverlo desde afuera. */
    public Vector2 getPosicion() {
        return new Vector2(posicion);
    }
}
