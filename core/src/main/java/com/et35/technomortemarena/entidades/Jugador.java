package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    private static final float VELOCIDAD = 320f;
    private static final float IMPULSO_SALTO = 780f;
    private static final float GRAVEDAD = -2200f;

    private final Controles controles;
    private final Color tinte;
    private final Vector2 posicion = new Vector2();
    private final Vector2 velocidad = new Vector2();

    private AlturaEspada alturaEspada = AlturaEspada.PECHO;
    private boolean mirandoDerecha;
    private boolean enElPiso;

    /**
     * @param x posicion horizontal inicial, en pixeles desde el borde izquierdo de la arena
     * @param mirandoDerecha hacia donde apunta la espada al empezar la ronda
     * @param tinte color con el que se pinta el sprite blanco, para distinguir a los duelistas
     */
    public Jugador(Controles controles, float x, boolean mirandoDerecha, Color tinte) {
        this.controles = controles;
        this.tinte = tinte;
        this.mirandoDerecha = mirandoDerecha;
        this.posicion.set(x, 0f);
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
    public void actualizar(float delta, Arena arena) {
        leerEntrada();
        integrarFisica(delta);
        resolverColisiones(arena);
    }

    private void leerEntrada() {
        // isKeyPressed: verdadero mientras la tecla este hundida -> movimiento continuo.
        boolean izquierda = Gdx.input.isKeyPressed(controles.getIzquierda());
        boolean derecha = Gdx.input.isKeyPressed(controles.getDerecha());

        velocidad.x = 0f;
        if (izquierda && !derecha) {
            velocidad.x = -VELOCIDAD;
            mirandoDerecha = false;
        } else if (derecha && !izquierda) {
            velocidad.x = VELOCIDAD;
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
    }

    private void resolverColisiones(Arena arena) {
        posicion.x = arena.limitarX(posicion.x, ANCHO);

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
        batch.setColor(tinte);
        dibujarSprite(batch, recursos.jugador, posicion.x, posicion.y, ANCHO, ALTO);
        dibujarEspada(batch, recursos);
        batch.setColor(Color.WHITE);
    }

    private void dibujarEspada(SpriteBatch batch, RecursosGraficos recursos) {
        float anchoEspada = recursos.espada.getWidth();
        float altoEspada = recursos.espada.getHeight();
        float y = posicion.y + alturaEspada.getDesplazamientoY();
        // La espada nace en el borde por el que mira el jugador, solapada unos pixeles con el brazo.
        float x = mirandoDerecha ? posicion.x + ANCHO - 14f : posicion.x - anchoEspada + 14f;
        dibujarSprite(batch, recursos.espada, x, y, anchoEspada, altoEspada);
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
