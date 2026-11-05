package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Las armas fuera de las manos durante una ronda: las balas y las armas tiradas que vuelan, y las armas
 * que aparecen en el mapa para que los duelistas las agarren.
 *
 * <p>Cada {@link #INTERVALO_APARICION} segundos, si hay menos de {@link #MAX_EN_EL_SUELO} armas de fuego
 * sueltas en el suelo, aparece una pistola o una escopeta (la que haya menos) en un lugar al azar sobre el
 * piso o una plataforma, fuera de las zonas prohibidas (los spawns). Ademas, cada bloque spawner del mapa
 * ({@code P} o {@code E} en la grilla) hace aparecer su arma encima cada {@link #INTERVALO_SPAWNER}
 * segundos, una sola a la vez. Las armas tiradas o que se caen de la mano quedan en el suelo con las balas
 * que tenian. Cualquier arma la agarra solo quien tiene la mano vacia, pasando por encima: para cambiar de
 * arma, primero hay que tirar la que se tiene. Lo simula el anfitrion (o el modo de prueba); el cliente
 * solo dibuja lo que le llega.
 */
public final class ArmasEnJuego {

    /** Segundos entre una aparicion y la siguiente. */
    public static final float INTERVALO_APARICION = 8f;
    /** Gravedad de las armas que caen: desde arriba del mapa tardan un par de segundos en llegar al piso. */
    static final float GRAVEDAD_CAIDA = -1100f * Arena.ESCALA;
    /** Tope de la velocidad de caida, para que se las pueda ver venir. */
    static final float CAIDA_MAXIMA = -1400f * Arena.ESCALA;

    /** Segundos entre una mejora y la siguiente, y cuantas puede haber en el mapa a la vez. */
    public static final float INTERVALO_POWER_UP = 12f;
    public static final int MAX_POWER_UPS = 2;
    /** Lado de la caja de una mejora: con eso se agarra, y asi de grande se dibuja. */
    public static final float LADO_POWER_UP = 40f * Arena.ESCALA;

    /** Segundos entre un arma y la siguiente en cada bloque spawner. */
    public static final float INTERVALO_SPAWNER = 5f;
    public static final int MAX_EN_EL_SUELO = 2;
    private static final float ESCALA = Arena.ESCALA;
    /** Caja de un arma de fuego en el suelo (la de la espada es su largo): con eso se agarra. */
    public static final float ANCHO_EN_EL_SUELO = 56f * ESCALA;
    public static final float ALTO_EN_EL_SUELO = 22f * ESCALA;
    /** Medidas con que se dibujan la pistola y la escopeta, en el suelo y en vuelo. */
    private static final float ANCHO_PISTOLA = 40f * ESCALA;
    private static final float ANCHO_ESCOPETA = 64f * ESCALA;
    private static final float ALTO_ARMA = 18f * ESCALA;
    /** Segundos que un arma recien caida no se puede agarrar: si no, quien la solto la levantaria al instante. */
    static final float ESPERA_PARA_AGARRAR = 0.8f;
    private static final Color COLOR_BALA = new Color(1f, 0.85f, 0.3f, 1f);
    private static final Color COLOR_LASER = new Color(0.35f, 1f, 1f, 1f);
    private static final Color COLOR_EXPLOSION = new Color(1f, 0.55f, 0.15f, 1f);
    private static final float ANCHO_RIFLE = 66f * ESCALA;
    private static final float ANCHO_LANZACOHETES = 66f * ESCALA;

    /** Radio de la explosion del cohete: a quien quede a menos de esto le saca un corazon, tambien al que lo tiro. */
    public static final float RADIO_EXPLOSION = 4f * Arena.CELDA;
    /** Tope de celdas rotas por ronda: lo que viaja por la red. */
    public static final int MAX_CELDAS_ROTAS = 120;
    /** Una de cada tantas armas que aparecen solas es la espada laser, si no hay otra en el mapa. */
    static final int CHANCE_ESPADA_LASER = 12;
    /** Las armas de fuego que aparecen solas en el mapa, repartidas: sale la que haya menos. */
    private static final TipoArma[] ARMAS_QUE_APARECEN = {
        TipoArma.PISTOLA, TipoArma.ESCOPETA, TipoArma.RIFLE_LASER, TipoArma.LANZACOHETES};

    private final List<Proyectil> proyectiles = new ArrayList<>();
    /**
     * Cada una como {@code {codigo de TipoArma, x, y, segundos hasta que se pueda agarrar, balas, 1 si la
     * hizo aparecer un spawner, velocidad vertical, 1 mientras cae}}. Las que caen no se pueden agarrar.
     */
    private final List<float[]> enElSuelo = new ArrayList<>();
    private final List<Proyectil> nuevos = new ArrayList<>();
    /** Las explosiones de este fotograma: se suman a los proyectiles despues de moverlos. */
    private final List<Proyectil> explosiones = new ArrayList<>();
    private final Random azar;
    /** Las mejoras en el mapa, como {@code {indice de PowerUp, x, y, velocidad vertical, 1 mientras cae}}. */
    private final List<float[]> powerUps = new ArrayList<>();
    private float tiempoParaPowerUp = INTERVALO_POWER_UP;
    private final List<float[]> zonasProhibidas;
    private float tiempoParaAparicion = INTERVALO_APARICION;
    /** Los spawners del mapa, como {@code {codigo de TipoArma, x0, x1, y de arriba, segundos para la proxima}}. */
    private final List<float[]> spawners = new ArrayList<>();
    /** La arena de la que salieron {@link #spawners}: si el mapa cambia (la edicion entre rondas), se vuelven a buscar. */
    private Arena arenaDeLosSpawners;
    private float tiempoAnimacion;
    /**
     * Las celdas que rompieron los cohetes en esta ronda, como {@code {columna, fila}} contando las filas desde
     * abajo. La pantalla rearma el mapa sin ellas; vuelven al empezar la ronda siguiente.
     */
    private final List<int[]> celdasRotas = new ArrayList<>();
    /** Veces que una espada tirada toco el piso desde que empezo la partida, para el sonido. */
    private int contadorEspadasAlCaer;

    /** @param zonasProhibidas zonas {@code {x0, y0, x1, y1}} en celdas donde no aparecen armas */
    public ArmasEnJuego(Random azar, List<float[]> zonasProhibidas) {
        this.azar = azar;
        this.zonasProhibidas = new ArrayList<>(zonasProhibidas);
    }

    /** Saca todo del mapa: se usa al empezar cada ronda. */
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

    /**
     * Un fotograma: junta lo que lanzaron los duelistas, mueve los proyectiles, hace aparecer armas y deja
     * que los duelistas las agarren. Se llama despues de actualizar a los dos duelistas.
     */
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
                    // el arma termino de rebotar: queda apoyada donde freno, con las balas que tenia
                    TipoArma tipo = proyectil.getArma();
                    dejarEnElSuelo(tipo, proyectil.getX() - anchoEnElSuelo(tipo) / 2f,
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
                it.remove(); // se cayo por un agujero del mapa
            }
        }
        while (proyectiles.size() > EstadoArmas.MAX_PROYECTILES) {
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

    /**
     * Cada {@link #INTERVALO_POWER_UP} segundos cae del cielo una mejora al azar sobre una superficie. Las que
     * ya se apoyaron las agarra el primer duelista que pasa por encima, tenga o no un arma en la mano.
     */
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

    /**
     * El cohete estalla en {@code (x, y)}: le saca un corazon a quien tenga el cuerpo a menos de
     * {@link #RADIO_EXPLOSION}, incluido el que lo tiro, y rompe los bloques de la celda del impacto y las ocho
     * de alrededor. Las paredes de los costados y la fila de abajo de todo no se rompen.
     */
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
        // la distancia al punto del cuerpo mas cercano a la explosion, no al centro: asi un cuerpo grande no la esquiva
        Rectangle cuerpo = jugador.getCaja();
        float dx = MathUtils.clamp(x, cuerpo.x, cuerpo.x + cuerpo.width) - x;
        float dy = MathUtils.clamp(y, cuerpo.y, cuerpo.y + cuerpo.height) - y;
        if (dx * dx + dy * dy > RADIO_EXPLOSION * RADIO_EXPLOSION) {
            return;
        }
        float direccion = cuerpo.x + cuerpo.width / 2f >= x ? 1f : -1f;
        if (jugador.recibirDanio(TipoArma.DANIO_EXPLOSION, direccion, false) && duenio != null && duenio != jugador) {
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

    /** Las celdas rotas por los cohetes en esta ronda, como {@code {columna, fila}} contando las filas desde abajo. */
    public List<int[]> getCeldasRotas() {
        return celdasRotas;
    }

    private static boolean agarraPowerUp(Jugador jugador, Rectangle caja) {
        return jugador != null && jugador.isVivo() && !jugador.isDesarmado() && jugador.getCaja().overlaps(caja);
    }

    /** Hace caer una mejora al azar desde arriba del mapa sobre una superficie al azar. */
    void aparecerPowerUp(Arena arena) {
        List<float[]> lugares = superficies(arena);
        if (lugares.isEmpty()) {
            return;
        }
        float[] lugar = lugares.get(azar.nextInt(lugares.size()));
        int tipo = azar.nextInt(PowerUp.values().length);
        powerUps.add(new float[] {tipo, lugar[0], Arena.ALTO_MUNDO - Arena.CELDA, 0f, 1f});
    }

    /** Las mejoras en el mapa, como {@code {indice de PowerUp, x, y, velocidad vertical, 1 mientras cae}}. */
    public List<float[]> getPowerUps() {
        return powerUps;
    }

    /**
     * La altura del bloque mas alto sobre el que se apoya algo de ancho {@code ancho} en {@code x} que cae de
     * {@code antes} a {@code despues}, o {@code NaN} si en ese tramo no toca ninguno.
     */
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

    /** Cada spawner cuenta su tiempo y, si no tiene un arma encima, hace aparecer la suya. */
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
                TipoArma tipo = TipoArma.porCodigo((int) spawner[0]);
                float centro = (spawner[1] + spawner[2]) / 2f;
                enElSuelo.add(new float[] {spawner[0], centro - anchoEnElSuelo(tipo) / 2f, spawner[3], 0f,
                    tipo.getBalasIniciales(), 1f, 0f, 0f});
            }
        }
    }

    /** Verdadero si sobre el spawner ya hay un arma esperando: no apila otra. */
    private boolean hayArmaEncima(float[] spawner) {
        for (float[] arma : enElSuelo) {
            float centro = arma[1] + anchoEnElSuelo(TipoArma.porCodigo((int) arma[0])) / 2f;
            boolean encima = centro >= spawner[1] && centro <= spawner[2]
                && arma[2] >= spawner[3] - 1f && arma[2] <= spawner[3] + 2f * Arena.CELDA;
            if (encima) {
                return true;
            }
        }
        return false;
    }

    /**
     * Los spawners del mapa: cada grupo de celdas pegadas {@code P} (pistolas) o {@code E} (escopetas) es uno,
     * y el arma aparece arriba de su fila mas alta, al medio.
     */
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
                // recorre el grupo de celdas pegadas del mismo tipo
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
                TipoArma tipo = celda == 'P' ? TipoArma.PISTOLA : TipoArma.ESCOPETA;
                encontrados.add(new float[] {TipoArma.codigoDe(tipo), minimaColumna * Arena.CELDA,
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
            TipoArma tipo = TipoArma.porCodigo((int) arma[0]);
            if (arma[3] > 0f || arma[7] == 1f || jugador.getArma() != null) {
                continue; // recien caida, o quien la pisa ya tiene algo en la mano: para cambiar, primero hay que tirarla
            }
            if (cuerpo.overlaps(new Rectangle(arma[1], arma[2], anchoEnElSuelo(tipo), ALTO_EN_EL_SUELO))) {
                it.remove();
                jugador.equipar(tipo, (int) arma[4]);
                return;
            }
        }
    }

    /** Las armas que se le cayeron a un duelista por un punetazo quedan a sus pies. */
    private void juntarSoltadas(Jugador jugador) {
        if (jugador == null) {
            return;
        }
        TipoArma soltada = jugador.tomarArmaSoltada();
        if (soltada != null) {
            // cae a los pies, o al piso si estaba en el aire
            dejarCayendo(soltada, jugador.getPosicion().x, jugador.getPosicion().y + 1f, jugador.getBalasSoltadas());
            enElSuelo.get(enElSuelo.size() - 1)[3] = ESPERA_PARA_AGARRAR;
        }
    }

    /** Deja un arma recien aparecida (cargada) en el suelo. */
    void dejarEnElSuelo(TipoArma tipo, float x, float y, float espera) {
        dejarEnElSuelo(tipo, x, y, espera, tipo.getBalasIniciales());
    }

    void dejarEnElSuelo(TipoArma tipo, float x, float y, float espera, int balas) {
        enElSuelo.add(new float[] {TipoArma.codigoDe(tipo), x, y, espera, balas, 0f, 0f, 0f});
        while (enElSuelo.size() > EstadoArmas.MAX_EN_EL_SUELO) {
            enElSuelo.remove(0);
        }
    }

    private int armasDeFuegoEnElSuelo() {
        int cantidad = cuantasHay(TipoArma.ESPADA_LASER);
        for (TipoArma tipo : ARMAS_QUE_APARECEN) {
            cantidad += cuantasHay(tipo);
        }
        return cantidad;
    }

    private int cuantasHay(TipoArma tipo) {
        int cantidad = 0;
        for (float[] arma : enElSuelo) {
            // las que puso un spawner no cuentan: esas aparecen por su cuenta
            if (TipoArma.porCodigo((int) arma[0]) == tipo && arma[5] == 0f) {
                cantidad++;
            }
        }
        return cantidad;
    }

    /** Ancho de la caja de un arma en el suelo: las espadas son mas largas. */
    static float anchoEnElSuelo(TipoArma tipo) {
        return tipo.esEspada() ? Jugador.ANCHO_ESPADA : ANCHO_EN_EL_SUELO;
    }

    private static float anchoDibujado(TipoArma tipo) {
        switch (tipo) {
            case ESCOPETA:
                return ANCHO_ESCOPETA;
            case RIFLE_LASER:
                return ANCHO_RIFLE;
            case LANZACOHETES:
                return ANCHO_LANZACOHETES;
            default:
                return ANCHO_PISTOLA;
        }
    }

    /**
     * Hace caer un arma de fuego (la que haya menos en el suelo) sobre una superficie al azar; muy de vez en
     * cuando, la espada laser. Sin lugar (un mapa sin piso), no pone nada.
     */
    void aparecerArma(Arena arena) {
        List<float[]> lugares = superficies(arena);
        if (lugares.isEmpty()) {
            return;
        }
        float[] lugar = lugares.get(azar.nextInt(lugares.size()));
        TipoArma tipo;
        if (cuantasHay(TipoArma.ESPADA_LASER) == 0 && azar.nextInt(CHANCE_ESPADA_LASER) == 0) {
            tipo = TipoArma.ESPADA_LASER;
        } else {
            // la que haya menos en el suelo, y entre las empatadas una al azar: asi con dos pistolas tiradas
            // igual aparece otra cosa
            List<TipoArma> menos = new ArrayList<>();
            int minimo = Integer.MAX_VALUE;
            for (TipoArma candidata : ARMAS_QUE_APARECEN) {
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
        // cae desde arriba del mapa sobre el lugar elegido: se la ve venir, y se agarra cuando toca el piso
        dejarCayendo(tipo, lugar[0], Arena.ALTO_MUNDO - Arena.CELDA, tipo.getBalasIniciales());
    }

    /** Deja un arma en el aire, cayendo: se agarra cuando se apoya en algo. */
    void dejarCayendo(TipoArma tipo, float x, float y, int balas) {
        dejarEnElSuelo(tipo, x, y, 0f, balas);
        enElSuelo.get(enElSuelo.size() - 1)[7] = 1f;
    }

    /**
     * Hace caer un arma un fotograma. Se apoya sobre el primer bloque que encuentra debajo.
     *
     * @return {@code false} si se cayo por debajo del mapa y hay que sacarla
     */
    private boolean caer(float[] arma, float delta, Arena arena) {
        arma[6] = Math.max(CAIDA_MAXIMA, arma[6] + GRAVEDAD_CAIDA * delta);
        float antes = arma[2];
        float despues = antes + arma[6] * delta;
        float ancho = anchoEnElSuelo(TipoArma.porCodigo((int) arma[0]));
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

    /**
     * Las celdas donde puede aparecer un arma: arriba de un bloque solido (no un pincho, trampolin ni
     * cinta), con aire encima y fuera de las zonas prohibidas. Devuelve la esquina inferior izquierda.
     */
    List<float[]> superficies(Arena arena) {
        String[] filas = arena.getFilas();
        List<float[]> lugares = new ArrayList<>();
        int alto = filas.length;
        for (int fila = 0; fila < alto; fila++) {
            String texto = filas[alto - 1 - fila];
            // sin las columnas de los bordes: pegada a la pared casi no se ve
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

    /** Las armas en el suelo, como {@code {codigo de TipoArma, x, y}}. */
    public List<float[]> getEnElSuelo() {
        return enElSuelo;
    }

    /** Veces que una espada tirada toco el piso; el contador solo sube, para disparar el sonido. */
    public int getContadorEspadasAlCaer() {
        return contadorEspadasAlCaer;
    }

    /** Copia en {@code destino} lo que hay que dibujar en el cliente. */
    public void exportar(EstadoArmas destino) {
        destino.proyectiles.clear();
        for (Proyectil p : proyectiles) {
            destino.proyectiles.add(new float[] {p.getTipo().ordinal(), p.getX(), p.getY(), p.getAngulo()});
        }
        destino.enElSuelo.clear();
        for (float[] arma : enElSuelo) {
            destino.enElSuelo.add(arma.clone());
        }
        destino.powerUps.clear();
        for (float[] mejora : powerUps) {
            destino.powerUps.add(new float[] {mejora[0], mejora[1], mejora[2]});
        }
        destino.celdasRotas.clear();
        for (int[] celda : celdasRotas) {
            destino.celdasRotas.add(celda.clone());
        }
        destino.contadorEspadasAlCaer = contadorEspadasAlCaer;
    }

    /** Pisa lo que hay con lo que mando el anfitrion. Lo usa el cliente, que no simula las armas. */
    public void importar(EstadoArmas origen, float delta) {
        tiempoAnimacion += delta;
        proyectiles.clear();
        for (float[] p : origen.proyectiles) {
            proyectiles.add(Proyectil.paraDibujar(Proyectil.Tipo.values()[(int) p[0]], p[1], p[2], p[3]));
        }
        enElSuelo.clear();
        for (float[] arma : origen.enElSuelo) {
            enElSuelo.add(arma.clone());
        }
        powerUps.clear();
        for (float[] mejora : origen.powerUps) {
            powerUps.add(mejora.clone());
        }
        celdasRotas.clear();
        for (int[] celda : origen.celdasRotas) {
            celdasRotas.add(celda.clone());
        }
        contadorEspadasAlCaer = origen.contadorEspadasAlCaer;
    }

    /** Dibuja las armas en el suelo y los proyectiles. Entre {@code batch.begin()} y {@code end()}. */
    public void dibujar(SpriteBatch batch, RecursosGraficos recursos) {
        float flote = (float) Math.sin(tiempoAnimacion * 4f) * 4f * ESCALA;
        for (float[] arma : enElSuelo) {
            TipoArma tipo = TipoArma.porCodigo((int) arma[0]);
            float ancho = tipo.esEspada() ? Jugador.ANCHO_ESPADA : anchoDibujado(tipo);
            float x = arma[1] + (anchoEnElSuelo(tipo) - ancho) / 2f;
            float y = arma[2] + 6f * ESCALA + flote;
            // un halo propio que late detras, para que se vea de lejos que hay algo para agarrar
            float brillo = 0.6f + 0.3f * (float) Math.sin(tiempoAnimacion * 6f);
            batch.setColor(1f, 1f, 1f, brillo);
            float margenX = 18f * ESCALA;
            float margenY = 14f * ESCALA;
            batch.draw(recursos.brillo, x - margenX, y - margenY, ancho + 2f * margenX, ALTO_ARMA + 2f * margenY);
            batch.setColor(tipo == TipoArma.ESPADA_LASER ? Jugador.COLOR_ESPADA_LASER : Color.WHITE);
            if (tipo.esEspada()) {
                batch.draw(recursos.espada, x, y, ancho, Jugador.ALTO_ESPADA);
            } else {
                batch.draw(texturaDe(recursos, tipo), x, y, ancho, ALTO_ARMA);
            }
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
            switch (p.getTipo()) {
                case BALA:
                    batch.setColor(COLOR_BALA);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 14f * ESCALA, 4f * ESCALA, p.getAngulo());
                    break;
                case PERDIGON:
                    batch.setColor(COLOR_BALA);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 7f * ESCALA, 4f * ESCALA, p.getAngulo());
                    break;
                case LASER:
                    // un rayo largo y finito, con un halo
                    batch.setColor(COLOR_LASER.r, COLOR_LASER.g, COLOR_LASER.b, 0.35f);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 64f * ESCALA, 9f * ESCALA, p.getAngulo());
                    batch.setColor(COLOR_LASER);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 60f * ESCALA, 3f * ESCALA, p.getAngulo());
                    break;
                case COHETE:
                    // la estela de fuego detras
                    float atras = 16f * ESCALA;
                    float estela = 26f * ESCALA * (0.8f + 0.2f * (float) Math.sin(tiempoAnimacion * 40f));
                    batch.setColor(COLOR_EXPLOSION);
                    batch.draw(recursos.brillo, p.getX() - (float) Math.cos(p.getAngulo()) * atras - estela / 2f,
                        p.getY() - (float) Math.sin(p.getAngulo()) * atras - estela / 2f, estela, estela);
                    batch.setColor(Color.WHITE);
                    dibujarCentrado(batch, recursos.cohete, p.getX(), p.getY(), 30f * ESCALA, 13f * ESCALA, p.getAngulo());
                    break;
                case EXPLOSION:
                    // el angulo lleva cuanto va, de 0 a 1: crece hasta el radio y se apaga
                    // el halo es difuso en los bordes: se dibuja mas grande que el radio para que se vea hasta donde pega
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
                case ESPADA_LANZADA:
                case ESPADA_LASER_LANZADA:
                    batch.setColor(p.getTipo() == Proyectil.Tipo.ESPADA_LASER_LANZADA ? Jugador.COLOR_ESPADA_LASER : Color.WHITE);
                    dibujarCentrado(batch, recursos.espada, p.getX(), p.getY(), Jugador.ANCHO_ESPADA, Jugador.ALTO_ESPADA, p.getAngulo());
                    break;
                default:
                    batch.setColor(Color.WHITE);
                    TipoArma tipo = p.getArma();
                    dibujarCentrado(batch, texturaDe(recursos, tipo), p.getX(), p.getY(), anchoDibujado(tipo), ALTO_ARMA, p.getAngulo());
                    break;
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** El icono de cada mejora; lo usa tambien la interfaz. */
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

    private static Texture texturaDe(RecursosGraficos recursos, TipoArma tipo) {
        switch (tipo) {
            case ESCOPETA:
                return recursos.escopeta;
            case RIFLE_LASER:
                return recursos.rifleLaser;
            case LANZACOHETES:
                return recursos.lanzacohetes;
            case ESPADA:
            case ESPADA_LASER:
                return recursos.espada;
            default:
                return recursos.pistola;
        }
    }

    private static void dibujarCentrado(SpriteBatch batch, Texture textura, float x, float y, float ancho, float alto, float angulo) {
        batch.draw(textura, x - ancho / 2f, y - alto / 2f, ancho / 2f, alto / 2f, ancho, alto, 1f, 1f,
            angulo * MathUtils.radiansToDegrees, 0, 0, textura.getWidth(), textura.getHeight(), false, false);
    }
}
