package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import java.util.concurrent.ThreadLocalRandom;

public final class Proyectil {
    public enum Tipo {
        BALA, PERDIGON, ESPADA_LANZADA, PISTOLA_LANZADA, ESCOPETA_LANZADA,
        LASER, COHETE, RIFLE_LASER_LANZADA, LANZACOHETES_LANZADA, ESPADA_LASER_LANZADA, EXPLOSION
    }

    private static final float ESCALA = Arena.ESCALA;
    public static final float VELOCIDAD_BALA = 1500f * ESCALA;
    public static final float VELOCIDAD_PERDIGON = 1300f * ESCALA;
    public static final float VELOCIDAD_LASER = 3000f * ESCALA;
    public static final float VELOCIDAD_COHETE = 700f * ESCALA;
    public static final float VELOCIDAD_LANZADA = 1200f * ESCALA;
    static final float GRAVEDAD_BALA = -250f * ESCALA;
    static final float GRAVEDAD_COHETE = -150f * ESCALA;
    static final float GRAVEDAD_LANZADA = -1100f * ESCALA;
    static final float GRAVEDAD_ESPADA_LASER = -500f * ESCALA;
    static final float VIDA_PERDIGON = 0.55f;
    static final float DURACION_EXPLOSION = 0.4f;
    private static final float REBOTE = 0.25f;
    private static final float ROCE = 0.6f;
    private static final float VELOCIDAD_PARA_REBOTAR = 200f * ESCALA;
    private static final float FRICCION = 8f;
    private static final float VELOCIDAD_PARA_FRENAR = 60f * ESCALA;
    private static final float GIRO_LANZADA = 14f;
    private static final float LADO_BALA = 8f * ESCALA;
    private static final float LADO_COHETE = 14f * ESCALA;
    static final float LADO_LANZADA = 28f * ESCALA;
    private static final float PASO_MAXIMO = 10f * ESCALA;
    static final float VELOCIDAD_REFLEJO_ESPADA = 0.8f;
    static final float DESVIO_REFLEJO_ESPADA = 0.12f;
    private static final float ESPERA_REFLEJO = 0.1f;

    private final Tipo tipo;
    private Jugador duenio;
    private float x;
    private float y;
    private float vx;
    private float vy;
    private float angulo;
    private float tiempoDeVuelo;
    private boolean activo = true;
    private boolean yaHirio;
    private boolean dejaArma;
    private boolean exploto;
    private boolean apoyada;
    private boolean tocandoPiso;
    private int caidasAlPiso;
    private int caidasDeArmaDeFuego;
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

    public static Proyectil paraDibujar(Tipo tipo, float x, float y, float angulo) {
        return new Proyectil(tipo, null, x, y, angulo, 0f);
    }

    static Proyectil explosion(float x, float y) {
        return new Proyectil(Tipo.EXPLOSION, null, x, y, 0f, 0f);
    }

    static Proyectil inofensiva(Tipo tipo, Jugador duenio, float x, float y, float vx, float vy) {
        Proyectil p = new Proyectil(tipo, duenio, x, y, 0f, 0f);
        p.vx = vx;
        p.vy = vy;
        p.yaHirio = true;
        return p;
    }

    Proyectil conBalas(int balas) {
        this.balas = balas;
        return this;
    }

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

    private boolean seRefleja() {
        return tipo == Tipo.BALA || tipo == Tipo.PERDIGON || tipo == Tipo.COHETE;
    }

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

    Jugador actualizar(float delta, Arena arena, Jugador uno, Jugador dos) {
        if (!activo) {
            return null;
        }
        tiempoDeVuelo += delta;
        if (tipo == Tipo.EXPLOSION) {
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
                activo = false;
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

    private void terminarContraAlgo() {
        activo = false;
        if (tipo == Tipo.COHETE) {
            exploto = true;
        }
    }

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
            tiempoDeVuelo = 0f;
        }
        return true;
    }

    private void herir(Jugador tocado) {
        if (tipo == Tipo.COHETE) {
            terminarContraAlgo();
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
            yaHirio = true;
            vx = -vx * 0.1f;
            vy = Math.min(vy, 0f);
        } else {
            activo = false;
        }
    }

    private void moverRebotando(float paso, Arena arena) {
        if (vy > 0f) {
            tocandoPiso = false;
        }
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
                if (!tocandoPiso) {
                    if (tipo == Tipo.ESPADA_LANZADA || tipo == Tipo.ESPADA_LASER_LANZADA) {
                        caidasAlPiso++;
                    } else if (esArmaDeFuegoTirada()) {
                        caidasDeArmaDeFuego++;
                    }
                }
                tocandoPiso = true;
                if (-vy < VELOCIDAD_PARA_REBOTAR) {
                    vy = 0f;
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

    public boolean dejaArmaEnElSuelo() {
        return dejaArma;
    }

    public boolean exploto() {
        return exploto;
    }

    Jugador getDuenio() {
        return duenio;
    }

    int tomarCaidasAlPiso() {
        int caidas = caidasAlPiso;
        caidasAlPiso = 0;
        return caidas;
    }

    int tomarCaidasDeArmaDeFuego() {
        int caidas = caidasDeArmaDeFuego;
        caidasDeArmaDeFuego = 0;
        return caidas;
    }

    private boolean esArmaDeFuegoTirada() {
        TipoArma arma = getArma();
        return arma != null && arma.esDeFuego();
    }

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
