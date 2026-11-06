package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.armas.Arma;
import com.et35.technomortemarena.entidades.armas.Espada;
import com.et35.technomortemarena.entidades.armas.Lanzacohetes;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import java.util.concurrent.ThreadLocalRandom;

public final class Proyectil {
    private static final float ESCALA = Arena.ESCALA;
    static final float DURACION_EXPLOSION = 0.4f;
    private static final float REBOTE = 0.25f;
    private static final float ROCE = 0.6f;
    private static final float VELOCIDAD_PARA_REBOTAR = 200f * ESCALA;
    private static final float FRICCION = 8f;
    private static final float VELOCIDAD_PARA_FRENAR = 60f * ESCALA;
    private static final float GIRO_LANZADA = 14f;
    static final float LADO_LANZADA = 28f * ESCALA;
    private static final float PASO_MAXIMO = 10f * ESCALA;
    private static final float ESPERA_REFLEJO = 0.1f;

    private final DefinicionProyectil definicion;
    private final Arma arma;
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
    private int balas;
    private float esperaReflejo;
    private final Rectangle caja = new Rectangle();

    public Proyectil(DefinicionProyectil definicion, Jugador duenio, float x, float y, float angulo, float velocidad) {
        this(definicion, null, duenio, x, y, angulo, velocidad);
    }

    private Proyectil(DefinicionProyectil definicion, Arma arma, Jugador duenio, float x, float y, float angulo,
                      float velocidad) {
        this.definicion = definicion;
        this.arma = arma;
        this.duenio = duenio;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.vx = (float) Math.cos(angulo) * velocidad;
        this.vy = (float) Math.sin(angulo) * velocidad;
    }

    static Proyectil explosion(float x, float y) {
        return new Proyectil(Lanzacohetes.EXPLOSION, null, x, y, 0f, 0f);
    }

    public static Proyectil lanzada(Arma arma, Jugador duenio, float x, float y, float angulo, float velocidad) {
        return new Proyectil(arma.getDefinicionLanzada(), arma, duenio, x, y, angulo, velocidad);
    }

    static Proyectil inofensiva(Arma arma, Jugador duenio, float x, float y, float vx, float vy) {
        Proyectil p = lanzada(arma, duenio, x, y, 0f, 0f);
        p.vx = vx;
        p.vy = vy;
        p.yaHirio = true;
        return p;
    }

    Proyectil conBalas(int balas) {
        this.balas = balas;
        return this;
    }

    public Arma getArma() {
        return arma;
    }

    public int getDanio() {
        return definicion.getDanio();
    }

    private boolean esDisparo() {
        return arma == null;
    }

    private boolean rebota() {
        return arma != null && arma.quedaEnElMapa(balas);
    }

    Jugador actualizar(float delta, Arena arena, Jugador uno, Jugador dos) {
        if (!activo) {
            return null;
        }
        tiempoDeVuelo += delta;
        if (definicion.getAspecto() == DefinicionProyectil.Aspecto.EXPLOSION) {
            angulo = Math.min(1f, tiempoDeVuelo / DURACION_EXPLOSION);
            activo = tiempoDeVuelo < DURACION_EXPLOSION;
            return null;
        }
        if (definicion.getAlcance() > 0f && tiempoDeVuelo >= definicion.getAlcance()) {
            activo = false;
            return null;
        }
        esperaReflejo = Math.max(0f, esperaReflejo - delta);
        vy += definicion.getGravedad() * delta;
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
            if (activo && definicion.isSeRefleja() && esperaReflejo <= 0f && (reflejarEn(uno) || reflejarEn(dos))) {
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
        if (definicion.isExplota()) {
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
        Espada espada = (Espada) jugador.getArma();
        float desvio = (float) ((ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * espada.getDesvioReflejo());
        float c = (float) Math.cos(desvio);
        float s = (float) Math.sin(desvio);
        float girado = rx * c - ry * s;
        ry = (rx * s + ry * c) * espada.getVelocidadReflejo();
        rx = girado * espada.getVelocidadReflejo();
        vx = rx;
        vy = ry;
        duenio = jugador;
        esperaReflejo = ESPERA_REFLEJO;
        tiempoDeVuelo = 0f;
        return true;
    }

    private void herir(Jugador tocado) {
        if (definicion.isExplota()) {
            terminarContraAlgo();
            return;
        }
        float direccion = vx >= 0f ? 1f : -1f;
        boolean hirio = definicion.isIgnoraInvulnerabilidad()
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
                if (arma.esEspada() && !tocandoPiso) {
                    caidasAlPiso++;
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
        float lado = definicion.getLado();
        return caja.set(x - lado / 2f, y - lado / 2f, lado, lado);
    }

    public boolean isActivo() {
        return activo;
    }

    public DefinicionProyectil getDefinicion() {
        return definicion;
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
