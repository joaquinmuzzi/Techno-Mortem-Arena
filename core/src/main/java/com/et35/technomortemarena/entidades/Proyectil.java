package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;

/**
 * Algo que vuela por la arena: una bala, un perdigon de escopeta o un arma tirada.
 *
 * <p>Todos caen con gravedad: las balas un poco (de lejos hay que apuntar mas arriba), las armas tiradas
 * bastante. Un arma tirada rebota poco en el piso y las paredes y, cuando se frena apoyada sobre un bloque,
 * queda en el mapa para que cualquiera la agarre ({@link #dejaArmaEnElSuelo}), con las balas que tenia. Un
 * arma de fuego sin balas se rompe al chocar. Las balas y perdigones se destruyen al tocar un bloque.
 *
 * <p>Hiere al primer duelista que toca que no sea quien lo lanzo, salvo que sea inofensivo: la espada que
 * sale volando por un desarme no lastima a nadie.
 */
public final class Proyectil {

    /** Que es: define como se mueve, como se dibuja y cuanto dano hace. */
    public enum Tipo { BALA, PERDIGON, ESPADA_LANZADA, PISTOLA_LANZADA, ESCOPETA_LANZADA }

    private static final float ESCALA = Arena.ESCALA;
    public static final float VELOCIDAD_BALA = 1500f * ESCALA;
    public static final float VELOCIDAD_PERDIGON = 1300f * ESCALA;
    /** Tirar el arma es un ultimo recurso: sale mas despacio que una estocada de lejos. */
    public static final float VELOCIDAD_LANZADA = 1200f * ESCALA;
    /** Las balas caen poco: a media pantalla bajan como medio cuerpo. */
    static final float GRAVEDAD_BALA = -250f * ESCALA;
    /** Un arma tirada cae bastante: hay que tirarla en arco para llegar lejos. */
    static final float GRAVEDAD_LANZADA = -1100f * ESCALA;
    /** Segundos que vuela un perdigon antes de perderse: la escopeta es de corto alcance. */
    static final float VIDA_PERDIGON = 0.35f;
    /** Cuanto de la velocidad conserva al rebotar: poco, para que un rebote no sirva de ataque. */
    private static final float REBOTE = 0.25f;
    /** Cuanto de la velocidad horizontal le queda en cada pique contra el piso. */
    private static final float ROCE = 0.6f;
    /** Si cae mas lento que esto, ya no rebota: queda apoyada y se arrastra por el piso. */
    private static final float VELOCIDAD_PARA_REBOTAR = 200f * ESCALA;
    /** Cuanto frena por segundo arrastrandose por el piso. */
    private static final float FRICCION = 8f;
    /** Por debajo de esta velocidad, apoyada sobre un bloque, se queda quieta en el mapa. */
    private static final float VELOCIDAD_PARA_FRENAR = 60f * ESCALA;
    /** Vueltas por segundo del arma tirada, en radianes. */
    private static final float GIRO_LANZADA = 14f;
    /** Lado de la caja con que choca: la bala es chica, el arma tirada mas grande. */
    private static final float LADO_BALA = 8f * ESCALA;
    static final float LADO_LANZADA = 28f * ESCALA;
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
    private float tiempoDeVuelo;
    private boolean activo = true;
    /** Un arma tirada hiere una sola vez: despues de tocar a alguien sigue cayendo sin herir. */
    private boolean yaHirio;
    private boolean dejaArma;
    /** Si en este fotograma quedo apoyada sobre un bloque. */
    private boolean apoyada;
    /** Las balas que le quedan a un arma de fuego tirada: con balas queda en el mapa, sin balas se rompe. */
    private int balas;
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

    /**
     * Un arma que sale volando sin herir a nadie, con la velocidad dada: la espada que se suelta en un
     * desarme. Cae y queda en el mapa como cualquier arma tirada.
     */
    static Proyectil inofensiva(Tipo tipo, Jugador duenio, float x, float y, float vx, float vy) {
        Proyectil p = new Proyectil(tipo, duenio, x, y, 0f, 0f);
        p.vx = vx;
        p.vy = vy;
        p.yaHirio = true;
        return p;
    }

    /** Le pone las balas que tenia el arma de fuego en la mano al tirarla. */
    Proyectil conBalas(int balas) {
        this.balas = balas;
        return this;
    }

    /** El proyectil que deja el arma {@code arma} al tirarla. */
    static Tipo lanzadaDe(TipoArma arma) {
        switch (arma) {
            case ESPADA:
                return Tipo.ESPADA_LANZADA;
            case PISTOLA:
                return Tipo.PISTOLA_LANZADA;
            default:
                return Tipo.ESCOPETA_LANZADA;
        }
    }

    /** El arma que es, si es un arma tirada; {@code null} para balas y perdigones. */
    public TipoArma getArma() {
        switch (tipo) {
            case ESPADA_LANZADA:
                return TipoArma.ESPADA;
            case PISTOLA_LANZADA:
                return TipoArma.PISTOLA;
            case ESCOPETA_LANZADA:
                return TipoArma.ESCOPETA;
            default:
                return null;
        }
    }

    /** Partes de corazon que saca al tocar a un duelista. */
    public int getDanio() {
        switch (tipo) {
            case BALA:
                return TipoArma.DANIO_BALA;
            case PERDIGON:
                return TipoArma.DANIO_PERDIGON;
            default:
                return getArma().getDanioLanzada();
        }
    }

    private boolean esDisparo() {
        return tipo == Tipo.BALA || tipo == Tipo.PERDIGON;
    }

    /** La espada siempre queda en el mapa; un arma de fuego solo si le quedan balas. */
    private boolean rebota() {
        return tipo == Tipo.ESPADA_LANZADA || (!esDisparo() && balas > 0);
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
        tiempoDeVuelo += delta;
        if (tipo == Tipo.PERDIGON && tiempoDeVuelo >= VIDA_PERDIGON) {
            activo = false;
            return null;
        }
        vy += (esDisparo() ? GRAVEDAD_BALA : GRAVEDAD_LANZADA) * delta;
        if (esDisparo()) {
            angulo = (float) Math.atan2(vy, vx);
        } else if (!apoyada) {
            angulo += (vx >= 0f ? -GIRO_LANZADA : GIRO_LANZADA) * delta;
        }
        float dx = vx * delta;
        float dy = vy * delta;
        int pasos = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / PASO_MAXIMO));
        Jugador herido = null;
        apoyada = false;
        for (int i = 0; i < pasos && activo; i++) {
            if (rebota()) {
                moverRebotando(delta / pasos, arena);
            } else {
                x += dx / pasos;
                y += dy / pasos;
                if (chocaConBloque(arena)) {
                    activo = false;
                }
            }
            if (!yaHirio && activo) {
                Jugador tocado = tocaA(uno);
                if (tocado == null) {
                    tocado = tocaA(dos);
                }
                if (tocado != null) {
                    herir(tocado);
                    herido = tocado;
                }
            }
            if (y < -LADO_LANZADA) {
                activo = false; // se cayo por un agujero del mapa
            }
        }
        if (rebota() && activo && apoyada) {
            vx *= (float) Math.exp(-FRICCION * delta);
            if (Math.abs(vx) < VELOCIDAD_PARA_FRENAR) {
                quedarEnElSuelo();
            }
        }
        if (!rebota() && !esDisparo() && (x < -LADO_LANZADA || x > arena.getAncho() + LADO_LANZADA)) {
            activo = false;
        }
        return herido;
    }

    private void herir(Jugador tocado) {
        float direccion = vx >= 0f ? 1f : -1f;
        if (tocado.recibirDanio(getDanio(), direccion, esDisparo()) && duenio != null) {
            duenio.registrarGolpe();
        }
        if (rebota()) {
            // pega y cae casi recta a los pies del herido, sin volver a herir
            yaHirio = true;
            vx = -vx * 0.1f;
            vy = Math.min(vy, 0f);
        } else {
            activo = false;
        }
    }

    /**
     * Mueve el arma un paso y, si choca, rebota en el eje del choque: en una pared se da vuelta la
     * velocidad horizontal y en el piso o el techo la vertical, perdiendo casi toda la velocidad. Si cae
     * despacio ya no pica: queda apoyada y se arrastra hasta frenar. Las paredes de la arena tambien la
     * hacen rebotar.
     */
    private void moverRebotando(float paso, Arena arena) {
        // con la velocidad de ahora: si ya reboto en este fotograma, el resto del fotograma va para el otro lado
        float dx = vx * paso;
        float dy = vy * paso;
        x += dx;
        if (chocaConBloque(arena) || x < LADO_LANZADA / 2f || x > arena.getAncho() - LADO_LANZADA / 2f) {
            x -= dx;
            vx = -vx * REBOTE;
        }
        y += dy;
        if (chocaConBloque(arena)) {
            y -= dy;
            if (vy < 0f && -vy < VELOCIDAD_PARA_REBOTAR) {
                vy = 0f; // apoyada: ya no pica
                apoyada = true;
            } else {
                vy = -vy * REBOTE;
                vx *= ROCE;
            }
        }
    }

    private void quedarEnElSuelo() {
        activo = false;
        dejaArma = true;
    }

    /**
     * Verdadero si es un arma que termino de rebotar apoyada sobre un bloque: hay que dejarla en el mapa,
     * en {@link #getX} y {@link #getY}, para que la agarre cualquiera.
     */
    public boolean dejaArmaEnElSuelo() {
        return dejaArma;
    }

    /** Las balas que le quedan al arma de fuego tirada. */
    public int getBalas() {
        return balas;
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
        float lado = esDisparo() ? LADO_BALA : LADO_LANZADA;
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
