package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;

/**
 * Algo que vuela por la arena: una bala de pistola o un arma tirada. Avanza en linea recta (la bala) o
 * cae con gravedad girando (el arma tirada), se destruye al tocar un bloque o salir de la arena y hiere
 * al primer duelista que toca que no sea quien lo lanzo.
 */
public final class Proyectil {

    /** Que es: define como se mueve, como se dibuja y cuanto dano hace. */
    public enum Tipo { BALA, ESPADA_LANZADA, PISTOLA_LANZADA }

    private static final float ESCALA = Arena.ESCALA;
    public static final float VELOCIDAD_BALA = 1500f * ESCALA;
    public static final float VELOCIDAD_LANZADA = 1600f * ESCALA;
    private static final float GRAVEDAD_LANZADA = -350f * ESCALA;
    /** Vueltas por segundo del arma tirada, en radianes. */
    private static final float GIRO_LANZADA = 14f;
    /** Lado de la caja con que choca: la bala es chica, el arma tirada mas grande. */
    private static final float LADO_BALA = 8f * ESCALA;
    private static final float LADO_LANZADA = 28f * ESCALA;
    /** Pasos de la simulacion por fotograma como mucho de este largo: asi una bala rapida no atraviesa una celda. */
    private static final float PASO_MAXIMO = 10f * ESCALA;

    private final Tipo tipo;
    /** Quien lo lanzo: no se hiere a si mismo. Nulo en las copias que solo se dibujan (el cliente). */
    private final Jugador duenio;
    private float x;
    private float y;
    private float vx;
    private float vy;
    private float angulo;
    private boolean activo = true;
    private final Rectangle caja = new Rectangle();

    public Proyectil(Tipo tipo, Jugador duenio, float x, float y, float angulo, float velocidad) {
        this.tipo = tipo;
        this.duenio = duenio;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.vx = (float) Math.cos(angulo) * velocidad;
        this.vy = (float) Math.sin(angulo) * velocidad;
    }

    /** Una copia que solo se dibuja: la manda el anfitrion y no se simula. */
    public static Proyectil paraDibujar(Tipo tipo, float x, float y, float angulo) {
        return new Proyectil(tipo, null, x, y, angulo, 0f);
    }

    /** Tercios de corazon que saca al tocar a un duelista. */
    public int getDanio() {
        switch (tipo) {
            case BALA:
                return TipoArma.DANIO_BALA;
            case ESPADA_LANZADA:
                return TipoArma.ESPADA.getDanioLanzada();
            default:
                return TipoArma.PISTOLA.getDanioLanzada();
        }
    }

    /**
     * Avanza el proyectil y resuelve contra que choca.
     *
     * @return el duelista que toco (y que ya recibio el dano), o {@code null} si no toco a nadie
     */
    Jugador actualizar(float delta, Arena arena, Jugador uno, Jugador dos) {
        if (!activo) {
            return null;
        }
        boolean cae = tipo != Tipo.BALA;
        if (cae) {
            vy += GRAVEDAD_LANZADA * delta;
            angulo += (vx >= 0f ? -GIRO_LANZADA : GIRO_LANZADA) * delta;
        } else {
            angulo = (float) Math.atan2(vy, vx);
        }
        float dx = vx * delta;
        float dy = vy * delta;
        int pasos = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / PASO_MAXIMO));
        for (int i = 0; i < pasos && activo; i++) {
            x += dx / pasos;
            y += dy / pasos;
            Jugador tocado = tocaA(uno);
            if (tocado == null) {
                tocado = tocaA(dos);
            }
            if (tocado != null) {
                activo = false;
                float direccion = vx >= 0f ? 1f : -1f;
                if (tocado.recibirDanio(getDanio(), direccion, tipo == Tipo.BALA)) {
                    duenio.registrarGolpe();
                }
                return tocado;
            }
            if (chocaConBloque(arena) || x < -LADO_LANZADA || x > arena.getAncho() + LADO_LANZADA || y < -LADO_LANZADA) {
                activo = false;
            }
        }
        return null;
    }

    private Jugador tocaA(Jugador jugador) {
        if (jugador == null || jugador == duenio || !jugador.isVivo()) {
            return null;
        }
        return getCaja().overlaps(jugador.getCaja()) ? jugador : null;
    }

    private boolean chocaConBloque(Arena arena) {
        Rectangle propia = getCaja();
        for (Bloque bloque : arena.getBloques()) {
            if (bloque.getArea().overlaps(propia)) {
                return true;
            }
        }
        return false;
    }

    /** La caja con que choca, centrada en la posicion. */
    public Rectangle getCaja() {
        float lado = tipo == Tipo.BALA ? LADO_BALA : LADO_LANZADA;
        return caja.set(x - lado / 2f, y - lado / 2f, lado, lado);
    }

    public boolean isActivo() {
        return activo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAngulo() {
        return angulo;
    }
}
