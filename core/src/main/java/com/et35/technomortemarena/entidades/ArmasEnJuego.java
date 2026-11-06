package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.armas.Arma;
import com.et35.technomortemarena.entidades.armas.ArmaDeFuego;
import com.et35.technomortemarena.entidades.armas.Armas;
import com.et35.technomortemarena.entidades.armas.Lanzacohetes;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class ArmasEnJuego {
    public static final float INTERVALO_APARICION = 8f;
    static final float GRAVEDAD_CAIDA = -1100f * Arena.ESCALA;
    static final float CAIDA_MAXIMA = -1400f * Arena.ESCALA;

    public static final float INTERVALO_POWER_UP = 12f;
    public static final int MAX_POWER_UPS = 2;
    public static final float LADO_POWER_UP = 40f * Arena.ESCALA;

    public static final float INTERVALO_SPAWNER = 5f;
    public static final int MAX_EN_EL_SUELO = 2;
    static final int MAX_PROYECTILES = 24;
    static final int MAX_ARMAS_TIRADAS = 6;
    private static final float ESCALA = Arena.ESCALA;
    public static final float ALTO_EN_EL_SUELO = 22f * ESCALA;
    private static final float ALTO_HALO = 18f * ESCALA;
    static final float ESPERA_PARA_AGARRAR = 0.8f;
    private static final Color COLOR_BALA = new Color(1f, 0.85f, 0.3f, 1f);
    private static final Color COLOR_LASER = new Color(0.35f, 1f, 1f, 1f);
    private static final Color COLOR_EXPLOSION = new Color(1f, 0.55f, 0.15f, 1f);

    public static final float RADIO_EXPLOSION = 4f * Arena.CELDA;
    public static final int MAX_CELDAS_ROTAS = 120;
    static final int CHANCE_ESPADA_LASER = 12;

    private final List<Proyectil> proyectiles = new ArrayList<>();
    private final List<float[]> enElSuelo = new ArrayList<>();
    private final List<Proyectil> nuevos = new ArrayList<>();
    private final List<Proyectil> explosiones = new ArrayList<>();
    private final Random azar;
    private final List<float[]> powerUps = new ArrayList<>();
    private float tiempoParaPowerUp = INTERVALO_POWER_UP;
    private final List<float[]> zonasProhibidas;
    private float tiempoParaAparicion = INTERVALO_APARICION;
    private final List<float[]> spawners = new ArrayList<>();
    private Arena arenaDeLosSpawners;
    private float tiempoAnimacion;
    private final List<int[]> celdasRotas = new ArrayList<>();
    private int contadorEspadasAlCaer;

    public static final int MARGEN_SPAWN = 4;

    public static float[] zonaDeSpawn(float x, float y, float ancho, float alto) {
        return new float[] {
            x / Arena.CELDA - MARGEN_SPAWN, y / Arena.CELDA - MARGEN_SPAWN,
            (x + ancho) / Arena.CELDA + MARGEN_SPAWN, (y + alto) / Arena.CELDA + MARGEN_SPAWN};
    }

    public ArmasEnJuego(Random azar, List<float[]> zonasProhibidas) {
        this.azar = azar;
        this.zonasProhibidas = new ArrayList<>(zonasProhibidas);
    }

    public void limpiar() {
        proyectiles.clear();
        enElSuelo.clear();
        tiempoParaAparicion = INTERVALO_APARICION;
        arenaDeLosSpawners = null;
        powerUps.clear();
        tiempoParaPowerUp = INTERVALO_POWER_UP;
        celdasRotas.clear();
        explosiones.clear();
    }

    public void actualizar(float delta, Arena arena, Jugador uno, Jugador dos) {
        tiempoAnimacion += delta;
        nuevos.clear();
        uno.tomarLanzados(nuevos);
        if (dos != null) {
            dos.tomarLanzados(nuevos);
        }
        proyectiles.addAll(nuevos);
        for (Iterator<Proyectil> it = proyectiles.iterator(); it.hasNext(); ) {
            Proyectil proyectil = it.next();
            proyectil.actualizar(delta, arena, uno, dos);
            if (proyectil.exploto()) {
                explotar(proyectil.getX(), proyectil.getY(), proyectil.getDuenio(), arena, uno, dos);
            }
            contadorEspadasAlCaer += proyectil.tomarCaidasAlPiso();
            if (!proyectil.isActivo()) {
                if (proyectil.dejaArmaEnElSuelo()) {
                    Arma tipo = proyectil.getArma();
                    dejarEnElSuelo(tipo, proyectil.getX() - tipo.getAnchoEnElSuelo() / 2f,
                        proyectil.getY() - Proyectil.LADO_LANZADA / 2f, 0f, proyectil.getBalas());
                }
                it.remove();
            }
        }
        proyectiles.addAll(explosiones);
        explosiones.clear();
        juntarSoltadas(uno);
        juntarSoltadas(dos);
        for (Iterator<float[]> it = enElSuelo.iterator(); it.hasNext(); ) {
            float[] arma = it.next();
            arma[3] = Math.max(0f, arma[3] - delta);
            if (arma[7] == 1f && !caer(arma, delta, arena)) {
                it.remove();
            }
        }
        while (proyectiles.size() > MAX_PROYECTILES) {
            proyectiles.remove(0);
        }
        tiempoParaAparicion -= delta;
        if (tiempoParaAparicion <= 0f) {
            tiempoParaAparicion = INTERVALO_APARICION;
            if (armasDeFuegoEnElSuelo() < MAX_EN_EL_SUELO) {
                aparecerArma(arena);
            }
        }
        actualizarSpawners(delta, arena);
        actualizarPowerUps(delta, arena, uno, dos);
        agarrar(uno);
        agarrar(dos);
    }

    private void actualizarPowerUps(float delta, Arena arena, Jugador uno, Jugador dos) {
        tiempoParaPowerUp -= delta;
        if (tiempoParaPowerUp <= 0f) {
            tiempoParaPowerUp = INTERVALO_POWER_UP;
            if (powerUps.size() < MAX_POWER_UPS) {
                aparecerPowerUp(arena);
            }
        }
        for (Iterator<float[]> it = powerUps.iterator(); it.hasNext(); ) {
            float[] mejora = it.next();
            if (mejora[4] == 1f) {
                mejora[3] = Math.max(CAIDA_MAXIMA, mejora[3] + GRAVEDAD_CAIDA * delta);
                float antes = mejora[2];
                float despues = antes + mejora[3] * delta;
                float apoyo = apoyoDebajo(arena, mejora[1], LADO_POWER_UP, antes, despues);
                if (!Float.isNaN(apoyo)) {
                    mejora[2] = apoyo;
                    mejora[3] = 0f;
                    mejora[4] = 0f;
                } else if (despues < -Arena.ALTO_MUNDO / 4f) {
                    it.remove();
                    continue;
                } else {
                    mejora[2] = despues;
                }
                continue;
            }
            Rectangle caja = new Rectangle(mejora[1], mejora[2], LADO_POWER_UP, LADO_POWER_UP);
            Jugador quien = agarraPowerUp(uno, caja) ? uno : agarraPowerUp(dos, caja) ? dos : null;
            if (quien != null) {
                quien.activarPowerUp(PowerUp.porIndice((int) mejora[0]));
                it.remove();
            }
        }
    }

    void explotar(float x, float y, Jugador duenio, Arena arena, Jugador uno, Jugador dos) {
        explosiones.add(Proyectil.explosion(x, y));
        herirConExplosion(uno, duenio, x, y);
        herirConExplosion(dos, duenio, x, y);
        String[] filas = arena.getFilas();
        int columnaImpacto = (int) Math.floor(x / Arena.CELDA);
        int filaImpacto = (int) Math.floor(y / Arena.CELDA);
        for (int columna = columnaImpacto - 1; columna <= columnaImpacto + 1; columna++) {
            for (int fila = filaImpacto - 1; fila <= filaImpacto + 1; fila++) {
                if (columna <= 0 || columna >= Arena.COLUMNAS - 1 || fila <= 0 || fila >= filas.length
                    || celdasRotas.size() >= MAX_CELDAS_ROTAS) {
                    continue;
                }
                char celda = celdaDe(filas, columna, fila);
                if (celda != '.' && celda != '1' && celda != '2' && !estaRota(columna, fila)) {
                    celdasRotas.add(new int[] {columna, fila});
                }
            }
        }
    }

    private static void herirConExplosion(Jugador jugador, Jugador duenio, float x, float y) {
        if (jugador == null || !jugador.isVivo()) {
            return;
        }
        Rectangle cuerpo = jugador.getCaja();
        float dx = MathUtils.clamp(x, cuerpo.x, cuerpo.x + cuerpo.width) - x;
        float dy = MathUtils.clamp(y, cuerpo.y, cuerpo.y + cuerpo.height) - y;
        if (dx * dx + dy * dy > RADIO_EXPLOSION * RADIO_EXPLOSION) {
            return;
        }
        float direccion = cuerpo.x + cuerpo.width / 2f >= x ? 1f : -1f;
        if (jugador.recibirDanio(Lanzacohetes.DANIO_EXPLOSION, direccion, false) && duenio != null && duenio != jugador) {
            duenio.registrarGolpe();
        }
    }

    private boolean estaRota(int columna, int fila) {
        for (int[] celda : celdasRotas) {
            if (celda[0] == columna && celda[1] == fila) {
                return true;
            }
        }
        return false;
    }

    public List<int[]> getCeldasRotas() {
        return celdasRotas;
    }

    private static boolean agarraPowerUp(Jugador jugador, Rectangle caja) {
        return jugador != null && jugador.isVivo() && !jugador.isDesarmado() && jugador.getCaja().overlaps(caja);
    }

    void aparecerPowerUp(Arena arena) {
        List<float[]> lugares = superficies(arena);
        if (lugares.isEmpty()) {
            return;
        }
        float[] lugar = lugares.get(azar.nextInt(lugares.size()));
        int tipo = azar.nextInt(PowerUp.values().length);
        powerUps.add(new float[] {tipo, lugar[0], Arena.ALTO_MUNDO - Arena.CELDA, 0f, 1f});
    }

    public List<float[]> getPowerUps() {
        return powerUps;
    }

    private static float apoyoDebajo(Arena arena, float x, float ancho, float antes, float despues) {
        float apoyo = Float.NaN;
        for (Bloque bloque : arena.getBloques()) {
            Rectangle area = bloque.getArea();
            float arriba = area.y + area.height;
            boolean debajo = area.x < x + ancho && area.x + area.width > x;
            if (debajo && arriba <= antes && arriba >= despues && (Float.isNaN(apoyo) || arriba > apoyo)) {
                apoyo = arriba;
            }
        }
        return apoyo;
    }

    private void actualizarSpawners(float delta, Arena arena) {
        if (arena != arenaDeLosSpawners) {
            arenaDeLosSpawners = arena;
            spawners.clear();
            spawners.addAll(buscarSpawners(arena));
        }
        for (float[] spawner : spawners) {
            spawner[4] -= delta;
            if (spawner[4] > 0f) {
                continue;
            }
            spawner[4] = INTERVALO_SPAWNER;
            if (!hayArmaEncima(spawner)) {
                Arma tipo = Armas.porCodigo((int) spawner[0]);
                float centro = (spawner[1] + spawner[2]) / 2f;
                enElSuelo.add(new float[] {spawner[0], centro - tipo.getAnchoEnElSuelo() / 2f, spawner[3], 0f,
                    tipo.getBalasIniciales(), 1f, 0f, 0f});
            }
        }
    }

    private boolean hayArmaEncima(float[] spawner) {
        for (float[] arma : enElSuelo) {
            float centro = arma[1] + Armas.porCodigo((int) arma[0]).getAnchoEnElSuelo() / 2f;
            boolean encima = centro >= spawner[1] && centro <= spawner[2]
                && arma[2] >= spawner[3] - 1f && arma[2] <= spawner[3] + 2f * Arena.CELDA;
            if (encima) {
                return true;
            }
        }
        return false;
    }

    static List<float[]> buscarSpawners(Arena arena) {
        String[] filas = arena.getFilas();
        int alto = filas.length;
        boolean[][] visto = new boolean[alto][Arena.COLUMNAS];
        List<float[]> encontrados = new ArrayList<>();
        for (int fila = 0; fila < alto; fila++) {
            for (int columna = 0; columna < Arena.COLUMNAS; columna++) {
                char celda = celdaDe(filas, columna, fila);
                if ((celda != 'P' && celda != 'E') || visto[fila][columna]) {
                    continue;
                }
                int minimaColumna = columna;
                int maximaColumna = columna;
                int filaMasAlta = fila;
                ArrayDeque<int[]> pendientes = new ArrayDeque<>();
                pendientes.add(new int[] {columna, fila});
                visto[fila][columna] = true;
                while (!pendientes.isEmpty()) {
                    int[] actual = pendientes.poll();
                    minimaColumna = Math.min(minimaColumna, actual[0]);
                    maximaColumna = Math.max(maximaColumna, actual[0]);
                    filaMasAlta = Math.max(filaMasAlta, actual[1]);
                    int[][] vecinos = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    for (int[] v : vecinos) {
                        int c = actual[0] + v[0];
                        int f = actual[1] + v[1];
                        if (c >= 0 && c < Arena.COLUMNAS && f >= 0 && f < alto && !visto[f][c]
                            && celdaDe(filas, c, f) == celda) {
                            visto[f][c] = true;
                            pendientes.add(new int[] {c, f});
                        }
                    }
                }
                Arma tipo = celda == 'P' ? Armas.PISTOLA : Armas.ESCOPETA;
                encontrados.add(new float[] {Armas.codigoDe(tipo), minimaColumna * Arena.CELDA,
                    (maximaColumna + 1) * Arena.CELDA, (filaMasAlta + 1) * Arena.CELDA, INTERVALO_SPAWNER});
            }
        }
        return encontrados;
    }

    private static char celdaDe(String[] filas, int columna, int fila) {
        return filas[filas.length - 1 - fila].charAt(columna);
    }

    private void agarrar(Jugador jugador) {
        if (jugador == null || !jugador.isVivo() || jugador.isDesarmado()) {
            return;
        }
        Rectangle cuerpo = jugador.getCaja();
        for (Iterator<float[]> it = enElSuelo.iterator(); it.hasNext(); ) {
            float[] arma = it.next();
            Arma tipo = Armas.porCodigo((int) arma[0]);
            if (arma[3] > 0f || arma[7] == 1f || jugador.getArma() != null) {
                continue;
            }
            if (cuerpo.overlaps(new Rectangle(arma[1], arma[2], tipo.getAnchoEnElSuelo(), ALTO_EN_EL_SUELO))) {
                it.remove();
                jugador.equipar(tipo, (int) arma[4]);
                return;
            }
        }
    }

    private void juntarSoltadas(Jugador jugador) {
        if (jugador == null) {
            return;
        }
        Arma soltada = jugador.tomarArmaSoltada();
        if (soltada != null) {
            dejarCayendo(soltada, jugador.getPosicion().x, jugador.getPosicion().y + 1f, jugador.getBalasSoltadas());
            enElSuelo.get(enElSuelo.size() - 1)[3] = ESPERA_PARA_AGARRAR;
        }
    }

    void dejarEnElSuelo(Arma tipo, float x, float y, float espera) {
        dejarEnElSuelo(tipo, x, y, espera, tipo.getBalasIniciales());
    }

    void dejarEnElSuelo(Arma tipo, float x, float y, float espera, int balas) {
        enElSuelo.add(new float[] {Armas.codigoDe(tipo), x, y, espera, balas, 0f, 0f, 0f});
        while (enElSuelo.size() > MAX_ARMAS_TIRADAS) {
            enElSuelo.remove(0);
        }
    }

    private int armasDeFuegoEnElSuelo() {
        int cantidad = cuantasHay(Armas.ESPADA_LASER);
        for (Arma tipo : Armas.queAparecen()) {
            cantidad += cuantasHay(tipo);
        }
        return cantidad;
    }

    private int cuantasHay(Arma tipo) {
        int cantidad = 0;
        for (float[] arma : enElSuelo) {
            if (Armas.porCodigo((int) arma[0]) == tipo && arma[5] == 0f) {
                cantidad++;
            }
        }
        return cantidad;
    }

    void aparecerArma(Arena arena) {
        List<float[]> lugares = superficies(arena);
        if (lugares.isEmpty()) {
            return;
        }
        float[] lugar = lugares.get(azar.nextInt(lugares.size()));
        Arma tipo;
        if (cuantasHay(Armas.ESPADA_LASER) == 0 && azar.nextInt(CHANCE_ESPADA_LASER) == 0) {
            tipo = Armas.ESPADA_LASER;
        } else {
            List<Arma> menos = new ArrayList<>();
            int minimo = Integer.MAX_VALUE;
            for (ArmaDeFuego candidata : Armas.queAparecen()) {
                int cantidad = cuantasHay(candidata);
                if (cantidad < minimo) {
                    minimo = cantidad;
                    menos.clear();
                }
                if (cantidad == minimo) {
                    menos.add(candidata);
                }
            }
            tipo = menos.get(azar.nextInt(menos.size()));
        }
        dejarCayendo(tipo, lugar[0], Arena.ALTO_MUNDO - Arena.CELDA, tipo.getBalasIniciales());
    }

    void dejarCayendo(Arma tipo, float x, float y, int balas) {
        dejarEnElSuelo(tipo, x, y, 0f, balas);
        enElSuelo.get(enElSuelo.size() - 1)[7] = 1f;
    }

    private boolean caer(float[] arma, float delta, Arena arena) {
        arma[6] = Math.max(CAIDA_MAXIMA, arma[6] + GRAVEDAD_CAIDA * delta);
        float antes = arma[2];
        float despues = antes + arma[6] * delta;
        float ancho = Armas.porCodigo((int) arma[0]).getAnchoEnElSuelo();
        float apoyo = apoyoDebajo(arena, arma[1], ancho, antes, despues);
        if (!Float.isNaN(apoyo)) {
            arma[2] = apoyo;
            arma[6] = 0f;
            arma[7] = 0f;
            return true;
        }
        arma[2] = despues;
        return despues > -Arena.ALTO_MUNDO / 4f;
    }

    List<float[]> superficies(Arena arena) {
        String[] filas = arena.getFilas();
        List<float[]> lugares = new ArrayList<>();
        int alto = filas.length;
        for (int fila = 0; fila < alto; fila++) {
            String texto = filas[alto - 1 - fila];
            for (int columna = 2; columna + 3 < Arena.COLUMNAS; columna++) {
                if (texto.charAt(columna) != '#' || texto.charAt(columna + 1) != '#') {
                    continue;
                }
                if (!esAire(filas, columna, fila + 1) || !esAire(filas, columna + 1, fila + 1)
                    || !esAire(filas, columna, fila + 2) || !esAire(filas, columna + 1, fila + 2)) {
                    continue;
                }
                if (enZonaProhibida(columna, fila + 1)) {
                    continue;
                }
                lugares.add(new float[] {columna * Arena.CELDA, (fila + 1) * Arena.CELDA});
            }
        }
        return lugares;
    }

    private static boolean esAire(String[] filas, int columna, int fila) {
        int indice = filas.length - 1 - fila;
        return indice < 0 || filas[indice].charAt(columna) == '.';
    }

    private boolean enZonaProhibida(int columna, int fila) {
        for (float[] zona : zonasProhibidas) {
            if (columna + 2 > zona[0] && columna < zona[2] && fila + 1 > zona[1] && fila < zona[3]) {
                return true;
            }
        }
        return false;
    }

    public List<Proyectil> getProyectiles() {
        return proyectiles;
    }

    public List<float[]> getEnElSuelo() {
        return enElSuelo;
    }

    public int getContadorEspadasAlCaer() {
        return contadorEspadasAlCaer;
    }

    public void dibujar(SpriteBatch batch, RecursosGraficos recursos) {
        float flote = (float) Math.sin(tiempoAnimacion * 4f) * 4f * ESCALA;
        for (float[] arma : enElSuelo) {
            Arma tipo = Armas.porCodigo((int) arma[0]);
            float ancho = tipo.getAnchoDibujado();
            float x = arma[1] + (tipo.getAnchoEnElSuelo() - ancho) / 2f;
            float y = arma[2] + 6f * ESCALA + flote;
            float brillo = 0.6f + 0.3f * (float) Math.sin(tiempoAnimacion * 6f);
            batch.setColor(1f, 1f, 1f, brillo);
            float margenX = 18f * ESCALA;
            float margenY = 14f * ESCALA;
            batch.draw(recursos.brillo, x - margenX, y - margenY, ancho + 2f * margenX, ALTO_HALO + 2f * margenY);
            batch.setColor(tipo.getColor());
            batch.draw(tipo.getTextura(recursos), x, y, ancho, tipo.getAltoDibujado());
        }
        for (float[] mejora : powerUps) {
            float y = mejora[2] + 4f * Arena.ESCALA + flote;
            float brillo = 0.6f + 0.3f * (float) Math.sin(tiempoAnimacion * 6f);
            batch.setColor(1f, 1f, 1f, brillo);
            float margen = 12f * Arena.ESCALA;
            batch.draw(recursos.brillo, mejora[1] - margen, y - margen, LADO_POWER_UP + 2f * margen, LADO_POWER_UP + 2f * margen);
            batch.setColor(Color.WHITE);
            batch.draw(texturaDePowerUp(recursos, PowerUp.porIndice((int) mejora[0])), mejora[1], y, LADO_POWER_UP, LADO_POWER_UP);
        }
        for (Proyectil p : proyectiles) {
            if (p.getArma() != null) {
                Arma tipo = p.getArma();
                batch.setColor(tipo.getColor());
                dibujarCentrado(batch, tipo.getTextura(recursos), p.getX(), p.getY(), tipo.getAnchoDibujado(),
                    tipo.getAltoDibujado(), p.getAngulo());
                continue;
            }
            switch (p.getDefinicion().getAspecto()) {
                case BALA:
                    batch.setColor(COLOR_BALA);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 14f * ESCALA, 4f * ESCALA, p.getAngulo());
                    break;
                case PERDIGON:
                    batch.setColor(COLOR_BALA);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 7f * ESCALA, 4f * ESCALA, p.getAngulo());
                    break;
                case LASER:
                    batch.setColor(COLOR_LASER.r, COLOR_LASER.g, COLOR_LASER.b, 0.35f);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 64f * ESCALA, 9f * ESCALA, p.getAngulo());
                    batch.setColor(COLOR_LASER);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 60f * ESCALA, 3f * ESCALA, p.getAngulo());
                    break;
                case COHETE:
                    float atras = 16f * ESCALA;
                    float estela = 26f * ESCALA * (0.8f + 0.2f * (float) Math.sin(tiempoAnimacion * 40f));
                    batch.setColor(COLOR_EXPLOSION);
                    batch.draw(recursos.brillo, p.getX() - (float) Math.cos(p.getAngulo()) * atras - estela / 2f,
                        p.getY() - (float) Math.sin(p.getAngulo()) * atras - estela / 2f, estela, estela);
                    batch.setColor(Color.WHITE);
                    dibujarCentrado(batch, recursos.cohete, p.getX(), p.getY(), 30f * ESCALA, 13f * ESCALA, p.getAngulo());
                    break;
                case EXPLOSION:
                    float avance = p.getAngulo();
                    float apagado = (float) Math.sqrt(1f - avance);
                    float diametro = 3f * RADIO_EXPLOSION * (0.6f + 0.4f * avance);
                    batch.setColor(COLOR_EXPLOSION.r, COLOR_EXPLOSION.g, COLOR_EXPLOSION.b, apagado);
                    for (int capa = 0; capa < 2; capa++) {
                        batch.draw(recursos.brillo, p.getX() - diametro / 2f, p.getY() - diametro / 2f, diametro, diametro);
                    }
                    batch.setColor(1f, 0.95f, 0.7f, apagado);
                    float centro = diametro * 0.5f;
                    for (int capa = 0; capa < 2; capa++) {
                        batch.draw(recursos.brillo, p.getX() - centro / 2f, p.getY() - centro / 2f, centro, centro);
                    }
                    break;
                default:
                    break;
            }
        }
        batch.setColor(Color.WHITE);
    }

    public static Texture texturaDePowerUp(RecursosGraficos recursos, PowerUp mejora) {
        switch (mejora) {
            case VELOCIDAD:
                return recursos.powerUpVelocidad;
            case SALTO:
                return recursos.powerUpSalto;
            default:
                return recursos.powerUpPunteria;
        }
    }

    private static void dibujarCentrado(SpriteBatch batch, Texture textura, float x, float y, float ancho, float alto, float angulo) {
        batch.draw(textura, x - ancho / 2f, y - alto / 2f, ancho / 2f, alto / 2f, ancho, alto, 1f, 1f,
            angulo * MathUtils.radiansToDegrees, 0, 0, textura.getWidth(), textura.getHeight(), false, false);
    }
}
