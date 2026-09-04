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

/**
 * Duelista controlado por un jugador humano.
 *
 * <p>Concentra estado (posicion, velocidad, altura de la guardia) y comportamiento (moverse, saltar,
 * dibujarse). Las teclas llegan desde afuera en un objeto {@link Controles}, asi la misma clase
 * sirve para los dos duelistas sin duplicar codigo.
 *
 * <p>Todavia no hay combate: esta es la base jugable sobre la que despues se agregan estocadas,
 * bloqueos y desarmes.
 */
public class Jugador {

    /** Ancho de la caja de colision, en pixeles. Coincide con el ancho del sprite. */
    public static final float ANCHO = 48f;

    /** Alto de la caja de colision, en pixeles. */
    public static final float ALTO = 96f;

    private static final float VELOCIDAD = 450f;
    private static final float IMPULSO_SALTO = 780f;
    private static final float GRAVEDAD = -2200f;

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
    
    private float anguloEspada = 0f;
    private float anguloObjetivo = 0f;
    private float tiempoParaNuevoObjetivo = 0f;
    
    private float tiempoParaRecuperarAtaque = 0f;

    private static final float VELOCIDAD_ANGULO = 6f;
    
    private float tiempoRestanteFlash;
    
    public static final float ANCHO_ESPADA = 56f;
    public static final float ALTO_ESPADA = 10f;

    private static final float ANCHO_BRAZO = 22f;
    private static final float ALTO_BRAZO = 8f;
    
    /**
     * @param x posicion horizontal inicial, en pixeles desde el borde izquierdo de la arena
     * @param mirandoDerecha hacia donde apunta la espada al empezar la ronda
     * @param tinte color con el que se pinta el sprite blanco, para distinguir a los duelistas
     */
    public Jugador(Controles controles, float x, boolean mirandoDerecha, Color tinte, int vida) {
        this.controles = controles;
        this.tinte = tinte;
        this.mirandoDerecha = mirandoDerecha;
        this.posicion.set(x, 0f);
        this.vida = vida;
    }

    /**
     * Avanza la simulacion del jugador un fotograma.
     *
     * <p>El orden importa: primero se lee la entrada, despues se integra la fisica y al final se
     * resuelven las colisiones. Si se resolvieran las colisiones antes de mover, el jugador podria
     * quedar dentro del piso durante un fotograma.
     *
     * @param delta segundos transcurridos desde el fotograma anterior
     */
    public void actualizar(float delta, Arena arena, Jugador oponente) {
    	if(vivo) {
    		leerEntrada();
    	}
        integrarFisica(delta);
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

    public Rectangle getCajaEspada() {
        float y = posicion.y + alturaEspada.getDesplazamientoY();
        float extension = estocada ? 24f : 0f; // mismo 20f que ya usás para dibujarla
        float x = mirandoDerecha
            ? posicion.x + ANCHO - 14f + extension
            : posicion.x - ANCHO_ESPADA + 14f - extension;
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
            velocidad.y = IMPULSO_SALTO;
            enElPiso = false;
        }
        if (Gdx.input.isKeyJustPressed(controles.getSubirEspada())) {
            alturaEspada = alturaEspada.subir();
        }
        if (Gdx.input.isKeyJustPressed(controles.getBajarEspada())) {
            alturaEspada = alturaEspada.bajar();
        }
    }

    private void integrarFisica(float delta) {
        // Multiplicar por delta hace que el movimiento sea independiente de los fotogramas por
        // segundo: la misma velocidad recorre la misma distancia en una maquina lenta y en una
        // rapida. Sin delta, el juego correria mas rapido en mejores placas de video.
        velocidad.y += GRAVEDAD * delta;
        posicion.x += velocidad.x * delta;
        posicion.y += velocidad.y * delta;
        if(velocidad.x > 0f) {
        	velocidad.x = Math.max(velocidad.x - 100f, 0);
        }else if(velocidad.x < 0f){
        	velocidad.x = Math.min(velocidad.x + 100f, 0);
        }
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
        	if (seCruzanEnY && penetracionX > 0f) {
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
        	    velocidad.x += 1200f*direccion;
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
        float piso = arena.getAlturaPiso();
        if (posicion.y <= piso) {
            posicion.y = piso;
            velocidad.y = 0f;
            enElPiso = true;
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
	    velocidad.x += direccion * 1200f;
	    velocidad.y += 400f;
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
     * se estira el mismo {@code 24f} durante la estocada, asi que sigue a la espada automaticamente
     * cuando cambia la altura de guardia o se ataca, sin necesitar sincronizarlo a mano.
     */
    private void dibujarBrazo(SpriteBatch batch, RecursosGraficos recursos) {
        int atacar = estocada ? 1 : 0;
        float y = posicion.y + alturaEspada.getDesplazamientoY() - (ALTO_BRAZO - ALTO_ESPADA) / 2f;
        float x = mirandoDerecha
            ? posicion.x + ANCHO - 14f + (24f * atacar)
            : posicion.x - ANCHO_BRAZO + 14f - (24f * atacar);
        dibujarSprite(batch, recursos.brazo, x, y, ANCHO_BRAZO, ALTO_BRAZO);
    }

    private void dibujarEspada(SpriteBatch batch, RecursosGraficos recursos) {
        int atacar = estocada ? 1 : 0;
        float anchoEspada = recursos.espada.getWidth();
        float altoEspada = recursos.espada.getHeight();
        float y = posicion.y + alturaEspada.getDesplazamientoY();
        float x = mirandoDerecha ? posicion.x + ANCHO - 14f + (24f*atacar) : posicion.x - anchoEspada + 14f - (24f*atacar);

        // El pivote es el borde pegado al cuerpo: x=0 si mira a la derecha (la guarda
        // queda ahi), x=anchoEspada si mira a la izquierda (ahi es donde el flip deja la guarda).
        float originX = mirandoDerecha ? 0f : anchoEspada;
        float originY = altoEspada / 2f;
        batch.draw(recursos.espada, x, y, originX, originY, anchoEspada, altoEspada,
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
