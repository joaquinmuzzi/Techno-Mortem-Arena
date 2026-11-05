package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Algo que vuela por la arena: una bala, un perdigon, un rayo laser, un cohete, un arma tirada o la
 * explosion de un cohete (que solo se dibuja).
 *
 * <p>Casi todos caen con gravedad: las balas un poco (de lejos hay que apuntar mas arriba), el cohete muy
 * poco, las armas tiradas bastante (la espada laser menos, para tiros mas largos). El rayo laser no cae. Un
 * arma tirada rebota poco en el piso y las paredes y, cuando se frena apoyada sobre un bloque, queda en el
 * mapa para que cualquiera la agarre ({@link #dejaArmaEnElSuelo}), con las balas que tenia. Un arma de fuego
 * sin balas se rompe al chocar. Las balas, perdigones y rayos se destruyen al tocar un bloque, y el cohete
 * explota ({@link #exploto}).
 *
 * <p>Hiere al primer duelista que toca que no sea quien lo lanzo, salvo que sea inofensivo: la espada que
 * sale volando por un desarme no lastima a nadie. Las balas, perdigones y cohetes que tocan una hoja en la
 * mano de alguien rebotan espejados contra la hoja y pasan a ser de quien los reflejo; el rayo laser no.
 */
public final class Proyectil {

    /** Que es: define como se mueve, como se dibuja y cuanto dano hace. */
    public enum Tipo {
        BALA, PERDIGON, ESPADA_LANZADA, PISTOLA_LANZADA, ESCOPETA_LANZADA,
        LASER, COHETE, RIFLE_LASER_LANZADA, LANZACOHETES_LANZADA, ESPADA_LASER_LANZADA, EXPLOSION
    }

    private static final float ESCALA = Arena.ESCALA;
    public static final float VELOCIDAD_BALA = 1500f * ESCALA;
    public static final float VELOCIDAD_PERDIGON = 1300f * ESCALA;
    public static final float VELOCIDAD_LASER = 3000f * ESCALA;
    public static final float VELOCIDAD_COHETE = 700f * ESCALA;
    /** Tirar el arma es un ultimo recurso: sale mas despacio que una estocada de lejos. */
    public static final float VELOCIDAD_LANZADA = 1200f * ESCALA;
    /** Las balas caen poco: a media pantalla bajan como medio cuerpo. */
    static final float GRAVEDAD_BALA = -250f * ESCALA;
    /** El cohete apenas cae. */
    static final float GRAVEDAD_COHETE = -150f * ESCALA;
    /** Un arma tirada cae bastante: hay que tirarla en arco para llegar lejos. */
    static final float GRAVEDAD_LANZADA = -1100f * ESCALA;
    /** La espada laser tirada cae menos: llega mas lejos. */
    static final float GRAVEDAD_ESPADA_LASER = -500f * ESCALA;
    /** Segundos que vuela un perdigon antes de perderse: la escopeta es de corto alcance. */
    static final float VIDA_PERDIGON = 0.55f;
    /** Segundos que se ve la explosion de un cohete. */
    static final float DURACION_EXPLOSION = 0.4f;
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
    private static final float LADO_COHETE = 14f * ESCALA;
    static final float LADO_LANZADA = 28f * ESCALA;
    /** Pasos de la simulacion por fotograma como mucho de este largo: asi una bala rapida no atraviesa una celda. */
    private static final float PASO_MAXIMO = 10f * ESCALA;
    /** Una espada comun refleja perdiendo algo de velocidad y con un desvio al azar; la laser, perfecto. */
    static final float VELOCIDAD_REFLEJO_ESPADA = 0.8f;
    static final float DESVIO_REFLEJO_ESPADA = 0.12f;
    /** Despues de reflejarse no vuelve a reflejarse enseguida en la misma hoja (todavia la esta tocando). */
    private static final float ESPERA_REFLEJO = 0.1f;

    private final Tipo tipo;
    /** Quien lo lanzo (o el ultimo que lo reflejo): no se hiere a si mismo. Nulo en las copias del cliente. */
    private Jugador duenio;
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
    /** Si es un cohete que choco: quien lo maneja tiene que hacer la explosion en {@link #getX}, {@link #getY}. */
    private boolean exploto;
    /** Si en este fotograma quedo apoyada sobre un bloque. */
    private boolean apoyada;
    /** Verdadero mientras la espada tirada esta tocando el piso; evita contar el mismo apoyo en cada fotograma. */
    private boolean tocandoPiso;
    /** Veces que esta espada tirada toco el piso: las junta {@link ArmasEnJuego}. */
    private int caidasAlPiso;
    /** Las balas que le quedan a un arma de fuego tirada: con balas queda en el mapa, sin balas se rompe. */
    private int balas;
    private float esperaReflejo;
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

    /** La explosion de un cohete, que solo se dibuja: crece y se apaga en {@link #DURACION_EXPLOSION}. */
    static Proyectil explosion(float x, float y) {
        return new Proyectil(Tipo.EXPLOSION, null, x, y, 0f, 0f);
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
            case ESCOPETA:
                return Tipo.ESCOPETA_LANZADA;
            case RIFLE_LASER:
                return Tipo.RIFLE_LASER_LANZADA;
            case LANZACOHETES:
                return Tipo.LANZACOHETES_LANZADA;
            default:
                return Tipo.ESPADA_LASER_LANZADA;
        }
    }

    /** El arma que es, si es un arma tirada; {@code null} para lo que se dispara y la explosion. */
    public TipoArma getArma() {
        switch (tipo) {
            case ESPADA_LANZADA:
                return TipoArma.ESPADA;
            case PISTOLA_LANZADA:
                return TipoArma.PISTOLA;
            case ESCOPETA_LANZADA:
                return TipoArma.ESCOPETA;
            case RIFLE_LASER_LANZADA:
                return TipoArma.RIFLE_LASER;
            case LANZACOHETES_LANZADA:
                return TipoArma.LANZACOHETES;
            case ESPADA_LASER_LANZADA:
                return TipoArma.ESPADA_LASER;
            default:
                return null;
        }
    }

    /** Partes de corazon que saca al tocar a un duelista (el cohete saca con la explosion). */
    public int getDanio() {
        switch (tipo) {
            case BALA:
                return TipoArma.DANIO_BALA;
            case PERDIGON:
                return TipoArma.DANIO_PERDIGON;
            case LASER:
                return TipoArma.DANIO_LASER;
            case COHETE:
            case EXPLOSION:
                return 0;
            default:
                return getArma().getDanioLanzada();
        }
    }

    private boolean esDisparo() {
        return tipo == Tipo.BALA || tipo == Tipo.PERDIGON || tipo == Tipo.LASER || tipo == Tipo.COHETE;
    }

    /** Lo que se refleja en una hoja: todo lo que se dispara menos el rayo laser. */
    private boolean seRefleja() {
        return tipo == Tipo.BALA || tipo == Tipo.PERDIGON || tipo == Tipo.COHETE;
    }

    /** Las espadas siempre quedan en el mapa; un arma de fuego solo si le quedan balas. */
    private boolean rebota() {
        return tipo == Tipo.ESPADA_LANZADA || tipo == Tipo.ESPADA_LASER_LANZADA || (!esDisparo() && balas > 0);
    }

    private float gravedad() {
        switch (tipo) {
            case BALA:
            case PERDIGON:
                return GRAVEDAD_BALA;
            case LASER:
                return 0f;
            case COHETE:
                return GRAVEDAD_COHETE;
            case ESPADA_LASER_LANZADA:
                return GRAVEDAD_ESPADA_LASER;
            default:
                return GRAVEDAD_LANZADA;
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
        tiempoDeVuelo += delta;
        if (tipo == Tipo.EXPLOSION) {
            // solo se dibuja: el angulo lleva cuanto va de la explosion, de 0 a 1, para que el cliente la vea crecer
            angulo = Math.min(1f, tiempoDeVuelo / DURACION_EXPLOSION);
            activo = tiempoDeVuelo < DURACION_EXPLOSION;
            return null;
        }
        if (tipo == Tipo.PERDIGON && tiempoDeVuelo >= VIDA_PERDIGON) {
            activo = false;
            return null;
        }
        esperaReflejo = Math.max(0f, esperaReflejo - delta);
        vy += gravedad() * delta;
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
                // con la velocidad de ahora: si se reflejo en este fotograma, el resto va para el otro lado
                x += vx * delta / pasos;
                y += vy * delta / pasos;
                if (chocaConBloque(arena)) {
                    terminarContraAlgo();
                }
            }
            if (activo && seRefleja() && esperaReflejo <= 0f && (reflejarEn(uno) || reflejarEn(dos))) {
                continue;
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
        if (!rebota() && (x < -LADO_LANZADA || x > arena.getAncho() + LADO_LANZADA || y > arena.getAlto() * 2f)) {
            activo = false;
        }
        return herido;
    }

    /** Se termina contra un bloque o un cuerpo: el cohete explota, lo demas desaparece. */
    private void terminarContraAlgo() {
        activo = false;
        if (tipo == Tipo.COHETE) {
            exploto = true;
        }
    }

    /**
     * Si toca la hoja que {@code jugador} tiene en la mano, rebota espejado contra la hoja: la velocidad se
     * da vuelta en la direccion perpendicular a la hoja y se conserva a lo largo de ella, como una pelota
     * contra una pared inclinada. Con la espada comun pierde algo de velocidad y se desvia un poco; con la
     * laser rebota perfecto. Pasa a ser de quien lo reflejo, asi puede herir al que lo disparo.
     */
    private boolean reflejarEn(Jugador jugador) {
        if (jugador == null || jugador == duenio || !jugador.isVivo() || !jugador.tieneHoja()
            || !jugador.getHoja().solapa(getCaja())) {
            return false;
        }
        float a = jugador.getHoja().getAngulo();
        float nx = (float) -Math.sin(a);
        float ny = (float) Math.cos(a);
        float punto = vx * nx + vy * ny;
        float rx = vx - 2f * punto * nx;
        float ry = vy - 2f * punto * ny;
        if (jugador.getArma() != TipoArma.ESPADA_LASER) {
            float desvio = (float) ((ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * DESVIO_REFLEJO_ESPADA);
            float c = (float) Math.cos(desvio);
            float s = (float) Math.sin(desvio);
            float girado = rx * c - ry * s;
            ry = (rx * s + ry * c) * VELOCIDAD_REFLEJO_ESPADA;
            rx = girado * VELOCIDAD_REFLEJO_ESPADA;
        }
        vx = rx;
        vy = ry;
        duenio = jugador;
        esperaReflejo = ESPERA_REFLEJO;
        if (tipo == Tipo.PERDIGON) {
            tiempoDeVuelo = 0f; // el perdigon reflejado llega tan lejos como uno nuevo
        }
        return true;
    }

    private void herir(Jugador tocado) {
        if (tipo == Tipo.COHETE) {
            terminarContraAlgo(); // el dano lo hace la explosion
            return;
        }
        float direccion = vx >= 0f ? 1f : -1f;
        boolean hirio = tipo == Tipo.PERDIGON || tipo == Tipo.LASER
            ? tocado.recibirPerdigon(getDanio(), direccion)
            : tocado.recibirDanio(getDanio(), direccion, esDisparo());
        if (hirio && duenio != null) {
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
        if (vy > 0f) {
            // volvio a subir despues de un rebote: el proximo golpe contra el piso es otro
            tocandoPiso = false;
        }
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
            if (vy < 0f) {
                if ((tipo == Tipo.ESPADA_LANZADA || tipo == Tipo.ESPADA_LASER_LANZADA) && !tocandoPiso) {
                    caidasAlPiso++;
                }
                tocandoPiso = true;
                if (-vy < VELOCIDAD_PARA_REBOTAR) {
                    vy = 0f; // apoyada: ya no pica
                    apoyada = true;
                } else {
                    vy = -vy * REBOTE;
                    vx *= ROCE;
                }
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

    /** Verdadero si es un cohete que choco: hay que hacer la explosion donde esta. */
    public boolean exploto() {
        return exploto;
    }

    /** Quien lo lanzo o lo reflejo por ultima vez. */
    Jugador getDuenio() {
        return duenio;
    }

    /**
     * Cuantas veces esta espada tirada toco el piso desde la ultima vez, y lo reinicia. Las espadas
     * cuentan cada rebote; las balas y los perdigones no cuentan.
     */
    int tomarCaidasAlPiso() {
        int caidas = caidasAlPiso;
        caidasAlPiso = 0;
        return caidas;
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
        float lado = tipo == Tipo.COHETE ? LADO_COHETE : esDisparo() ? LADO_BALA : LADO_LANZADA;
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

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }

    public float getAngulo() {
        return angulo;
    }
}
