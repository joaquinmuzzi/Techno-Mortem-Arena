package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;

public class Jugador {
    private static final float ESCALA = Arena.ESCALA;

    public static final Color TINTE_UNO = new Color(0.42f, 0.78f, 1f, 1f);
    public static final Color TINTE_DOS = new Color(1f, 0.48f, 0.42f, 1f);

    public static final float ANCHO = 48f * ESCALA;

    public static final float ALTO = 96f * ESCALA;

    private static final float VELOCIDAD = 450f * ESCALA;
    public static final float IMPULSO_SALTO = 780f * ESCALA;
    private static final float GRAVEDAD = -2200f * ESCALA;

    private static final float RITMO_EMPUJE = 4f;
    private static final float EMPUJE_GOLPE = 0.5f * VELOCIDAD;
    private static final float EMPUJE_CHOQUE = 0.2f * VELOCIDAD;

    private static final float DURACION_ESTOCADA = 0.14f;
    private static final float RECARGA_ESTOCADA = 0.08f;
    private static final float VELOCIDAD_GIRO = 12f;
    private static final float VELOCIDAD_GIRO_BLOQUEADO = 3f;
    public static final int PARTES_POR_CORAZON = 6;
    private static final float RECARGA_PISTOLA = 0.22f;
    private static final float RECARGA_ESCOPETA = 0.6f;
    private static final float RECARGA_RIFLE = 0.12f;
    private static final float RECARGA_LANZACOHETES = 1f;
    static final float ABANICO_ESCOPETA = 0.3f;
    static final float DISPERSION_PERDIGON = 0.05f;
    private static final float VARIACION_VELOCIDAD_PERDIGON = 0.1f;
    private static final float DURACION_PUNIO = 0.16f;
    private static final float RECARGA_PUNIO = 0.35f;
    private static final float EMPUJE_PUNIO = 0.35f * VELOCIDAD;
    private static final float LADO_PUNIO = 36f * ESCALA;
    static final int CHANCE_DESARME = 3;
    static final float DESDE_DONDE_DESARMA = 0.5f;
    static final float HASTA_DONDE_SE_DESARMA = 0.6f;
    static final float GIRO_PARA_DESARMAR = 2.5f;
    private static final float VUELO_DESARME_Y = 900f * ESCALA;
    private static final float VUELO_DESARME_X = 220f * ESCALA;
    private static final float FLASH_BALA = 0.25f;
    private static final float EMPUJE_BALA = 0.08f * VELOCIDAD;
    private static final float LARGO_PISTOLA = 36f * ESCALA;
    private static final float DURACION_RODADA = 0.35f;
    private static final float RECARGA_RODADA = 0.9f;
    private static final float VELOCIDAD_RODADA = 1.8f * VELOCIDAD;
    public static final float ALTO_RODANDO = ALTO / 2f;
    private static final float ALTURA_HOMBRO = 56f * ESCALA;
    private static final float DESPLAZAMIENTO_HOMBRO = 10f * ESCALA;

    private final FuenteEntrada fuente;
    private final EntradaJugador entrada = new EntradaJugador();
    private final Color tinte;
    private final int vidaInicial;
    private final Vector2 posicion = new Vector2();
    private final Vector2 velocidad = new Vector2();
    private int vida;

    private boolean mirandoDerecha;
    private boolean enElPiso;
    private boolean vivo = true;
    private boolean desarmado;

    private float angulo;
    private float anguloObjetivo;
    private float anguloPrevio;
    private float tiempoEstocada;
    private float bloqueadoTimer;
    private float duracionEstocadaActual = DURACION_ESTOCADA;
    private float recargaEstocada;
    private float extension;
    private boolean rodando;
    private float tiempoRodada;
    private float recargaRodada;
    private float direccionRodada;
    private TipoArma arma = TipoArma.ESPADA;
    private int balas;
    private float recargaDisparo;
    private float velocidadDeGiro;
    private int balasSoltadas;
    private boolean punioConecto;
    private TipoArma armaSoltada;
    private Random azar = new Random();
    private final float[] tiempoPowerUp = new float[PowerUp.values().length];
    private final List<Proyectil> lanzados = new ArrayList<>();
    private final Hoja hoja = new Hoja();
    private final Hoja hojaDeBarrido = new Hoja();
    private final Rectangle limitesPropios = new Rectangle();
    private final Rectangle limitesAjenos = new Rectangle();

    private boolean entradaIzquierda;
    private boolean entradaDerecha;

    private boolean corriendo;
    private boolean enVuelo;
    private boolean acabaDeAterrizar;
    private boolean acabaDeGolpear;
    private int contadorGolpes;
    private int contadorAterrizajes;
    private int contadorDisparosPistola;
    private int contadorDisparosEscopeta;
    private int contadorEspadasEquipadas;
    private int contadorArmasDeFuegoEquipadas;
    private int contadorDisparosLaser;
    private int contadorDisparosBazuca;
    private int contadorPowerUps;

    private Bloque bloqueBajoPies;
    private boolean montadoSobreOponente;

    private float tiempoRestanteFlash;

    private float empujeX;

    public static final float ANCHO_ESPADA = Hoja.LARGO;
    public static final float ALTO_ESPADA = Hoja.GROSOR;

    private static final float ANCHO_PISTOLA = 30f * ESCALA;
    private static final float ALTO_PISTOLA = 12f * ESCALA;
    private static final float ANCHO_ESCOPETA = 46f * ESCALA;
    private static final float ANCHO_RIFLE = 50f * ESCALA;
    private static final float ANCHO_LANZACOHETES = 52f * ESCALA;
    private static final float ALTO_LANZACOHETES = 16f * ESCALA;
    public static final Color COLOR_ESPADA_LASER = new Color(1f, 0.25f, 0.25f, 1f);

    private static final float ANCHO_BRAZO = 22f * ESCALA;
    private static final float ALTO_BRAZO = 8f * ESCALA;

    public Jugador(FuenteEntrada fuente, float x, float y, boolean mirandoDerecha, Color tinte, int vida) {
        this.fuente = fuente;
        this.tinte = tinte;
        this.mirandoDerecha = mirandoDerecha;
        this.posicion.set(x, y);
        this.vidaInicial = vida * PARTES_POR_CORAZON;
        this.vida = vidaInicial;
        this.anguloObjetivo = mirandoDerecha ? 0f : MathUtils.PI;
        this.angulo = mirandoDerecha ? 0f : MathUtils.PI;
        this.anguloPrevio = angulo;
        actualizarHoja();
    }

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
        bloqueadoTimer = 0f;
        duracionEstocadaActual = DURACION_ESTOCADA;
        tiempoEstocada = 0f;
        recargaEstocada = 0f;
        extension = 0f;
        entradaIzquierda = false;
        entradaDerecha = false;
        rodando = false;
        tiempoRodada = 0f;
        recargaRodada = 0f;
        arma = TipoArma.ESPADA;
        balas = 0;
        recargaDisparo = 0f;
        punioConecto = false;
        armaSoltada = null;
        lanzados.clear();
        Arrays.fill(tiempoPowerUp, 0f);
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

    public void actualizar(float delta, Arena arena, Jugador oponente) {
        acabaDeAterrizar = false;
        acabaDeGolpear = false;
        boolean estabaEnPiso = enElPiso;
        anguloPrevio = angulo;
        if (vivo) {
            leerEntrada(arena, delta);
        }
        actualizarEstocada(delta);
        integrarFisica(delta, arena, oponente);
        apuntar(delta);
        velocidadDeGiro = delta > 0f ? Math.abs(Hoja.diferenciaAngular(anguloPrevio, angulo)) / delta : 0f;
        actualizarArma();
        actualizarHoja();
        resolverColisiones(arena, oponente);
        actualizarPiso(estabaEnPiso);
        if (tiempoRestanteFlash > 0f) {
            tiempoRestanteFlash -= delta;
        }
        if (bloqueadoTimer > 0f) {
            bloqueadoTimer -= delta;
        }
        for (int i = 0; i < tiempoPowerUp.length; i++) {
            tiempoPowerUp[i] = Math.max(0f, tiempoPowerUp[i] - delta);
        }
    }

    public void morir() {
        vivo = false;
    }

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

    public void impulsar(float velocidadVertical) {
        velocidad.y = velocidadVertical;
        enElPiso = false;
        montadoSobreOponente = false;
        bloqueBajoPies = null;
    }

    private float pivoteX() {
        return posicion.x + ANCHO / 2f + (mirandoDerecha ? DESPLAZAMIENTO_HOMBRO : -DESPLAZAMIENTO_HOMBRO);
    }

    private float pivoteY() {
        return posicion.y + (rodando ? ALTURA_HOMBRO / 2f : ALTURA_HOMBRO);
    }

    private void actualizarHoja() {
        hoja.colocar(pivoteX(), pivoteY(), angulo, extension * Hoja.ALCANCE_ESTOCADA);
    }

    private void apuntar(float delta) {
        if (!vivo || !entrada.hayPuntero) {
            return;
        }
        mirandoDerecha = entrada.apuntaX >= posicion.x + ANCHO / 2f;
        float dx = entrada.apuntaX - pivoteX();
        float dy = entrada.apuntaY - pivoteY();
        if (dx * dx + dy * dy > 1f) {
            anguloObjetivo = (float) Math.atan2(dy, dx);
        }
        float giroMaximo = (bloqueadoTimer > 0f ? VELOCIDAD_GIRO_BLOQUEADO : VELOCIDAD_GIRO) * delta;
        float diferencia = Hoja.diferenciaAngular(angulo, anguloObjetivo);
        angulo = normalizarAngulo(angulo + MathUtils.clamp(diferencia, -giroMaximo, giroMaximo));
    }

    private static float normalizarAngulo(float radianes) {
        return Hoja.diferenciaAngular(0f, radianes);
    }

    private void actualizarEstocada(float delta) {
        if (recargaEstocada > 0f) {
            recargaEstocada -= delta;
        }
        if (tiempoEstocada > 0f) {
            tiempoEstocada = Math.max(0f, tiempoEstocada - delta);
        }
        if (recargaDisparo > 0f) {
            recargaDisparo -= delta;
        }
        if (vivo && arma != null && arma.esDeFuego() && entrada.estocada && recargaDisparo <= 0f && balas > 0) {
            disparar();
        }
        if (vivo && arma == null && !desarmado && entrada.estocada && tiempoEstocada <= 0f && recargaEstocada <= 0f) {
            duracionEstocadaActual = DURACION_PUNIO;
            tiempoEstocada = DURACION_PUNIO;
            recargaEstocada = RECARGA_PUNIO;
            punioConecto = false;
        }
        if (vivo && arma == TipoArma.ESPADA && entrada.estocada && tiempoEstocada <= 0f && recargaEstocada <= 0f) {
            duracionEstocadaActual = DURACION_ESTOCADA * (bloqueadoTimer > 0f ? 1.2f : 1f);
            tiempoEstocada = duracionEstocadaActual;
            recargaEstocada = RECARGA_ESTOCADA * (bloqueadoTimer > 0f ? 6f : 1f);
        }
        extension = tiempoEstocada > 0f
            ? (float) Math.sin(Math.PI * (1f - tiempoEstocada / duracionEstocadaActual))
            : 0f;
    }

    private void disparar() {
        float x = pivoteX() + (float) Math.cos(angulo) * LARGO_PISTOLA;
        float y = pivoteY() + (float) Math.sin(angulo) * LARGO_PISTOLA;
        if (arma == TipoArma.ESCOPETA) {
            for (int i = 0; i < TipoArma.PERDIGONES; i++) {
                float abanico = tienePowerUp(PowerUp.PUNTERIA) ? PowerUp.FACTOR_ABANICO : 1f;
                float desvio = abanico * (ABANICO_ESCOPETA * ((float) i / (TipoArma.PERDIGONES - 1) - 0.5f)
                    + (azar.nextFloat() * 2f - 1f) * DISPERSION_PERDIGON);
                float velocidad = Proyectil.VELOCIDAD_PERDIGON
                    * (1f + (azar.nextFloat() * 2f - 1f) * VARIACION_VELOCIDAD_PERDIGON);
                lanzados.add(new Proyectil(Proyectil.Tipo.PERDIGON, this, x, y, angulo + desvio, velocidad));
            }
            recargaDisparo = RECARGA_ESCOPETA * factorRecarga();
            contadorDisparosEscopeta++;
        } else if (arma == TipoArma.RIFLE_LASER) {
            lanzados.add(new Proyectil(Proyectil.Tipo.LASER, this, x, y, angulo, Proyectil.VELOCIDAD_LASER));
            recargaDisparo = RECARGA_RIFLE * factorRecarga();
            contadorDisparosLaser++;
        } else if (arma == TipoArma.LANZACOHETES) {
            lanzados.add(new Proyectil(Proyectil.Tipo.COHETE, this, x, y, angulo, Proyectil.VELOCIDAD_COHETE));
            recargaDisparo = RECARGA_LANZACOHETES * factorRecarga();
            contadorDisparosBazuca++;
        } else {
            lanzados.add(new Proyectil(Proyectil.Tipo.BALA, this, x, y, angulo, Proyectil.VELOCIDAD_BALA));
            recargaDisparo = RECARGA_PISTOLA * factorRecarga();
            contadorDisparosPistola++;
        }
        balas--;
        if (balas <= 0) {
            arma = null;
        }
    }

    private void actualizarArma() {
        if (arma == null || !vivo || desarmado || !entrada.lanzar) {
            return;
        }
        lanzados.add(new Proyectil(Proyectil.lanzadaDe(arma), this, pivoteX(), pivoteY(), angulo, Proyectil.VELOCIDAD_LANZADA)
            .conBalas(balas));
        arma = null;
        balas = 0;
        tiempoEstocada = 0f;
        extension = 0f;
    }

    private boolean tieneEspada() {
        return !desarmado && arma != null && arma.esEspada();
    }

    public boolean tieneHoja() {
        return tieneEspada();
    }

    private void leerEntrada(Arena arena, float delta) {
        fuente.leer(entrada);
        entradaIzquierda = entrada.izquierda;
        entradaDerecha = entrada.derecha;

        if (entradaIzquierda && !entradaDerecha) {
            velocidad.x += -(VELOCIDAD * factorVelocidad() * (bloqueadoTimer > 0f ? 0.5f : 1f) + velocidad.x);
        } else if (entradaDerecha && !entradaIzquierda) {
            velocidad.x += VELOCIDAD * factorVelocidad() * (bloqueadoTimer > 0f ? 0.5f : 1f) - velocidad.x;
        }

        actualizarRodada(arena, delta);
        if (rodando) {
            velocidad.x = direccionRodada * VELOCIDAD_RODADA * factorVelocidad();
        } else if (enElPiso && entrada.saltar) {
            impulsar(IMPULSO_SALTO * (tienePowerUp(PowerUp.SALTO) ? PowerUp.FACTOR_SALTO : 1f));
        }
    }

    private void actualizarRodada(Arena arena, float delta) {
        if (recargaRodada > 0f) {
            recargaRodada -= delta;
        }
        if (rodando) {
            tiempoRodada -= delta;
            if (tiempoRodada <= 0f && hayLugarParaPararse(arena)) {
                rodando = false;
            }
        } else if (entrada.rodar && enElPiso && recargaRodada <= 0f) {
            rodando = true;
            tiempoRodada = DURACION_RODADA;
            recargaRodada = RECARGA_RODADA;
            if (entradaIzquierda != entradaDerecha) {
                direccionRodada = entradaDerecha ? 1f : -1f;
            } else {
                direccionRodada = mirandoDerecha ? 1f : -1f;
            }
        }
    }

    private boolean hayLugarParaPararse(Arena arena) {
        Rectangle parado = new Rectangle(posicion.x, posicion.y, ANCHO, ALTO);
        for (Bloque bloque : arena.getBloques()) {
            if (bloque.getArea().overlaps(parado)) {
                return false;
            }
        }
        return true;
    }

    private float altoActual() {
        return rodando ? ALTO_RODANDO : ALTO;
    }

    private void integrarFisica(float delta, Arena arena, Jugador oponente) {
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

        posicion.x += (velocidad.x + empujeX) * delta;
        empujeX *= (float) Math.exp(-RITMO_EMPUJE * delta);
        resolverBloquesEnX(arena);

        float yAnterior = posicion.y;
        if (montadoAhora) {
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

    private void resolverBloquesEnY(Arena arena, float yAnterior, Bloque bloqueAnterior) {
        for (Bloque bloque : arena.getBloques()) {
            Rectangle caja = bloque.getArea();
            if (!cuerpo().overlaps(caja)) {
                continue;
            }
            bloque.alTocar(this);
            boolean estabaArriba = yAnterior >= caja.y + caja.height;
            boolean estabaAbajo = yAnterior + altoActual() <= caja.y;
            if (estabaArriba || (!estabaAbajo && velocidad.y <= 0f)) {
                posicion.y = caja.y + caja.height;
                velocidad.y = 0f;
                enElPiso = true;
                bloqueBajoPies = bloque;
                if (bloque != bloqueAnterior) {
                    bloque.alAterrizar(this);
                }
            } else {
                posicion.y = caja.y - altoActual();
                velocidad.y = 0f;
            }
        }
    }

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

    private void empujar(float direccion, float distancia) {
        empujeX = direccion * distancia * RITMO_EMPUJE;
    }

    private Rectangle cuerpo() {
        return new Rectangle(posicion.x, posicion.y, ANCHO, altoActual());
    }

    private void resolverColisiones(Arena arena, Jugador oponente) {
        posicion.x = arena.limitarX(posicion.x, ANCHO);
        Hoja hojaOponente = oponente.hoja;

        Rectangle miCaja = cuerpo();
        Rectangle cajaOponente = oponente.cuerpo();
        if (miCaja.overlaps(cajaOponente)) {
            float miY0 = posicion.y, miY1 = posicion.y + altoActual();
            float suY0 = oponente.posicion.y, suY1 = oponente.posicion.y + oponente.altoActual();
            boolean seCruzanEnY = miY0 < suY1 && suY0 < miY1;
            float miX0 = posicion.x, miX1 = posicion.x + ANCHO;
            float suX0 = oponente.posicion.x, suX1 = oponente.posicion.x + ANCHO;
            float penetracionX = Math.min(miX1, suX1) - Math.max(miX0, suX0);
            float penetracionY = Math.min(miY1, suY1) - Math.max(miY0, suY0);
            if (seCruzanEnY && penetracionX > 0f && penetracionX <= penetracionY) {
                separarDe(oponente, penetracionX, arena);
            }
        }

        if (oponente.tieneEspada() && hojaOponente.solapa(miCaja)) {
            float penetracionX = penetracionEnX(hojaOponente.limites(limitesAjenos), miCaja);
            if (penetracionX > 0f) {
                separarDe(oponente, penetracionX, arena);
            }
        }

        boolean seAtraviesan = (arma == TipoArma.ESPADA_LASER) != (oponente.arma == TipoArma.ESPADA_LASER);
        if (tieneEspada() && oponente.tieneEspada() && !seAtraviesan && hoja.solapa(hojaOponente)) {
            if (oponente.arma != TipoArma.ESPADA_LASER && desarmaAlChocar(hojaOponente)) {
                float lado = oponente.posicion.x >= posicion.x ? 1f : -1f;
                oponente.soltarEspadaAlAire(lado);
                registrarGolpe();
            } else {
                float penetracionX = penetracionEnX(hoja.limites(limitesPropios), hojaOponente.limites(limitesAjenos));
                if (penetracionX > 0f) {
                    float direccion = separarDe(oponente, penetracionX, arena);
                    empujar(direccion, EMPUJE_CHOQUE);
                    bloqueadoTimer = 1f;
                }
            }
        }

        if (tieneEspada() && !(oponente.tiempoRestanteFlash > 0f) && vivo && golpea(cajaOponente)) {
            oponente.activarFlash(1f, this);
            registrarGolpe();
        }

        if (arma == null && !desarmado && vivo && tiempoEstocada > 0f && !punioConecto && punioToca(cajaOponente)) {
            punioConecto = true;
            float direccion = oponente.posicion.x + ANCHO / 2f >= posicion.x + ANCHO / 2f ? 1f : -1f;
            if (oponente.recibirPunetazo(direccion)) {
                registrarGolpe();
            }
        }
        if (posicion.y < -ALTO) {
            vivo = false;
        }
    }

    private float separarDe(Jugador oponente, float penetracionX, Arena arena) {
        float miCentro = posicion.x + ANCHO / 2f;
        float suCentro = oponente.posicion.x + ANCHO / 2f;
        float direccion = (miCentro < suCentro) ? -1f : 1f;
        posicion.x += direccion * (penetracionX / 2f);
        posicion.x = arena.limitarX(posicion.x, ANCHO);
        return direccion;
    }

    private static float penetracionEnX(Rectangle a, Rectangle b) {
        return Math.min(a.x + a.width, b.x + b.width) - Math.max(a.x, b.x);
    }

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
        dibujarSprite(batch, recursos.jugador, posicion.x, posicion.y, ANCHO, altoActual());
        if (!desarmado) {
            dibujarBrazo(batch, recursos);
            if (arma == TipoArma.ESPADA) {
                batch.setColor(Color.WHITE);
                dibujarEspada(batch, recursos);
            } else if (arma == TipoArma.PISTOLA) {
                batch.setColor(Color.WHITE);
                dibujarGirado(batch, recursos.pistola, ANCHO_PISTOLA, ALTO_PISTOLA);
            } else if (arma == TipoArma.ESCOPETA) {
                batch.setColor(Color.WHITE);
                dibujarGirado(batch, recursos.escopeta, ANCHO_ESCOPETA, ALTO_PISTOLA);
            } else if (arma == TipoArma.RIFLE_LASER) {
                batch.setColor(Color.WHITE);
                dibujarGirado(batch, recursos.rifleLaser, ANCHO_RIFLE, ALTO_PISTOLA);
            } else if (arma == TipoArma.LANZACOHETES) {
                batch.setColor(Color.WHITE);
                dibujarGirado(batch, recursos.lanzacohetes, ANCHO_LANZACOHETES, ALTO_LANZACOHETES);
            } else if (arma == TipoArma.ESPADA_LASER) {
                batch.setColor(COLOR_ESPADA_LASER);
                dibujarEspada(batch, recursos);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void activarFlash(float duracion, Jugador atacante) {
        tiempoRestanteFlash = duracion;
        recibirImpacto(atacante);
    }

    public boolean isVivo() {
        return vivo;
    }

    private void recibirImpacto(Jugador atacante) {
    	float miCentro = posicion.x + ANCHO / 2f;
	    float suCentro = atacante.getPosicion().x + ANCHO / 2f;
	    float direccion = (miCentro < suCentro) ? -1f : 1f;
	    empujar(direccion, EMPUJE_GOLPE);
	    velocidad.y += 400f * ESCALA;
	    quitarVida(atacante.arma != null && atacante.arma.esEspada() ? atacante.arma.getDanioHoja() : PARTES_POR_CORAZON);
    }

    private boolean desarmaAlChocar(Hoja hojaOponente) {
        return esDesarme(hoja, hojaOponente, velocidadDeGiro >= GIRO_PARA_DESARMAR || tiempoEstocada > 0f);
    }

    static boolean esDesarme(Hoja mia, Hoja suya, boolean laMuevo) {
        if (!laMuevo || !mia.solapa(suya)) {
            return false;
        }
        float[] contacto = mia.puntoDeContacto(suya);
        return contacto[0] >= DESDE_DONDE_DESARMA && contacto[1] <= HASTA_DONDE_SE_DESARMA;
    }

    void soltarEspadaAlAire(float lado) {
        lanzados.add(Proyectil.inofensiva(Proyectil.lanzadaDe(arma), this, hoja.getBaseX(), hoja.getBaseY(),
            lado * VUELO_DESARME_X, VUELO_DESARME_Y));
        arma = null;
        balas = 0;
        tiempoEstocada = 0f;
        extension = 0f;
        bloqueadoTimer = 0f;
    }

    private boolean punioToca(Rectangle cuerpoAjeno) {
        float x = hoja.getBaseX() + (float) Math.cos(angulo) * LADO_PUNIO / 2f;
        float y = hoja.getBaseY() + (float) Math.sin(angulo) * LADO_PUNIO / 2f;
        return cuerpoAjeno.overlaps(new Rectangle(x - LADO_PUNIO / 2f, y - LADO_PUNIO / 2f, LADO_PUNIO, LADO_PUNIO));
    }

    boolean recibirPunetazo(float direccion) {
        if (!vivo || tiempoRestanteFlash > 0f) {
            return false;
        }
        tiempoRestanteFlash = FLASH_BALA;
        empujar(direccion, EMPUJE_PUNIO);
        quitarVida(TipoArma.DANIO_PUNIO);
        if (!desarmado && arma != null && arma != TipoArma.ESPADA_LASER && azar.nextInt(CHANCE_DESARME) == 0) {
            armaSoltada = arma;
            balasSoltadas = balas;
            arma = null;
            balas = 0;
            tiempoEstocada = 0f;
            extension = 0f;
        }
        return true;
    }

    public TipoArma tomarArmaSoltada() {
        TipoArma soltada = armaSoltada;
        armaSoltada = null;
        return soltada;
    }

    public int getBalasSoltadas() {
        return balasSoltadas;
    }

    void setAzar(Random azar) {
        this.azar = azar;
    }

    private void quitarVida(int partes) {
        vida = Math.max(0, vida - partes);
        if (vida == 0) {
            vivo = false;
        }
    }

    public boolean recibirDanio(int partes, float direccion, boolean esBala) {
        if (!vivo || tiempoRestanteFlash > 0f) {
            return false;
        }
        tiempoRestanteFlash = esBala ? FLASH_BALA : 1f;
        empujar(direccion, esBala ? EMPUJE_BALA : EMPUJE_GOLPE);
        if (!esBala) {
            velocidad.y += 400f * ESCALA;
        }
        quitarVida(partes);
        return true;
    }

    boolean recibirPerdigon(int partes, float direccion) {
        if (!vivo) {
            return false;
        }
        empujar(direccion, EMPUJE_BALA);
        quitarVida(partes);
        return true;
    }

    public void activarPowerUp(PowerUp mejora) {
        tiempoPowerUp[mejora.ordinal()] = PowerUp.DURACION;
        contadorPowerUps++;
    }

    public boolean tienePowerUp(PowerUp mejora) {
        return tiempoPowerUp[mejora.ordinal()] > 0f;
    }

    public float getTiempoPowerUp(PowerUp mejora) {
        return tiempoPowerUp[mejora.ordinal()];
    }

    private float factorVelocidad() {
        return tienePowerUp(PowerUp.VELOCIDAD) ? PowerUp.FACTOR_VELOCIDAD : 1f;
    }

    private float factorRecarga() {
        return tienePowerUp(PowerUp.PUNTERIA) ? PowerUp.FACTOR_RECARGA : 1f;
    }

    void registrarGolpe() {
        acabaDeGolpear = true;
        contadorGolpes++;
    }

    public void equipar(TipoArma nueva) {
        equipar(nueva, nueva.getBalasIniciales());
    }

    public void equipar(TipoArma nueva, int balasQueTrae) {
        arma = nueva;
        balas = nueva.esDeFuego() ? balasQueTrae : 0;
        tiempoEstocada = 0f;
        extension = 0f;
        if (nueva.esEspada()) {
            contadorEspadasEquipadas++;
        } else if (nueva.esDeFuego()) {
            contadorArmasDeFuegoEquipadas++;
        }
    }

    public void tomarLanzados(List<Proyectil> destino) {
        destino.addAll(lanzados);
        lanzados.clear();
    }

    public TipoArma getArma() {
        return arma;
    }

    public int getBalas() {
        return balas;
    }

    public Rectangle getCaja() {
        return cuerpo();
    }

    public boolean isDesarmado() {
        return desarmado;
    }

    public int getPartes() {
        return vida;
    }

    private void dibujarBrazo(SpriteBatch batch, RecursosGraficos recursos) {
        dibujarGirado(batch, recursos.brazo, ANCHO_BRAZO, ALTO_BRAZO);
    }

    private void dibujarEspada(SpriteBatch batch, RecursosGraficos recursos) {
        dibujarGirado(batch, recursos.espada, ANCHO_ESPADA, ALTO_ESPADA);
    }

    private void dibujarGirado(SpriteBatch batch, Texture textura, float ancho, float alto) {
        boolean haciaLaIzquierda = Math.cos(angulo) < 0.0;
        batch.draw(textura, hoja.getBaseX(), hoja.getBaseY() - alto / 2f,
            0f, alto / 2f, ancho, alto, 1f, 1f, angulo * MathUtils.radiansToDegrees,
            0, 0, textura.getWidth(), textura.getHeight(), false, haciaLaIzquierda);
    }

    private void dibujarSprite(SpriteBatch batch, Texture textura,
                               float x, float y, float ancho, float alto) {
        batch.draw(textura, x, y, ancho, alto,
            0, 0, textura.getWidth(), textura.getHeight(),
            !mirandoDerecha, false);
    }

    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }

    public boolean estaCorriendo() {
        return corriendo;
    }

    public boolean estaRodando() {
        return rodando;
    }

    public boolean acabaDeGolpear() {
        return acabaDeGolpear;
    }

    public boolean acabaDeAterrizar() {
        return acabaDeAterrizar;
    }

    public void exportar(EstadoJugador destino) {
        destino.x = posicion.x;
        destino.y = posicion.y;
        destino.vida = vida;
        destino.mirandoDerecha = mirandoDerecha;
        destino.vivo = vivo;
        destino.corriendo = corriendo;
        destino.rodando = rodando;
        destino.arma = TipoArma.codigoDe(arma);
        destino.balas = balas;
        System.arraycopy(tiempoPowerUp, 0, destino.tiempoPowerUp, 0, tiempoPowerUp.length);
        destino.anguloEspada = angulo;
        destino.extension = extension;
        destino.tiempoRestanteFlash = tiempoRestanteFlash;
        destino.contadorGolpes = contadorGolpes;
        destino.contadorAterrizajes = contadorAterrizajes;
        destino.contadorDisparosPistola = contadorDisparosPistola;
        destino.contadorDisparosEscopeta = contadorDisparosEscopeta;
        destino.contadorEspadasEquipadas = contadorEspadasEquipadas;
        destino.contadorArmasDeFuegoEquipadas = contadorArmasDeFuegoEquipadas;
        destino.contadorDisparosLaser = contadorDisparosLaser;
        destino.contadorDisparosBazuca = contadorDisparosBazuca;
        destino.contadorPowerUps = contadorPowerUps;
    }

    public void importar(EstadoJugador origen) {
        posicion.set(origen.x, origen.y);
        vida = origen.vida;
        mirandoDerecha = origen.mirandoDerecha;
        vivo = origen.vivo;
        corriendo = origen.corriendo;
        rodando = origen.rodando;
        arma = TipoArma.porCodigo(origen.arma);
        balas = origen.balas;
        System.arraycopy(origen.tiempoPowerUp, 0, tiempoPowerUp, 0, tiempoPowerUp.length);
        angulo = origen.anguloEspada;
        anguloObjetivo = origen.anguloEspada;
        extension = origen.extension;
        tiempoRestanteFlash = origen.tiempoRestanteFlash;
        contadorGolpes = origen.contadorGolpes;
        contadorAterrizajes = origen.contadorAterrizajes;
        contadorDisparosPistola = origen.contadorDisparosPistola;
        contadorDisparosEscopeta = origen.contadorDisparosEscopeta;
        contadorEspadasEquipadas = origen.contadorEspadasEquipadas;
        contadorArmasDeFuegoEquipadas = origen.contadorArmasDeFuegoEquipadas;
        contadorDisparosLaser = origen.contadorDisparosLaser;
        contadorDisparosBazuca = origen.contadorDisparosBazuca;
        contadorPowerUps = origen.contadorPowerUps;
        actualizarHoja();
    }

    public int getContadorGolpes() {
        return contadorGolpes;
    }

    public int getContadorAterrizajes() {
        return contadorAterrizajes;
    }

    public int getContadorDisparosPistola() {
        return contadorDisparosPistola;
    }

    public int getContadorDisparosEscopeta() {
        return contadorDisparosEscopeta;
    }

    public int getContadorEspadasEquipadas() {
        return contadorEspadasEquipadas;
    }

    public int getContadorArmasDeFuegoEquipadas() {
        return contadorArmasDeFuegoEquipadas;
    }

    public int getContadorDisparosLaser() {
        return contadorDisparosLaser;
    }

    public int getContadorDisparosBazuca() {
        return contadorDisparosBazuca;
    }

    public int getContadorPowerUps() {
        return contadorPowerUps;
    }

    public int getVida() {
        return (vida + PARTES_POR_CORAZON - 1) / PARTES_POR_CORAZON;
    }

    public void setDesarmado(boolean desarmado) {
        this.desarmado = desarmado;
    }

    public float getAngulo() {
        return angulo;
    }

    public float getExtension() {
        return extension;
    }

    public Hoja getHoja() {
        return hoja;
    }

    public Vector2 getPosicion() {
        return new Vector2(posicion);
    }
}
